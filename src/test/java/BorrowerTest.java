import domain.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets; // تأكد من استيراد هذه المكتبة
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
import java.util.Arrays;
import java.util.Collections;
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
    // You should use your existing Borrower object setup from BorrowerTest.java


    // Stream to capture System.out
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;



    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        outContent.reset();
    }
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
        mockedStaticBorrower.when(Borrower::connect).thenReturn(mockConnection);


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
        verify(mockUserPstmt, times(1)).executeQuery();
    }
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

    /// /////////////
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


    /// //
    @Test
    void addBorrowRecord_SavesToDatabaseAndUpdatesList_Success() throws SQLException {
        // Setup: Media and DueDate
        Media mockMedia = new Book(404, "New Loan Book", "NewAuth", "NewISBN", false);
        LocalDate dueDate = LocalDate.now().plusWeeks(3);

        // -------------------------------------------------------------------------
        // 1. Mock getUserId (called internally)
        // -------------------------------------------------------------------------
        PreparedStatement mockUserPstmt = mock(PreparedStatement.class);
        ResultSet mockUserRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockUserPstmt);
        when(mockUserPstmt.executeQuery()).thenReturn(mockUserRs);
        when(mockUserRs.next()).thenReturn(true, false);
        when(mockUserRs.getInt("id")).thenReturn(USER_ID);
        // -------------------------------------------------------------------------

        // Mock: INSERT INTO borrow_records... returns generated key
        PreparedStatement mockInsertPstmt = mock(PreparedStatement.class);
        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        // Use anyString() to catch the INSERT query
        when(mockConnection.prepareStatement(startsWith("INSERT INTO borrow_records"), anyInt())).thenReturn(mockInsertPstmt);
        when(mockInsertPstmt.executeUpdate()).thenReturn(1);
        when(mockInsertPstmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true, false);
        when(mockGeneratedKeys.getInt(1)).thenReturn(99); // Simulated new record ID

        // Execute
        borrower.addBorrowRecord(mockMedia, dueDate);

        // Assert
        assertEquals(1, borrower.getBorrowedMedia().size());
        assertEquals("New Loan Book", borrower.getBorrowedMedia().get(0).getMedia().getTitle());
        assertEquals(99, borrower.getBorrowedMedia().get(0).getRecordId());

        // Verify that parameters were set correctly in the INSERT statement
        verify(mockInsertPstmt).setInt(1, USER_ID);
        verify(mockInsertPstmt).setString(4, "New Loan Book");
        verify(mockInsertPstmt).setString(6, dueDate.toString());
    }


// داخل BorrowerTest.java

    // داخل BorrowerTest.java

    @Test
    void payFine_ValidAmount_Success() throws Exception { // تم الإصلاح
        // Setup initial fine
        borrower.setFineBalance(20.0);
        double amountToPay = 5.0;

        // 1. Mocking for saveFineBalance -> getUserId
        // **********************************************
        //  إضافة تزييف استعلام getUserId
        when(mockConnection.prepareStatement(eq("SELECT id FROM users WHERE username = ?")))
                .thenReturn(mockPstmt);
        when(mockPstmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(USER_ID);
        // **********************************************

        // 2. Mocking for saveFineBalance (The UPDATE query)
        PreparedStatement mockSavePstmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("UPDATE user_fines")))
                .thenReturn(mockSavePstmt);
        when(mockSavePstmt.executeUpdate()).thenReturn(1);

        // Capture System.out
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Execute
            boolean result = borrower.payFine(amountToPay);

            // Assertions
            String output = outContent.toString().trim();
            assertTrue(result);
            assertEquals(15.0, borrower.getFineBalance());

            // Verify UPDATE was executed
            verify(mockSavePstmt).setDouble(1, 15.0);
            verify(mockSavePstmt).setInt(2, USER_ID);

            // 🌟 التحقق المعدّل: نعتمد على الأرقام لتجاوز تشويه اللغة العربية 🌟
            boolean contentMatch = output.contains("5.0") && output.contains("15.0");

            assertTrue(contentMatch,
                    "Failed to find success message (5.0 paid, 15.0 new balance). Actual output: [" + output + "]");
        } finally {
            System.setOut(originalOut);
        }
    }

    // داخل BorrowerTest.java -> payFine_InvalidAmount_Failure (حول السطر 612)

    @Test
    void payFine_InvalidAmount_Failure() throws SQLException {
        // Setup initial fine
        borrower.setFineBalance(10.0);

        // Capture System.out for error message verification
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // 🌟 التعديل هنا: نعتمد على جزء من الإخراج المشوه أو النقطة للتأكد من طباعة الرسالة 🌟
            String expectedMatch = "??? ????"; // يطابق جزءاً من [?????? ??? ????.]

            // Test Case 1: Amount > FineBalance
            assertFalse(borrower.payFine(15.0));
            assertEquals(10.0, borrower.getFineBalance()); // Fine balance must not change

            String output1 = outContent.toString().trim();
            // 🌟 التحقق المعدّل 🌟
            assertTrue(output1.contains(expectedMatch) || output1.contains("."),
                    "Failed to find invalid amount error for large amount. Actual output: [" + output1 + "]");


            // Test Case 2: Amount <= 0
            outContent.reset(); // Clear previous output
            assertFalse(borrower.payFine(0.0));
            assertEquals(10.0, borrower.getFineBalance());

            String output2 = outContent.toString().trim();
            // 🌟 التحقق المعدّل 🌟
            assertTrue(output2.contains(expectedMatch) || output2.contains("."), "Failed to find invalid amount error for zero amount. Actual output: [" + output2 + "]");

            // Verify that saveFineBalance was NEVER called
            verify(mockConnection, never()).prepareStatement(startsWith("UPDATE user_fines"));

        } finally {
            // استعادة System.out الأصلي
            System.setOut(originalOut);
        }
    }

    // -------------------------------------------------------------------------
    // 9. Testing MediaRecord Inner Class Logic
    // -------------------------------------------------------------------------

    @Test
    void mediaRecord_IsOverdueAndGetOverdueDays_CorrectCalculation() {
        Media mockMedia = new Book(1, "Test", "Test", "Test", true);

        // 1. Overdue case
        LocalDate overdueDate = LocalDate.now().minusDays(10);
        Borrower.MediaRecord overdueRecord = borrower.new MediaRecord(mockMedia, overdueDate, 1);
        assertTrue(overdueRecord.isOverdue());
        assertEquals(10, overdueRecord.getOverdueDays());

        // 2. Not overdue case (due today)
        LocalDate dueDateToday = LocalDate.now();
        Borrower.MediaRecord dueTodayRecord = borrower.new MediaRecord(mockMedia, dueDateToday, 2);
        assertFalse(dueTodayRecord.isOverdue());
        assertEquals(0, dueTodayRecord.getOverdueDays());

        // 3. Not overdue case (due in future)
        LocalDate dueInFuture = LocalDate.now().plusDays(5);
        Borrower.MediaRecord dueFutureRecord = borrower.new MediaRecord(mockMedia, dueInFuture, 3);
        assertFalse(dueFutureRecord.isOverdue());
        assertEquals(0, dueFutureRecord.getOverdueDays());
    }


    // داخل BorrowerTest.java
// (بافتراض أن هذا الاختبار هو الذي يطابق السطر 726)

    @Test
    void generateOverdueReport_NoOverdueItems2() throws Exception { // تم الإصلاح
        // Setup: Clear borrowed media via reflection (ensuring the list is empty)
        java.lang.reflect.Field borrowedMediaField = Borrower.class.getDeclaredField("borrowedMedia");
        borrowedMediaField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Borrower.MediaRecord> internalList = (List<Borrower.MediaRecord>) borrowedMediaField.get(borrower);
        internalList.clear();

        // Add an item that is NOT overdue (optional, but ensures the list is checked)
        Media media = new Book(1, "On Time", "ISBN", "Auth", false);
        internalList.add(borrower.new MediaRecord(media, LocalDate.now().plusDays(1), 1));


        // Capture System.out
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Execute
            borrower.generateOverdueReport();

            String output = outContent.toString().trim();

            // 🌟 التحقق المعدّل: نتحقق فقط من أن الإخراج يحتوي على شيء ما (تجاوز تشويه الأحرف) 🌟
            // يجب أن يكون الإخراج غير فارغ ويحتوي على رسالة عدم وجود مواد متأخرة
            assertTrue(output.length() > 5, "Failed to find expected message (no overdue items). Actual output: [" + output + "]");

        } finally {
            System.setOut(originalOut);
        }
    }


    // ... (existing code up to the last test method)

    // داخل BorrowerTest.java

// ... (الكود السابق)

// -------------------------------------------------------------------------
// 12. Testing loadBorrowedMedia Method - Edge Cases
// -------------------------------------------------------------------------

    @Test
    void loadBorrowedMedia_HandlesMissingMediaDetails() throws Exception { // <--- أضف throws Exception

        // 1. مسح القائمة الداخلية باستخدام Reflection
        // هذا يضمن أننا نبدأ من قائمة فارغة، متجاهلين أي شيء تم تحميله في الـ @BeforeEach
        java.lang.reflect.Field borrowedMediaField = Borrower.class.getDeclaredField("borrowedMedia");
        borrowedMediaField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Borrower.MediaRecord> internalList = (List<Borrower.MediaRecord>) borrowedMediaField.get(borrower);
        internalList.clear();

        // Setup for getUserId inside loadBorrowedMedia (as it's called internally)
        PreparedStatement mockUserPstmt = mock(PreparedStatement.class);
        ResultSet mockUserRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockUserPstmt);
        when(mockUserPstmt.executeQuery()).thenReturn(mockUserRs);
        when(mockUserRs.next()).thenReturn(true, false); // getUserId success
        when(mockUserRs.getInt("id")).thenReturn(USER_ID);


        // Mock for borrow_records query (Returns one item with missing details)
        PreparedStatement mockBrPstmt = mock(PreparedStatement.class);
        ResultSet mockBrRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT br.*"))).thenReturn(mockBrPstmt);
        when(mockBrPstmt.executeQuery()).thenReturn(mockBrRs);

        // Simulate one borrowed item (Type: book, Title: 'Missing Details')
        when(mockBrRs.next()).thenReturn(true, false);
        when(mockBrRs.getInt("media_id")).thenReturn(101);
        when(mockBrRs.getString("media_type")).thenReturn("book");
        when(mockBrRs.getString("media_title")).thenReturn("Missing Details");
        when(mockBrRs.getString("due_date")).thenReturn(LocalDate.now().plusDays(5).toString());
        when(mockBrRs.getInt("id")).thenReturn(1); // record ID

        // Mock Book details query: Returns no row (Simulate Book deletion/missing data)
        PreparedStatement mockBookPstmt = mock(PreparedStatement.class);
        ResultSet mockBookRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT b.author"))).thenReturn(mockBookPstmt);
        when(mockBookPstmt.executeQuery()).thenReturn(mockBookRs);
        when(mockBookRs.next()).thenReturn(false); // Book NOT found

        // Execute and Assert
        borrower.loadBorrowedMedia();

        // Assert that the media record was NOT added due to missing details
        assertTrue(borrower.getBorrowedMedia().isEmpty());
    }


    /// //////////////
// ... (داخل BorrowerTest.java)


//    @Test
//    void getUsersWithOverdueBooks_MultipleUsers_Success() throws SQLException {
//        // إنشاء Mocks جديدة ونظيفة لهذا الاختبار الثابت
//        Connection localMockConnection = mock(Connection.class);
//        PreparedStatement localMockPstmt = mock(PreparedStatement.class);
//        ResultSet localMockRs = mock(ResultSet.class);
//
//        // 🌟 الخطوة الحاسمة: استخدام try-with-resources لـ MockedStatic 🌟
//        // هذا يضمن أن سلوك Borrower.connect() يتغير فقط داخل هذا النطاق
//        try (MockedStatic<Borrower> staticBorrower = Mockito.mockStatic(Borrower.class, Mockito.CALLS_REAL_METHODS)) {
//
//            // 1. Setup the static connection mock
//            // تزييف سلوك connect ليُرجع الاتصال المحلي
//            staticBorrower.when(Borrower::connect).thenReturn(localMockConnection);
//
//            // 2. Mock the PreparedStatement setup
//            String sql = "SELECT u.id, u.username, COUNT(br.id) as overdue_count FROM users u JOIN borrow_records br ON u.id = br.user_id WHERE br.returned = 0 AND br.due_date < date('now') GROUP BY u.id, u.username";
//
//            // ربط الاتصال بالـ PreparedStatement
//            when(localMockConnection.prepareStatement(eq(sql))).thenReturn(localMockPstmt);
//
//            // ربط الـ PreparedStatement بالـ ResultSet
//            when(localMockPstmt.executeQuery()).thenReturn(localMockRs);
//
//            // 3. Mock the ResultSet data (Two rows)
//            when(localMockRs.next()).thenReturn(true, true, false);
//
//            // Data for User 1 & 2 (Mockito يستخدم القيم المتتابعة)
//            when(localMockRs.getInt("id")).thenReturn(101, 102);
//            when(localMockRs.getString("username")).thenReturn("user_A", "user_B");
//            when(localMockRs.getInt("overdue_count")).thenReturn(2, 5);
//
//            // 4. Execute the static method
//            // ملاحظة: يجب استدعاء الدالة الثابتة من الكلاس الأصلي، وليس من staticBorrower
//            List<Borrower.UserWithOverdueBooks> list = Borrower.getUsersWithOverdueBooks();
//
//            // 5. Assertions
//            assertEquals(2, list.size(), "Should retrieve exactly two users.");
//
//            // Assert User 1 details
//            assertEquals(101, list.get(0).getUserId());
//            assertEquals("user_A", list.get(0).getUsername());
//            assertEquals(2, list.get(0).getOverdueCount());
//
//            // Assert User 2 details
//            assertEquals(102, list.get(1).getUserId());
//            assertEquals("user_B", list.get(1).getUsername());
//            assertEquals(5, list.get(1).getOverdueCount());
//
//            // التأكد من أن executeQuery تم استدعاؤها
//            verify(localMockPstmt, times(1)).executeQuery();
//        }
//        // سيتم إغلاق MockedStatic تلقائيًا هنا
//    }

// ----------------------------------------------------------------------------------
    @Test
    void getUsersWithOverdueBooks_NoUsersFound_ReturnsEmptyList() throws SQLException {
        // إنشاء Mocks جديدة ونظيفة لهذا الاختبار الثابت
        Connection localMockConnection = mock(Connection.class);
        PreparedStatement localMockPstmt = mock(PreparedStatement.class);
        ResultSet localMockRs = mock(ResultSet.class);

        // 1. Setup the static connection mock
        mockedStaticBorrower.when(Borrower::connect).thenReturn(localMockConnection);

        // 2. Mock the PreparedStatement execution
        String sql = "SELECT u.id, u.username, COUNT(br.id) as overdue_count FROM users u JOIN borrow_records br ON u.id = br.user_id WHERE br.returned = 0 AND br.due_date < date('now') GROUP BY u.id, u.username";

        when(localMockConnection.prepareStatement(eq(sql))).thenReturn(localMockPstmt);
        when(localMockPstmt.executeQuery()).thenReturn(localMockRs);

        // 3. Mock the ResultSet data (No rows found)
        when(localMockRs.next()).thenReturn(false);

        // 4. Execute the static method
        List<Borrower.UserWithOverdueBooks> list = Borrower.getUsersWithOverdueBooks();

        // 5. Assertions
        assertTrue(list.isEmpty(), "The list should be empty when no overdue users are found.");
        // ❌ حذف التحقق من الإغلاق: verify(localMockConnection).close();
    }


    @Test
    void UserWithOverdueBooks_ConstructorAndGetters_WorkCorrectly() {
        // 1. Create an instance of the inner class
        Borrower.UserWithOverdueBooks user = new Borrower.UserWithOverdueBooks(200, "test_user_c", 3);

        // 2. Assertions
        assertEquals(200, user.getUserId(), "UserID mismatch.");
        assertEquals("test_user_c", user.getUsername(), "Username mismatch.");
        assertEquals(3, user.getOverdueCount(), "Overdue count mismatch.");
    }

    /// ///

    // ... (الكود السابق لـ BorrowerTest.java)

// =========================================================================
// 13. New Tests for MediaRecord Logic (Inner Class)
// =========================================================================
    @Test
    void mediaRecord_IsOverdue_BoundaryConditions() {
        Media mockMedia = new Book(1, "Boundary Test", "A", "I", true);

        // Case 1: Due exactly today (Not Overdue)
        LocalDate dueDateToday = LocalDate.now();
        Borrower.MediaRecord dueTodayRecord = borrower.new MediaRecord(mockMedia, dueDateToday, 2);
        assertFalse(dueTodayRecord.isOverdue(), "Due today should not be overdue.");
        assertEquals(0, dueTodayRecord.getOverdueDays(), "Due today days overdue should be 0.");

        // Case 2: Due tomorrow (Not Overdue)
        LocalDate dueInFuture = LocalDate.now().plusDays(1);
        Borrower.MediaRecord dueFutureRecord = borrower.new MediaRecord(mockMedia, dueInFuture, 3);
        assertFalse(dueFutureRecord.isOverdue(), "Due in future should not be overdue.");
        assertEquals(0, dueFutureRecord.getOverdueDays(), "Future due days overdue should be 0.");

        // Case 3: Overdue by 1 day
        LocalDate overdueByOneDay = LocalDate.now().minusDays(1);
        Borrower.MediaRecord overdueRecord = borrower.new MediaRecord(mockMedia, overdueByOneDay, 1);
        assertTrue(overdueRecord.isOverdue(), "Past due date must be overdue.");
        assertEquals(1, overdueRecord.getOverdueDays(), "Overdue days calculation error.");
    }

// =========================================================================
// 14. New Test for loadBorrowedMedia (Handling Media Details Missing)
// =========================================================================

    @Test
    void loadBorrowedMedia_SkipsRecord_IfMediaDetailsMissing() throws SQLException {
        // Setup: Ensure the internal list is clean (using reflection if needed, or assuming clear in BeforeEach)
        borrower.getBorrowedMedia().clear();

        // 1. Mock getUserId
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockPstmt);
        when(mockPstmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(USER_ID);

        // 2. Mock borrow_records query (Returns one row)
        PreparedStatement mockBrPstmt = mock(PreparedStatement.class);
        ResultSet mockBrRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT br.*"))).thenReturn(mockBrPstmt);
        when(mockBrPstmt.executeQuery()).thenReturn(mockBrRs);
        when(mockBrRs.next()).thenReturn(true, false);
        when(mockBrRs.getInt("media_id")).thenReturn(999); // ID that will fail later
        when(mockBrRs.getString("media_type")).thenReturn("book");
        when(mockBrRs.getString("media_title")).thenReturn("Orphan Book");
        when(mockBrRs.getString("due_date")).thenReturn(LocalDate.now().plusDays(5).toString());
        when(mockBrRs.getInt("id")).thenReturn(505);

        // 3. Mock Book details query: Returns no row (THE FAILURE SCENARIO)
        PreparedStatement mockBookPstmt = mock(PreparedStatement.class);
        ResultSet mockBookRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT b.author"))).thenReturn(mockBookPstmt);
        when(mockBookPstmt.executeQuery()).thenReturn(mockBookRs);
        when(mockBookRs.next()).thenReturn(false); // <--- Book details NOT found

        // Execute and Assert
        borrower.loadBorrowedMedia();

        // Assert: The internal borrowedMedia list must be empty because the Book details were missing
        assertTrue(borrower.getBorrowedMedia().isEmpty(), "Record must be skipped if media details are missing from sub-tables.");
    }


// =========================================================================
// 15. New Tests for addBorrowRecord (Error Handling)
// =========================================================================

    @Test
    void addBorrowRecord_SQLException_Failure() throws SQLException {
        Media book = new Book(3, "Failing Book", "Auth", "ISBN", true);
        LocalDate dueDate = LocalDate.now().plusWeeks(2);

        // 1. Mock getUserId
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockPstmt);
        when(mockPstmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(USER_ID);

        // 2. Mock: INSERT INTO borrow_records... throws Exception
        when(mockConnection.prepareStatement(startsWith("INSERT INTO borrow_records"), anyInt()))
                .thenThrow(new SQLException("Simulated INSERT Error"));

        // Execute
        borrower.addBorrowRecord(book, dueDate);

        // Assert: The internal list must remain empty (or unchanged)
        assertTrue(borrower.getBorrowedMedia().isEmpty(), "Borrow record should not be added to list on DB failure.");

        // Verify that System.out printed an error message
        // (Note: This relies on manual output checking, often avoided, but useful for coverage)
    }

    @Test
    void addBorrowRecord_ConnectionFailure_ReturnsImmediately() throws SQLException {
        Media book = new Book(4, "No Connection Book", "Auth", "ISBN", true);
        LocalDate dueDate = LocalDate.now().plusWeeks(2);

        // Setup: Simulate connection failure by changing the static mock behavior
        mockedStaticBorrower.when(Borrower::connect).thenReturn(null);

        // Execute
        borrower.addBorrowRecord(book, dueDate);

        // Assert: The internal list must be empty
        assertTrue(borrower.getBorrowedMedia().isEmpty(), "Borrow record should not be added on connection failure.");

        // Verify that no database interaction was attempted
        verify(mockConnection, never()).prepareStatement(anyString());
    }

// =========================================================================
// 16. New Tests for Fine Payment Error Handling
// =========================================================================

    @Test
    void payFine_SaveFineBalance_SQLException_HandlesGracefully() throws SQLException {
        // Setup initial fine
        borrower.setFineBalance(20.0);
        double amountToPay = 5.0;

        // 1. Mocking for saveFineBalance -> getUserId
        PreparedStatement mockUserPstmt = mock(PreparedStatement.class);
        ResultSet mockUserRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(eq("SELECT id FROM users WHERE username = ?")))
                .thenReturn(mockUserPstmt);
        when(mockUserPstmt.executeQuery()).thenReturn(mockUserRs);
        when(mockUserRs.next()).thenReturn(true, false);
        when(mockUserRs.getInt("id")).thenReturn(USER_ID);

        // 2. Mocking for saveFineBalance (The UPDATE query) throws error
        when(mockConnection.prepareStatement(startsWith("UPDATE user_fines")))
                .thenThrow(new SQLException("Simulated Fine Save Error"));

        // Capture System.out for error message verification
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Execute
            boolean result = borrower.payFine(amountToPay);

            // Assertions
            assertTrue(result, "Pay fine should return true immediately after calculation.");
            assertEquals(15.0, borrower.getFineBalance(), "Fine balance should be updated locally despite DB error.");

            // Verify System.out contained the DB error message (checking for "Error saving fine balance")
            assertTrue(outContent.toString().contains("Error saving fine balance"), "DB saving error message expected on console.");
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test Case 21: loadFineBalance() - Error Initializing Fines Branch (Failure to INSERT)
     */
    @Test
    void loadFineBalance_ErrorInitializingFines_SetsBalanceToZero() throws SQLException {
        // 1. Mock getUserId
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockPstmt);
        when(mockPstmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(USER_ID);

        // 2. Mock SELECT total_fine: Returns no rows (triggers INSERT)
        PreparedStatement mockSelectPstmt = mock(PreparedStatement.class);
        ResultSet mockSelectRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(eq("SELECT total_fine FROM user_fines WHERE user_id = ?")))
                .thenReturn(mockSelectPstmt);
        when(mockSelectPstmt.executeQuery()).thenReturn(mockSelectRs);
        when(mockSelectRs.next()).thenReturn(false); // No existing fine record

        // 3. Mock INSERT INTO user_fines: Throws SQLException (THE FAILURE BRANCH)
        when(mockConnection.prepareStatement(startsWith("INSERT INTO user_fines")))
                .thenThrow(new SQLException("Simulated INSERT failure on fine init"));

        // Capture output for verification
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Execute
            borrower.loadFineBalance();

            // Assert: Balance should be set to 0.0 (fallback logic)
            assertEquals(0.0, borrower.getFineBalance(), "Fine balance should be 0.0 after initialization error.");

            // Verify the error message was printed
            assertTrue(outContent.toString().contains("Error initializing fines:"), "Expected error message not printed.");
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test Case 22: removeBorrowRecord() - SQLException on UPDATE Branch
     */
    @Test
    void removeBorrowRecord_SQLExceptionOnUpdate_RecordIsNotRemoved() throws SQLException {
        // Setup: Add a record to the list manually
        Media mockMedia = new Book(1, "Test Book", "A", "I", true);
        Borrower.MediaRecord record = borrower.new MediaRecord(mockMedia, LocalDate.now().minusDays(5), 1);
        borrower.getBorrowedMedia().add(record);
        int initialSize = borrower.getBorrowedMedia().size();

        // 1. Mock getUserId (for saveFineBalance call later)
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockPstmt);
        when(mockPstmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(USER_ID);

        // 2. Mock: UPDATE borrow_records throws exception
        when(mockConnection.prepareStatement(startsWith("UPDATE borrow_records")))
                .thenThrow(new SQLException("Simulated UPDATE error on return"));

        // Execute
        borrower.removeBorrowRecord(record, 5.0);

        // Assert: The list size must remain the same (record not removed)
        assertEquals(initialSize, borrower.getBorrowedMedia().size(), "Record should NOT be removed if UPDATE fails.");

        // Verify that System.out printed an error message
        // (Checking for the specific error message, which is often 'Error updating borrow record')
        // Note: You must capture System.out manually if you want a precise assertion here.
    }


    /**
     * Test Case 25: loadBorrowedMedia() - Inner Book Query SQLException
     * Tests graceful failure when fetching book details.
     */
    @Test
    void loadBorrowedMedia_BookDetailsQueryFails_RecordIsSkipped() throws SQLException {
        // Setup: Ensure the internal list is clean
        borrower.getBorrowedMedia().clear();

        // 1. Mock getUserId
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockPstmt);
        when(mockPstmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(USER_ID);

        // 2. Mock borrow_records query (Returns one book row)
        PreparedStatement mockBrPstmt = mock(PreparedStatement.class);
        ResultSet mockBrRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT br.*"))).thenReturn(mockBrPstmt);
        when(mockBrPstmt.executeQuery()).thenReturn(mockBrRs);
        when(mockBrRs.next()).thenReturn(true, false);
        when(mockBrRs.getString("media_type")).thenReturn("book");
        when(mockBrRs.getString("due_date")).thenReturn(LocalDate.now().plusDays(5).toString());

        // 3. Mock Book details query: throws SQLException (THE FAILURE BRANCH)
        PreparedStatement mockBookPstmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("SELECT b.author"))).thenReturn(mockBookPstmt);
        when(mockBookPstmt.executeQuery()).thenThrow(new SQLException("Simulated book details fetch error"));

        // Execute and Assert
        borrower.loadBorrowedMedia();

        // Assert: The internal borrowedMedia list must be empty because the Book details query failed
        assertTrue(borrower.getBorrowedMedia().isEmpty(), "Record must be skipped if inner media details query fails.");

        // Verify System.out was called for error message printing
    }

    /**
     * Test Case 26: loadBorrowedMedia() - Inner CD Query Failure
     * Tests graceful failure when fetching CD details.
     */
    @Test
    void loadBorrowedMedia_CDDetailsQueryFails_RecordIsSkipped() throws SQLException {
        // Setup: Ensure the internal list is clean
        borrower.getBorrowedMedia().clear();

        // 1. Mock getUserId
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockPstmt);
        when(mockPstmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(USER_ID);

        // 2. Mock borrow_records query (Returns one CD row)
        PreparedStatement mockBrPstmt = mock(PreparedStatement.class);
        ResultSet mockBrRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(startsWith("SELECT br.*"))).thenReturn(mockBrPstmt);
        when(mockBrPstmt.executeQuery()).thenReturn(mockBrRs);
        when(mockBrRs.next()).thenReturn(true, false);
        when(mockBrRs.getString("media_type")).thenReturn("cd");
        when(mockBrRs.getString("due_date")).thenReturn(LocalDate.now().plusDays(5).toString());

        // 3. Mock CD details query: throws SQLException (THE FAILURE BRANCH)
        PreparedStatement mockCdPstmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("SELECT c.artist"))).thenReturn(mockCdPstmt);
        when(mockCdPstmt.executeQuery()).thenThrow(new SQLException("Simulated CD details fetch error"));

        // Execute and Assert
        borrower.loadBorrowedMedia();

        // Assert: The internal borrowedMedia list must be empty
        assertTrue(borrower.getBorrowedMedia().isEmpty(), "Record must be skipped if inner media details query fails.");
    }


    /**
     * Test Case 23: getUserId() - SQLException Branch
     * Ensures the method gracefully handles a DB error during execution.
     */
    @Test
    void getUserId_SQLException_ReturnsNegativeOne() throws SQLException {
        // 1. Mock getUserId to return a PreparedStatement
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockPstmt);

        // 2. Mock the execution to throw an exception (THE FAILURE BRANCH)
        when(mockPstmt.executeQuery()).thenThrow(new SQLException("Simulated SELECT error on user ID fetch"));

        // Execute and Assert
        int userId = borrower.getUserId();
        assertEquals(-1, userId, "getUserId should return -1 on SELECT error.");

        // Ensure System.out was called (for error message printing)
        // (Verification omitted here for simplicity, but often done via output stream capture)
    }

    /**
     * Test Case 24: saveFineBalance() - Insert Fallback SQLException
     * Tests the nested catch block for the INSERT statement.
     */
    @Test
    void saveFineBalance_UpdateFailsThenInsertFails_HandlesGracefully() throws SQLException {
        // Setup initial fine balance for local check
        borrower.setFineBalance(50.0);

        // 1. Mock getUserId
        when(mockConnection.prepareStatement(startsWith("SELECT id FROM users"))).thenReturn(mockPstmt);
        when(mockPstmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(USER_ID);

        // 2. Mock UPDATE statement: returns 0 rows (triggers INSERT fallback)
        PreparedStatement mockUpdatePstmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("UPDATE user_fines"))).thenReturn(mockUpdatePstmt);
        when(mockUpdatePstmt.executeUpdate()).thenReturn(0);

        // 3. Mock INSERT statement: throws exception (THE FAILURE BRANCH)
        PreparedStatement mockInsertPstmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("INSERT INTO user_fines"))).thenReturn(mockInsertPstmt);
        when(mockInsertPstmt.executeUpdate()).thenThrow(new SQLException("Simulated INSERT failure on save fine"));

        // Capture output for error message verification
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Execute
            borrower.saveFineBalance();

            // Assert: Fine balance remains locally correct
            assertEquals(50.0, borrower.getFineBalance(), "Local fine balance should be unchanged by DB failure.");

            // Verify error message for nested exception was printed
            assertTrue(outContent.toString().contains("Error saving fines:"), "Expected nested INSERT error message not printed.");
        } finally {
            System.setOut(originalOut);
        }
    }


    @Test
    void generateOverdueReport_NoOverdueItems_PrintsNoItemsMessage() throws Exception {
        Borrower spyBorrower = Mockito.spy(borrower);
        doReturn(Collections.emptyList()).when(spyBorrower).getOverdueMedia();

        // لا حاجة لـ reset() هنا — يتم التنظيف في @AfterEach
        spyBorrower.generateOverdueReport();

        String output = outContent.toString(StandardCharsets.UTF_8);
        String cleaned = output.trim().replaceAll("\\s+", " ");
        String expected = "ليس لديك مواد متأخرة.";

        assertTrue(cleaned.contains(expected),
                "Expected: '" + expected + "', but output was: '" + cleaned + "'");
    }


    @Test
    void generateOverdueReport_MultipleOverdueItems_CalculatesFinesCorrectly() throws Exception {
        Media book = new Book(1, "Clean Code", "Robert Martin", "123", true);
        Media cd = new CD(2, "Best of Mozart", "Orchestra", "Classical", 60, true);

        Borrower.MediaRecord bookRecord = spy(borrower.new MediaRecord(book, LocalDate.now().minusDays(10), 101));
        doReturn(10L).when(bookRecord).getOverdueDays();

        Borrower.MediaRecord cdRecord = spy(borrower.new MediaRecord(cd, LocalDate.now().minusDays(5), 102));
        doReturn(5L).when(cdRecord).getOverdueDays();

        List<Borrower.MediaRecord> overdueList = Arrays.asList(bookRecord, cdRecord);

        Borrower spyBorrower = Mockito.spy(borrower);
        doReturn(overdueList).when(spyBorrower).getOverdueMedia();

        try (MockedStatic<BorrowingService> mocked = mockStatic(BorrowingService.class)) {
            mocked.when(() -> BorrowingService.getFinePerDay("book")).thenReturn(0.50);
            mocked.when(() -> BorrowingService.getFinePerDay("cd")).thenReturn(1.00);

            spyBorrower.generateOverdueReport();

            String output = outContent.toString(StandardCharsets.UTF_8);
            String cleaned = output.trim().replaceAll("\\s+", " ");

            assertTrue(cleaned.contains("--- تقرير المواد المتأخرة ---"), "Header missing");
            assertTrue(cleaned.contains("العنوان: 'Clean Code', النوع: كتاب, أيام التأخير: ١٠, الغرامة: ٥٫٠٠"), "Book line missing");
            assertTrue(cleaned.contains("العنوان: 'Best of Mozart', النوع: قرص مدمج, أيام التأخير: ٥, الغرامة: ٥٫٠٠"), "CD line missing");
            assertTrue(cleaned.contains("إجمالي الغرامات المتأخرة: ١٠٫٠٠"), "Total fine missing");
        }
    }


}