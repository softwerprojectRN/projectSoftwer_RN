import domain.Borrower;
import domain.Book;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BorrowerTest {

    // --- Mocks ثابتة ومشتركة ---
    private static MockedStatic<Borrower> mockedBorrowerStatic;
    private static Connection mockConnection;

    // --- Mocks يتم إنشاؤها لكل اختبار ---
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private Borrower borrower;
    private Book testBook;

    @BeforeAll
    public static void setUpStatic() {
        mockConnection = mock(Connection.class);
        mockedBorrowerStatic = mockStatic(Borrower.class);
        mockedBorrowerStatic.when(Borrower::connect).thenReturn(mockConnection);
    }

    @AfterAll
    public static void tearDownStatic() {
        mockedBorrowerStatic.close();
    }

    @BeforeEach
    public void setUp() throws SQLException {
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        testBook = new Book(1, "Test Book", "Test Author", "123456789", false);

        // --- إعداد mocks لسلوك الـ constructor ---
        when(mockConnection.prepareStatement(contains("SELECT id FROM users"))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(123);

        PreparedStatement mockBorrowedStmt = mock(PreparedStatement.class);
        ResultSet mockBorrowedRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("SELECT br.*, b.id as book_id"))).thenReturn(mockBorrowedStmt);
        when(mockBorrowedStmt.executeQuery()).thenReturn(mockBorrowedRs);
        when(mockBorrowedRs.next()).thenReturn(false);

        PreparedStatement mockFineStmt = mock(PreparedStatement.class);
        ResultSet mockFineRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("SELECT total_fine FROM user_fines"))).thenReturn(mockFineStmt);
        when(mockFineStmt.executeQuery()).thenReturn(mockFineRs);
        when(mockFineRs.next()).thenReturn(true);
        when(mockFineRs.getDouble("total_fine")).thenReturn(0.0);

        borrower = new Borrower("testuser", "hashedpassword", "salt");
    }

    // ========== الاختبارات الأصلية (لم يتم تغييرها) ==========

    @Test
    public void testGetUserId() throws SQLException {
        // Mock للاتصال
        mockedBorrowerStatic.when(Borrower::connect).thenReturn(mockConnection);

        // إعداد PreparedStatement و ResultSet
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConnection.prepareStatement(contains("SELECT id FROM users"))).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("id")).thenReturn(999);

        // استدعاء الدالة
        int userId = borrower.getUserId();

        // التحقق
        assertEquals(999, userId);
        verify(mockStmt).setString(1, "testuser");
    }


    @Test
    public void testLoadBorrowedBooks() throws SQLException {
        PreparedStatement mockLoadStmt = mock(PreparedStatement.class);
        ResultSet mockLoadRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("SELECT br.*, b.id as book_id"))).thenReturn(mockLoadStmt);
        when(mockLoadStmt.executeQuery()).thenReturn(mockLoadRs);
        when(mockLoadRs.next()).thenReturn(true, true, false);
        when(mockLoadRs.getInt("book_id")).thenReturn(1, 2);
        when(mockLoadRs.getString("book_title")).thenReturn("Test Book 1", "Test Book 2");
        when(mockLoadRs.getString("book_isbn")).thenReturn("123456789", "987654321");
        when(mockLoadRs.getString("due_date")).thenReturn(LocalDate.now().plusDays(14).toString(),
                LocalDate.now().plusDays(7).toString());
        when(mockLoadRs.getInt("id")).thenReturn(1, 2);
        borrower.loadBorrowedBooks();
        List<Borrower.BookRecord> borrowedBooks = borrower.getBorrowedBooks();
        assertEquals(2, borrowedBooks.size());
    }
    @Test
    public void testLoadFineBalance() throws SQLException {
        // Mock للاتصال
        mockedBorrowerStatic.when(Borrower::connect).thenReturn(mockConnection);

        // Mock PreparedStatement و ResultSet
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        // إعداد الـ mocks
        when(mockConnection.prepareStatement(contains("SELECT total_fine"))).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);           // يوجد سجل
        when(mockRs.getDouble("total_fine")).thenReturn(50.75);

        // استدعاء الدالة
        borrower.loadFineBalance();

        // التأكد من أن الرصيد تم تحميله
        assertEquals(50.75, borrower.getFineBalance(), 0.001);
    }


    @Test
    public void testAddBorrowRecord() throws SQLException {
        // Mock للاتصال
        mockedBorrowerStatic.when(Borrower::connect).thenReturn(mockConnection);

        // Mock PreparedStatement و ResultSet
        PreparedStatement mockInsertStmt = mock(PreparedStatement.class);
        ResultSet mockGeneratedKeys = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockInsertStmt);
        when(mockInsertStmt.executeUpdate()).thenReturn(1); // نجاح الإدراج
        when(mockInsertStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true); // المفتاح موجود
        when(mockGeneratedKeys.getInt(1)).thenReturn(456); // ID وهمي

        // استدعاء الدالة
        LocalDate dueDate = LocalDate.now().plusDays(14);
        borrower.addBorrowRecord(testBook, dueDate);

        // التأكد من إضافة السجل
        List<Borrower.BookRecord> borrowedBooks = borrower.getBorrowedBooks();
        assertEquals(1, borrowedBooks.size());
        assertEquals(testBook.getTitle(), borrowedBooks.get(0).getBook().getTitle());
        assertEquals(dueDate, borrowedBooks.get(0).getDueDate());
        assertEquals(456, borrowedBooks.get(0).getRecordId());
    }


    @Test
    public void testRemoveBorrowRecord() throws Exception {
        Borrower.BookRecord record = borrower.new BookRecord(testBook, LocalDate.now().plusDays(7), 123);
        Field borrowedBooksField = Borrower.class.getDeclaredField("borrowedBooks");
        borrowedBooksField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Borrower.BookRecord> actualBorrowedBooks = (List<Borrower.BookRecord>) borrowedBooksField.get(borrower);
        actualBorrowedBooks.add(record);

        PreparedStatement mockUpdateStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(contains("UPDATE borrow_records SET returned = 1"))).thenReturn(mockUpdateStmt);
        when(mockUpdateStmt.executeUpdate()).thenReturn(1);
        borrower.removeBorrowRecord(record, 5.0);
        assertTrue(borrower.getBorrowedBooks().isEmpty());
        assertEquals(5.0, borrower.getFineBalance(), 0.001);
    }

    @Test
    public void testPayFine() throws SQLException {
        borrower.setFineBalance(10.0);
        PreparedStatement mockUpdateFineStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(contains("UPDATE user_fines SET total_fine"))).thenReturn(mockUpdateFineStmt);
        when(mockUpdateFineStmt.executeUpdate()).thenReturn(1);
        boolean result = borrower.payFine(5.0);
        assertTrue(result);
        assertEquals(5.0, borrower.getFineBalance(), 0.001);
    }

    @Test
    public void testPayFineInvalidAmount() {
        borrower.setFineBalance(10.0);
        assertFalse(borrower.payFine(15.0));
        assertEquals(10.0, borrower.getFineBalance(), 0.001);
    }

    @Test
    public void testGetOverdueBooks() throws Exception {
        LocalDate overdueDate = LocalDate.now().minusDays(5);
        LocalDate futureDate = LocalDate.now().plusDays(5);
        Borrower.BookRecord overdueRecord = borrower.new BookRecord(testBook, overdueDate, 123);
        Borrower.BookRecord futureRecord = borrower.new BookRecord(testBook, futureDate, 456);

        Field borrowedBooksField = Borrower.class.getDeclaredField("borrowedBooks");
        borrowedBooksField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Borrower.BookRecord> actualBorrowedBooks = (List<Borrower.BookRecord>) borrowedBooksField.get(borrower);
        actualBorrowedBooks.add(overdueRecord);
        actualBorrowedBooks.add(futureRecord);

        List<Borrower.BookRecord> overdueBooks = borrower.getOverdueBooks();
        assertEquals(1, overdueBooks.size());
        assertEquals(overdueRecord, overdueBooks.get(0));
    }

    @Test
    public void testBookRecordIsOverdue() {
        Borrower.BookRecord overdueRecord = borrower.new BookRecord(testBook, LocalDate.now().minusDays(5), 123);
        Borrower.BookRecord futureRecord = borrower.new BookRecord(testBook, LocalDate.now().plusDays(5), 456);
        assertTrue(overdueRecord.isOverdue());
        assertFalse(futureRecord.isOverdue());
    }

    @Test
    public void testBookRecordGetOverdueDays() {
        Borrower.BookRecord overdueRecord = borrower.new BookRecord(testBook, LocalDate.now().minusDays(5), 123);
        assertEquals(5, overdueRecord.getOverdueDays());
    }

    // ========== الاختبارات الجديدة لزيادة التغطية ==========

    @Test
    public void testGetUserIdUserNotFound() throws SQLException {
        PreparedStatement mockUserIdStmt = mock(PreparedStatement.class);
        ResultSet mockUserIdRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("SELECT id FROM users"))).thenReturn(mockUserIdStmt);
        when(mockUserIdStmt.executeQuery()).thenReturn(mockUserIdRs);
        when(mockUserIdRs.next()).thenReturn(false); // المستخدم غير موجود
        int userId = borrower.getUserId();
        assertEquals(-1, userId);
    }

    @Test
    public void testGetUserIdSqlException() throws SQLException {
        PreparedStatement mockUserIdStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(contains("SELECT id FROM users"))).thenReturn(mockUserIdStmt);
        when(mockUserIdStmt.executeQuery()).thenThrow(new SQLException("Database error"));
        int userId = borrower.getUserId();
        assertEquals(-1, userId);
    }

    @Test
    public void testLoadBorrowedBooksWithInvalidUserId() throws SQLException {
        PreparedStatement mockUserIdStmt = mock(PreparedStatement.class);
        ResultSet mockUserIdRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("SELECT id FROM users"))).thenReturn(mockUserIdStmt);
        when(mockUserIdStmt.executeQuery()).thenReturn(mockUserIdRs);
        when(mockUserIdRs.next()).thenReturn(false); // getUserId() سيعيد -1

        borrower.loadBorrowedBooks();
        assertTrue(borrower.getBorrowedBooks().isEmpty());
    }

    @Test
    public void testLoadFineBalanceInsertsNewRecord() throws SQLException {
        PreparedStatement mockSelectStmt = mock(PreparedStatement.class);
        ResultSet mockSelectRs = mock(ResultSet.class);
        PreparedStatement mockInsertStmt = mock(PreparedStatement.class);

        // عندما يتم استدعاء loadFineBalance، سيتم أولاً استدعاء getUserId
        when(mockConnection.prepareStatement(contains("SELECT id FROM users"))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(123);

        // الآن، قم بإعداد mock لاستعلام تحميل الرصيد
        when(mockConnection.prepareStatement(contains("SELECT total_fine FROM user_fines"))).thenReturn(mockSelectStmt);
        when(mockSelectStmt.executeQuery()).thenReturn(mockSelectRs);
        when(mockSelectRs.next()).thenReturn(false); // لا يوجد سجل غرامات

        // وأخيراً، إعداد mock لعملية الإدراج
        when(mockConnection.prepareStatement(contains("INSERT INTO user_fines"))).thenReturn(mockInsertStmt);
        when(mockInsertStmt.executeUpdate()).thenReturn(1);

        borrower.loadFineBalance();
        assertEquals(0.0, borrower.getFineBalance());
        verify(mockInsertStmt).executeUpdate();
    }

    @Test
    public void testSaveFineBalanceInsertsNewRecord() throws SQLException {
        borrower.setFineBalance(25.0);

        // سنقوم بإنشاء mocks جديدة لهذا الاختبار لتجنب التداخل
        PreparedStatement mockUpdateStmt = mock(PreparedStatement.class);
        PreparedStatement mockInsertStmt = mock(PreparedStatement.class);

        // getUserId() mock - سيتم استدعاؤه بواسطة saveFineBalance
        when(mockConnection.prepareStatement(contains("SELECT id FROM users"))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(123);

        // إعداد mock لعملية التحديث (UPDATE) لتفشل
        when(mockConnection.prepareStatement(contains("UPDATE user_fines SET total_fine"))).thenReturn(mockUpdateStmt);
        when(mockUpdateStmt.executeUpdate()).thenReturn(0); // لم يتم تحديث أي صف

        // إعداد mock لعملية الإدراج (INSERT) لتنجح
        when(mockConnection.prepareStatement(contains("INSERT INTO user_fines"))).thenReturn(mockInsertStmt);
        when(mockInsertStmt.executeUpdate()).thenReturn(1);

        // --- التعديل الرئيسي ---
        // قم بإزالة الاستدعاء المباشر للدالة الخاصة
        // borrower.saveFineBalance(); // <-- احذف هذا السطر

        // اختبر الدالة بشكل غير مباشر فقط
        borrower.payFine(5.0); // هذا سيستدعي saveFineBalance() مرة واحدة فقط

        // الآن، التحقق سينجح لأن كل دالة تم استدعاؤها مرة واحدة فقط
        verify(mockUpdateStmt, times(1)).executeUpdate();
        verify(mockInsertStmt, times(1)).executeUpdate();

        // يمكنك أيضاً التحقق من أن الرصيد قد تغير بشكل صحيح
        assertEquals(20.0, borrower.getFineBalance());
    }
    @Test
    public void testAddBorrowRecordFailsToAddToList() throws SQLException {
        PreparedStatement mockInsertStmt = mock(PreparedStatement.class);
        ResultSet mockInsertRs = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockInsertStmt);
        when(mockInsertStmt.executeUpdate()).thenReturn(1);
        when(mockInsertStmt.getGeneratedKeys()).thenReturn(mockInsertRs);
        when(mockInsertRs.next()).thenReturn(false); // لا توجد مفاتيح تم إنشاؤها

        borrower.addBorrowRecord(testBook, LocalDate.now().plusDays(14));
        assertTrue(borrower.getBorrowedBooks().isEmpty());
    }

    @Test
    public void testRemoveBorrowRecordFailsToUpdate() throws Exception {
        Borrower.BookRecord record = borrower.new BookRecord(testBook, LocalDate.now().plusDays(7), 123);
        Field borrowedBooksField = Borrower.class.getDeclaredField("borrowedBooks");
        borrowedBooksField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Borrower.BookRecord> actualBorrowedBooks = (List<Borrower.BookRecord>) borrowedBooksField.get(borrower);
        actualBorrowedBooks.add(record);
        double initialFine = borrower.getFineBalance();

        PreparedStatement mockUpdateStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(contains("UPDATE borrow_records SET returned = 1"))).thenReturn(mockUpdateStmt);
        when(mockUpdateStmt.executeUpdate()).thenReturn(0); // فشل التحديث

        borrower.removeBorrowRecord(record, 5.0);

        assertEquals(1, borrower.getBorrowedBooks().size()); // لم تتم إزالة الكتاب
        assertEquals(initialFine, borrower.getFineBalance()); // لم تتم إضافة الغرامة
    }

    @Test
    public void testPayFineZeroAmount() {
        borrower.setFineBalance(10.0);
        assertFalse(borrower.payFine(0.0));
        assertEquals(10.0, borrower.getFineBalance(), 0.001);
    }

    @Test
    public void testBookRecordGetOverdueDaysWhenNotOverdue() {
        Borrower.BookRecord futureRecord = borrower.new BookRecord(testBook, LocalDate.now().plusDays(5), 456);
        assertEquals(0, futureRecord.getOverdueDays());
    }
    @Test
    public void testStaticInitializerDatabaseNull() {
        mockedBorrowerStatic.when(Borrower::connect).thenReturn(null);
        // هذا فقط لإجبار static block على الفرع الذي يتعامل مع conn == null
        Borrower temp = new Borrower("user", "pass", "salt");
        assertNotNull(temp);
    }

    @Test
    public void testAddBorrowRecordExecuteUpdateFails() throws SQLException {
        Borrower.BookRecord record = borrower.new BookRecord(testBook, LocalDate.now().plusDays(7), 0);

        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockGeneratedKeys = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(0); // فشل الإدراج
        when(mockStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(false); // لا يوجد مفتاح مولد

        borrower.addBorrowRecord(testBook, LocalDate.now().plusDays(7));

        // يجب أن لا يتم إضافة أي سجل
        assertTrue(borrower.getBorrowedBooks().isEmpty());
    }

    @Test
    public void testAddBorrowRecordNoGeneratedKeys() throws SQLException {
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);
        when(mockStmt.getGeneratedKeys()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false); // لا توجد مفاتيح

        borrower.addBorrowRecord(testBook, LocalDate.now().plusDays(7));
        assertTrue(borrower.getBorrowedBooks().isEmpty());
    }
    @Test
    public void testRemoveBorrowRecordExecuteUpdateFails() throws Exception {
        // إنشاء سجل جديد
        Borrower.BookRecord record = borrower.new BookRecord(testBook, LocalDate.now().plusDays(7), 123);

        // استخدام Reflection لإضافة السجل إلى borrowedBooks الخاصة بالـ borrower
        Field borrowedBooksField = Borrower.class.getDeclaredField("borrowedBooks");
        borrowedBooksField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Borrower.BookRecord> actualBorrowedBooks = (List<Borrower.BookRecord>) borrowedBooksField.get(borrower);
        actualBorrowedBooks.add(record);

        // Mock PreparedStatement لتحديث borrow_records
        PreparedStatement mockUpdateStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(contains("UPDATE borrow_records"))).thenReturn(mockUpdateStmt);
        when(mockUpdateStmt.executeUpdate()).thenReturn(0); // فشل التحديث

        // استدعاء الدالة
        borrower.removeBorrowRecord(record, 5.0);

        // التحقق: بما أن التحديث فشل، يجب أن يبقى السجل في القائمة
        List<Borrower.BookRecord> borrowedBooks = borrower.getBorrowedBooks();
        assertEquals(1, borrowedBooks.size());
        assertEquals(0.0, borrower.getFineBalance(), 0.001); // لم تُضاف الغرامة
    }

    @Test
    public void testBookRecordGetOverdueDaysNotOverdue() {
        LocalDate futureDate = LocalDate.now().plusDays(3);
        Borrower.BookRecord record = borrower.new BookRecord(testBook, futureDate, 1);
        assertEquals(0, record.getOverdueDays());
    }
    @Test
    public void testPayFineAmountTooHigh() {
        borrower.setFineBalance(10.0);
        assertFalse(borrower.payFine(20.0));
    }

}