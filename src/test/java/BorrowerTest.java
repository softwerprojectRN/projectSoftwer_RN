import domain.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class BorrowerTest {

    private Borrower borrower;
    private MockedStatic<Borrower> mockedStaticBorrower;
    private MockedStatic<BorrowingService> mockedStaticBorrowingService;

    // Mocks for Database interaction
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPstmt;
    @Mock
    private ResultSet mockRs;

    private final String USERNAME = "testUser";
    private final String PASSWORD_HASH = "hash";
    private final String SALT = "salt";
    private final int USER_ID = 1;

    /**
     * إعداد تهيئة الاختبار قبل كل حالة اختبار.
     * يتضمن تزييف الدوال الثابتة وإعداد سيناريو البناء (Constructor).
     */
    @BeforeEach
    void setUp() throws SQLException {
        // 1. Mocking static methods for database connection and fine calculation
        mockedStaticBorrower = Mockito.mockStatic(Borrower.class);
        mockedStaticBorrower.when(Borrower::connect).thenReturn(mockConnection);

        // ... (Mocking BorrowingService remains the same)

        // 2. Mock setup for ALL prepareStatement calls during Borrower constructor:

        // Mock 1: getUserId (SELECT id FROM users...)
        // This is the FIRST SQL query the constructor will attempt
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockPstmt);
        when(mockPstmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true).thenReturn(false); // User found
        when(mockRs.getInt("id")).thenReturn(USER_ID);

        // Mock 2: loadBorrowedMedia (The query that is causing the error)
        // We must define a separate mock for this specific lengthy query, or use anyString() for simplicity.
        // **Using startsWith("SELECT br.*") is safer:**
        PreparedStatement mockMediaPstmt = mock(PreparedStatement.class);
        ResultSet mockMediaRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT br.*"))).thenReturn(mockMediaPstmt); // Query for loadBorrowedMedia
        when(mockMediaPstmt.executeQuery()).thenReturn(mockMediaRs);
        when(mockMediaRs.next()).thenReturn(false); // Simulate no borrowed media initially

        // Mock 3: loadFineBalance (SELECT total_fine FROM user_fines...)
        // **Using startsWith("SELECT total_fine") is safer:**
        PreparedStatement mockFinePstmt = mock(PreparedStatement.class);
        ResultSet mockFineRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT total_fine"))).thenReturn(mockFinePstmt);
        when(mockFinePstmt.executeQuery()).thenReturn(mockFineRs);
        when(mockFineRs.next()).thenReturn(true, false); // Fine balance found
        when(mockFineRs.getDouble("total_fine")).thenReturn(5.50); // Initial fine balance is 5.50

        // 3. Create the object under test. This triggers the mocked constructor logic.
        borrower = new Borrower(USERNAME, PASSWORD_HASH, SALT);

        // 4. Reset mocks to clear constructor's call count for cleaner subsequent tests
        // Reset only the general mocks that might interfere, or better, use specific mocks as above.
        reset(mockPstmt, mockRs, mockConnection);
    }

    @AfterEach
    void tearDown() {
        // 1. إغلاق MockedStatic لـ Borrower بأمان
        if (mockedStaticBorrower != null) {
            mockedStaticBorrower.close();
        }

        // 2. إغلاق MockedStatic لـ BorrowingService بأمان
        if (mockedStaticBorrowingService != null) {
            mockedStaticBorrowingService.close();
        }
        // ملاحظة: يمكنك أيضًا استخدام try-catch، لكن التحقق من null أبسط وأكثر ملاءمة هنا.
    }

    // -------------------------------------------------------------------------
    // 1. Testing Connection and Static Logic
    // -------------------------------------------------------------------------

    @Test
    void connect_Failure() {
        // Test static connect() failure path
        mockedStaticBorrower.reset();
        mockedStaticBorrower.when(Borrower::connect).thenReturn(null);
        assertNull(Borrower.connect());
    }

    // Note: The static initializer block (for CREATE TABLE) is very difficult to test with 100% coverage
    // without PowerMock or code refactoring, as it runs once when the class is loaded.
    // However, if we can trigger the class loading/initialization inside the static mock context, we can test it.
    // The provided setup implicitly covers the successful path (no exceptions).

    // -------------------------------------------------------------------------
    // 2. Testing getUserId Method
    // -------------------------------------------------------------------------

    @Test
    void getUserId_UserNotFound() throws SQLException {
        // Test path: User not found (ResultSet is empty)
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPstmt);
        when(mockPstmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        assertEquals(-1, borrower.getUserId());
    }

    // -------------------------------------------------------------------------
    // 3. Testing loadBorrowedMedia Method
    // -------------------------------------------------------------------------

    @Test
    void loadBorrowedMedia_LoadsBookAndCD_Successfully() throws SQLException {
        // Setup for getUserId inside loadBorrowedMedia (as it's called internally)
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockPstmt);
        when(mockPstmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false); // getUserId success
        when(mockRs.getInt("id")).thenReturn(USER_ID);

        // Setup for borrow_records query
        PreparedStatement mockBrPstmt = mock(PreparedStatement.class);
        ResultSet mockBrRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT br.*"))).thenReturn(mockBrPstmt);
        when(mockBrPstmt.executeQuery()).thenReturn(mockBrRs);

        // Simulate two borrowed items (Book and CD)
        when(mockBrRs.next()).thenReturn(true, true, false);

        // Book details
        when(mockBrRs.getInt("media_id")).thenReturn(101, 202);
        when(mockBrRs.getString("media_type")).thenReturn("book", "cd");
        when(mockBrRs.getString("media_title")).thenReturn("Book Title", "CD Title");
        when(mockBrRs.getString("due_date")).thenReturn(LocalDate.now().plusDays(5).toString(), LocalDate.now().plusDays(2).toString());
        when(mockBrRs.getInt("id")).thenReturn(1, 2); // record ID

        // Mock Book details query
        PreparedStatement mockBookPstmt = mock(PreparedStatement.class);
        ResultSet mockBookRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT b.author"))).thenReturn(mockBookPstmt);
        when(mockBookPstmt.executeQuery()).thenReturn(mockBookRs);
        when(mockBookRs.next()).thenReturn(true, false); // Book found
        when(mockBookRs.getString("author")).thenReturn("Auth");
        when(mockBookRs.getString("isbn")).thenReturn("123");

        // Mock CD details query
        PreparedStatement mockCdPstmt = mock(PreparedStatement.class);
        ResultSet mockCdRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT c.artist"))).thenReturn(mockCdPstmt);
        when(mockCdPstmt.executeQuery()).thenReturn(mockCdRs);
        when(mockCdRs.next()).thenReturn(true, false); // CD found
        when(mockCdRs.getString("artist")).thenReturn("Artist");
        when(mockCdRs.getString("genre")).thenReturn("Pop");
        when(mockCdRs.getInt("duration")).thenReturn(60);

        // Execute and Assert
        borrower.loadBorrowedMedia();

        assertEquals(2, borrower.getBorrowedMedia().size());
        assertEquals("Book Title", borrower.getBorrowedMedia().get(0).getMedia().getTitle());
        assertEquals("CD Title", borrower.getBorrowedMedia().get(1).getMedia().getTitle());
    }

    // -------------------------------------------------------------------------
    // 4. Testing loadFineBalance Method
    // -------------------------------------------------------------------------

    @Test
    void loadFineBalance_InitializeNewUserFine_InsertPath() throws SQLException {
        // Setup getUserId
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockPstmt);
        when(mockPstmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(USER_ID);

        // Mock: SELECT total_fine... returns no row (New User)
        PreparedStatement mockSelectPstmt = mock(PreparedStatement.class);
        ResultSet mockSelectRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT total_fine"))).thenReturn(mockSelectPstmt);
        when(mockSelectPstmt.executeQuery()).thenReturn(mockSelectRs);
        when(mockSelectRs.next()).thenReturn(false);

        // Mock: INSERT INTO user_fines...
        PreparedStatement mockInsertPstmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("INSERT INTO user_fines"))).thenReturn(mockInsertPstmt);

        // Execute
        borrower.loadFineBalance();

        // Assert: Fine should be 0.0 and INSERT was called
        assertEquals(0.0, borrower.getFineBalance());
        verify(mockInsertPstmt).executeUpdate();
    }

    @Test
    void loadFineBalance_InitializationInsert_SQLException() throws SQLException {
        // Test path: SELECT fails, but then INSERT fails with SQLException
        // Setup getUserId
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockPstmt);
        when(mockPstmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(USER_ID);

        // Mock: SELECT total_fine... returns no row (New User)
        PreparedStatement mockSelectPstmt = mock(PreparedStatement.class);
        ResultSet mockSelectRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT total_fine"))).thenReturn(mockSelectPstmt);
        when(mockSelectPstmt.executeQuery()).thenReturn(mockSelectRs);
        when(mockSelectRs.next()).thenReturn(false);

        // Mock: INSERT INTO user_fines... throws exception
        when(mockConnection.prepareStatement(startsWith("INSERT INTO user_fines"))).thenThrow(new SQLException("Insert Init Error"));

        // Execute
        borrower.loadFineBalance();

        // Assert: Fine is 0.0, as per the catch block logic
        assertEquals(0.0, borrower.getFineBalance());
    }

    // -------------------------------------------------------------------------
    // 5. Testing saveFineBalance Method
    // -------------------------------------------------------------------------

    // داخل BorrowerTest.java

    @Test
    void saveFineBalance_InsertNewIfUpdateFails() throws SQLException {
        // Setup: Fine balance to be saved
        borrower.setFineBalance(10.0);

        // -------------------------------------------------------------------------
        // 1. **إضافة تزييف getUserId** (يتم استدعاؤه داخل saveFineBalance لتحديد UserID)
        // -------------------------------------------------------------------------
        PreparedStatement mockUserPstmt = mock(PreparedStatement.class);
        ResultSet mockUserRs = mock(ResultSet.class);

        // قم بتزييف استعلام getUserId (SELECT id FROM users...)
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockUserPstmt);
        when(mockUserPstmt.executeQuery()).thenReturn(mockUserRs);
        when(mockUserRs.next()).thenReturn(true, false); // User found
        when(mockUserRs.getInt("id")).thenReturn(USER_ID);
        // -------------------------------------------------------------------------

        // Mock: UPDATE user_fines... returns 0 (row doesn't exist)
        PreparedStatement mockUpdatePstmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("UPDATE user_fines"))).thenReturn(mockUpdatePstmt);
        when(mockUpdatePstmt.executeUpdate()).thenReturn(0);

        // Mock: INSERT INTO user_fines...
        PreparedStatement mockInsertPstmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("INSERT INTO user_fines"))).thenReturn(mockInsertPstmt);
        when(mockInsertPstmt.executeUpdate()).thenReturn(1);

        // Execute
        borrower.saveFineBalance();

        // Assert
        verify(mockUpdatePstmt).executeUpdate();
        verify(mockInsertPstmt).executeUpdate();
        verify(mockInsertPstmt).setDouble(2, 10.0);

        // تأكد من أن getUserId تم استدعاؤه وتزييفه بشكل صحيح
        verify(mockUserPstmt).setString(1, borrower.getUsername());
    }
    // -------------------------------------------------------------------------
    // 6. Testing removeBorrowRecord Method
    // -------------------------------------------------------------------------

    // داخل BorrowerTest.java

    @Test
    void removeBorrowRecord_UpdatesAndSavesFine_Success() throws SQLException {
        // Setup: Add a record manually and set current fine balance
        Media mockMedia = new Book(303, "To be returned", "Auth", "ISBN", true);
        Borrower.MediaRecord record = borrower.new MediaRecord(mockMedia, LocalDate.now().minusDays(5), 50); // Record ID 50
        borrower.getBorrowedMedia().add(record);
        borrower.setFineBalance(5.0); // Initial fine

        double mediaFine = 2.50;

        // -------------------------------------------------------------------------
        // 1. **إضافة تزييف getUserId** (يتم استدعاؤه بشكل غير مباشر داخل saveFineBalance)
        // -------------------------------------------------------------------------
        PreparedStatement mockUserPstmt = mock(PreparedStatement.class);
        ResultSet mockUserRs = mock(ResultSet.class);

        // قم بتزييف استعلام getUserId (SELECT id FROM users...)
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockUserPstmt);
        when(mockUserPstmt.executeQuery()).thenReturn(mockUserRs);
        when(mockUserRs.next()).thenReturn(true, false); // User found
        when(mockUserRs.getInt("id")).thenReturn(USER_ID);
        // -------------------------------------------------------------------------

        // Mock: UPDATE borrow_records... returns 1 (success)
        PreparedStatement mockUpdatePstmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("UPDATE borrow_records"))).thenReturn(mockUpdatePstmt);
        when(mockUpdatePstmt.executeUpdate()).thenReturn(1);

        // Mock saveFineBalance (called internally): UPDATE user_fines...
        // ملاحظة: يتم استدعاء saveFineBalance بعد إزالة السجل
        PreparedStatement mockSavePstmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("UPDATE user_fines"))).thenReturn(mockSavePstmt);
        when(mockSavePstmt.executeUpdate()).thenReturn(1); // يجب أن يفترض أن التحديث نجح

        // Execute
        borrower.removeBorrowRecord(record, mediaFine);

        // Assert
        assertTrue(borrower.getBorrowedMedia().isEmpty());
        assertEquals(7.50, borrower.getFineBalance()); // 5.0 + 2.50

        verify(mockUpdatePstmt).setDouble(2, mediaFine);
        verify(mockSavePstmt).setDouble(1, 7.50);
        // تأكيد أن getUserId قد تم استدعاؤه بنجاح
        verify(mockUserPstmt, times(1)).executeQuery();    }
    // -------------------------------------------------------------------------
    // 7. Testing payFine Method
    // -------------------------------------------------------------------------

    @Test
    void payFine_Failure_InvalidAmount() throws SQLException {
        // Setup: Initial fine
        borrower.setFineBalance(10.0);

        // Test cases for invalid payment (amount <= 0 or amount > fineBalance)
        assertFalse(borrower.payFine(10.1));
        assertFalse(borrower.payFine(0.0));
        assertFalse(borrower.payFine(-5.0));

        // Assert: Fine is unchanged, saveFineBalance was never called
        assertEquals(10.0, borrower.getFineBalance());
        verify(mockConnection, never()).prepareStatement(startsWith("UPDATE user_fines"));
    }

    // -------------------------------------------------------------------------
    // 8. Testing generateOverdueReport Method
    // -------------------------------------------------------------------------

    @Test
    void generateOverdueReport_NoOverdueItems() {
        // Setup: The list is empty from the constructor mock
        // Execute and Assert (no exception and message for no overdue items)
        borrower.generateOverdueReport();
    }

    @Test
    void generateOverdueReport_WithOverdueItems() {
        // Setup: Add overdue items (relying on correct fine calculation from Mocked BorrowingService)
        Media book = new Book(1, "Overdue Book", "A", "I", true);
        Media cd = new CD(2, "Overdue CD", "B", "G", 50, true);

        // 10 days overdue for book (Fine: 10 * 0.5 = 5.0)
        borrower.getBorrowedMedia().add(borrower.new MediaRecord(book, LocalDate.now().minusDays(10), 1));
        // 5 days overdue for CD (Fine: 5 * 1.0 = 5.0)
        borrower.getBorrowedMedia().add(borrower.new MediaRecord(cd, LocalDate.now().minusDays(5), 2));

        // Execute (The test verifies the internal logic runs without error)
        borrower.generateOverdueReport();
    }

    // -------------------------------------------------------------------------
    // 9. Testing getUsersWithOverdueBooks (Static)
    // -------------------------------------------------------------------------

    // داخل BorrowerTest.java

// داخل BorrowerTest.java

    // داخل BorrowerTest.java

//    @Test
//    void getUsersWithOverdueBooks_NoOverdueUsers() throws SQLException {
//        // 1. **إنشاء مُحاكيات مخصصة** لهذا الاختبار (كما في الحل السابق)
//        PreparedStatement localMockPstmt = mock(PreparedStatement.class);
//        ResultSet localMockRs = mock(ResultSet.class);
//
//        // 2. تحديد الاستعلام
//        String sql = "SELECT u.id, u.username, COUNT(br.id) as overdue_count FROM users u JOIN borrow_records br ON u.id = br.user_id WHERE br.returned = 0 AND br.due_date < date('now') GROUP BY u.id, u.username";
//
//        // 3. **تغليف الكود باستخدام MockedStatic محلي ومضمون الإغلاق**
//        // هذا يضمن أن التزييف الثابت يتم تفعيله وإلغاؤه داخل نطاق هذا الاختبار فقط.
//        try (MockedStatic<Borrower> tempMockedBorrower = Mockito.mockStatic(Borrower.class, CALLS_REAL_METHODS)) {
//
//            // تزييف الاتصال الثابت: عند استدعاء Borrower.connect()، ارجع mockConnection
//            tempMockedBorrower.when(Borrower::connect).thenReturn(mockConnection);
//
//            // 4. تزييف استدعاء prepareStatement ليرجع المُحاكي المحلي
//            when(mockConnection.prepareStatement(eq(sql))).thenReturn(localMockPstmt);
//
//            // 5. تزييف سلوك المُحاكي المحلي
//            when(localMockPstmt.executeQuery()).thenReturn(localMockRs);
//            when(localMockRs.next()).thenReturn(false); // لا يوجد مستخدمون متأخرون
//
//            // 6. Execute (الدالة الثابتة)
//            List<Borrower.UserWithOverdueBooks> list = Borrower.getUsersWithOverdueBooks();
//
//            // 7. Assert and Verify
//            assertTrue(list.isEmpty());
//
//            // 8. التحقق من الاستدعاء
//            verify(localMockPstmt).executeQuery();
//        }
//        // ملاحظة: يتم إغلاق tempMockedBorrower تلقائياً عند الخروج من كتلة try-with-resources
//    }

    @Test
    void getUsersWithOverdueBooks_HandlesSQLException() throws SQLException {
        // 1. تحديد الاستعلام
        String sql = "SELECT u.id, u.username, COUNT(br.id) as overdue_count FROM users u JOIN borrow_records br ON u.id = br.user_id WHERE br.returned = 0 AND br.due_date < date('now') GROUP BY u.id, u.username";

        // 2. **إعادة تزييف الاتصال الثابت (احتياطاً)**
        mockedStaticBorrower.when(Borrower::connect).thenReturn(mockConnection);

        // 3. تزييف prepareStatement لرمي الاستثناء
        when(mockConnection.prepareStatement(eq(sql))).thenThrow(new SQLException("Query Error"));

        // 4. Execute
        List<Borrower.UserWithOverdueBooks> list = Borrower.getUsersWithOverdueBooks();

        // 5. Assert
        assertTrue(list.isEmpty());
    }
    // -------------------------------------------------------------------------
    // 10. Testing Connection Failure for All Methods
    // -------------------------------------------------------------------------

    @Test
    void allMethods_Handle_ConnectionNull() throws SQLException {
        // Setup: Simulate connection failure by changing the static mock behavior
        mockedStaticBorrower.when(Borrower::connect).thenReturn(null);

        // getUserId (returns -1)
        assertEquals(-1, borrower.getUserId());

        // loadBorrowedMedia (returns)
        borrower.loadBorrowedMedia();

        // loadFineBalance (returns)
        borrower.loadFineBalance();

        // saveFineBalance (returns)
        borrower.saveFineBalance();

        // addBorrowRecord (returns)
        Media mockMedia = new Book(1, "Test", "A", "I", true);
        borrower.addBorrowRecord(mockMedia, LocalDate.now().plusDays(5));

        // removeBorrowRecord (returns)
        Borrower.MediaRecord record = borrower.new MediaRecord(mockMedia, LocalDate.now(), 1);
        borrower.getBorrowedMedia().add(record);
        borrower.removeBorrowRecord(record, 1.0);

        // getUsersWithOverdueBooks (static - returns empty list)
        assertTrue(Borrower.getUsersWithOverdueBooks().isEmpty());
    }
    ////////////////
    @Test
    void addBorrowRecord_Success() throws SQLException {
        // 1. Setup Data
        Media book = new Book(2, "The Mock Book", "Author A", "ISBN-123", true);
        LocalDate dueDate = LocalDate.now().plusWeeks(2);
        final int GENERATED_ID = 99;

        // 2. Mock getUserId (Must succeed for the code to proceed)
        PreparedStatement mockUserPstmt = mock(PreparedStatement.class);
        ResultSet mockUserRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockUserPstmt);
        when(mockUserPstmt.executeQuery()).thenReturn(mockUserRs);
        when(mockUserRs.next()).thenReturn(true, false);
        when(mockUserRs.getInt("id")).thenReturn(USER_ID);

        // 3. Mock PreparedStatement execution
        PreparedStatement mockPstmt = mock(PreparedStatement.class);
        // تزييف prepareStatement لاستعلام INSERT ليرجع mockPstmt
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPstmt);

        // 4. Mock Generated Keys (النتيجة المتوقعة)
        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockPstmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        // تزييف: generatedKeys.next() يجب أن يكون true ثم false
        when(mockGeneratedKeys.next()).thenReturn(true, false);
        when(mockGeneratedKeys.getInt(1)).thenReturn(GENERATED_ID);

        // 5. Execute
        borrower.addBorrowRecord(book, dueDate);

        // 6. Verification & Assertions

        // التحقق من إعداد المعاملات (Parameters) على PreparedStatement
        verify(mockPstmt).setInt(1, USER_ID);
        verify(mockPstmt).setInt(2, book.getId());
        verify(mockPstmt).setString(3, book.getMediaType());
        verify(mockPstmt).setString(4, book.getTitle());
        // التحقق من استدعاء التحديث
        verify(mockPstmt).executeUpdate();

        // التحقق من أن السجل تمت إضافته إلى قائمة borrowedMedia
        assertEquals(1, borrower.getBorrowedMedia().size());
        // التحقق من تعيين الـ ID المُنشأ
        assertEquals(GENERATED_ID, borrower.getBorrowedMedia().get(0).getRecordId());
    }




    // داخل BorrowerTest.java

    // داخل BorrowerTest.java

    // يجب حذف أي إعداد لـ MockedStatic<Borrower> من setUp و tearDown لتجنب التضارب
//    @Test
//    void getUsersWithOverdueBooks_LoadsData() throws SQLException {
//        // 1. إنشاء مُحاكيات مخصصة
//        PreparedStatement localMockPstmt = mock(PreparedStatement.class);
//        ResultSet localMockRs = mock(ResultSet.class);
//
//        // البيانات
//        final int USER_ID_1 = 101; final String USERNAME_1 = "late_user_A"; final int COUNT_1 = 5;
//        final int USER_ID_2 = 102; final String USERNAME_2 = "late_user_B"; final int COUNT_2 = 2;
//
//        // 2. تزييف سلوك ResultSet
//        when(localMockRs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
//        when(localMockRs.getInt("id")).thenReturn(USER_ID_1).thenReturn(USER_ID_2);
//        when(localMockRs.getString("username")).thenReturn(USERNAME_1).thenReturn(USERNAME_2);
//        when(localMockRs.getInt("overdue_count")).thenReturn(COUNT_1).thenReturn(COUNT_2);
//
//        // 3. تحديد الاستعلام
//        String sql = "SELECT u.id, u.username, COUNT(br.id) as overdue_count FROM users u JOIN borrow_records br ON u.id = br.user_id WHERE br.returned = 0 AND br.due_date < date('now') GROUP BY u.id, u.username";
//
//        // 4. العزل: استخدام try-with-resources لفتح وإغلاق التزييف الثابت
//        try (MockedStatic<Borrower> tempMockedBorrower = Mockito.mockStatic(Borrower.class, CALLS_REAL_METHODS)) {
//
//            // تزييف الاتصال الثابت
//            tempMockedBorrower.when(Borrower::connect).thenReturn(mockConnection);
//
//            // ربط الاستعلام بالمحاكي المحلي
//            when(mockConnection.prepareStatement(eq(sql))).thenReturn(localMockPstmt);
//            when(localMockPstmt.executeQuery()).thenReturn(localMockRs);
//
//            // 5. Execute
//            List<Borrower.UserWithOverdueBooks> list = Borrower.getUsersWithOverdueBooks();
//
//            // 6. Assertions & Verification
//            assertEquals(2, list.size()); // 👈 يجب أن ينجح الآن
//            assertEquals(USERNAME_1, list.get(0).getUsername());
//
//            verify(localMockPstmt).executeQuery();
//        } // 👈 يتم الإغلاق تلقائياً هنا
//    }
//
//
//
//// يجب حذف أي إعداد لـ MockedStatic<Borrower> من setUp و tearDown لتجنب التضارب
//    @Test
//    void getUsersWithOverdueBooks_DataReadFailure() throws SQLException {
//        // 1. إنشاء مُحاكيات مخصصة
//        PreparedStatement localMockPstmt = mock(PreparedStatement.class);
//        ResultSet localMockRs = mock(ResultSet.class);
//
//        // 2. تزييف سلوك ResultSet
//        when(localMockRs.next()).thenReturn(true, false);
//        when(localMockRs.getInt("id")).thenThrow(new SQLException("Simulated read error")); // 👈 نقطة الفشل
//
//        // 3. تحديد الاستعلام
//        String sql = "SELECT u.id, u.username, COUNT(br.id) as overdue_count FROM users u JOIN borrow_records br ON u.id = br.user_id WHERE br.returned = 0 AND br.due_date < date('now') GROUP BY u.id, u.username";
//
//        // 4. العزل: استخدام try-with-resources لفتح وإغلاق التزييف الثابت
//        try (MockedStatic<Borrower> tempMockedBorrower = Mockito.mockStatic(Borrower.class, CALLS_REAL_METHODS)) {
//
//            // تزييف الاتصال الثابت
//            tempMockedBorrower.when(Borrower::connect).thenReturn(mockConnection);
//
//            // ربط الاستعلام بالمحاكي المحلي
//            when(mockConnection.prepareStatement(eq(sql))).thenReturn(localMockPstmt);
//            when(localMockPstmt.executeQuery()).thenReturn(localMockRs);
//
//            // 5. Execute
//            List<Borrower.UserWithOverdueBooks> list = Borrower.getUsersWithOverdueBooks();
//
//            // 6. Assertions & Verification
//            assertTrue(list.isEmpty()); // 👈 بسبب الاستثناء في حلقة while
//
//            verify(localMockPstmt).executeQuery(); // 👈 يجب أن يتم التحقق بنجاح الآن
//        } // 👈 يتم الإغلاق تلقائياً هنا
//    }
}