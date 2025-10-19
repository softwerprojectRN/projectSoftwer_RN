//import domain.Book;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.io.File;
//import java.sql.*;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
///**
// * فئة اختبار لكلاس Book.
// * نراعي مبدأ Single Responsibility Principle (SRP) من خلال فصل الاختبارات حسب المسؤوليات الرئيسية.
// */
//@ExtendWith(MockitoExtension.class)
//public class BookTest {
//
//    private static final String DB_URL = "jdbc:sqlite:test_database.db";
//    private Connection connection;
//    private MockedStatic<DriverManager> driverManagerMock;
//
//    @BeforeEach
//    void setUp() throws SQLException {
//        // الحصول على connection حقيقي
//        Connection realConn = DriverManager.getConnection(DB_URL);
//
//        // Mock DriverManager
//        driverManagerMock = mockStatic(DriverManager.class);
//        when(DriverManager.getConnection(anyString())).thenReturn(realConn);
//
//        connection = DriverManager.getConnection(DB_URL);
//
//        // إنشاء الجدول
//        String createTableSQL = "CREATE TABLE IF NOT EXISTS books (\n" +
//                " id integer PRIMARY KEY AUTOINCREMENT,\n" +
//                " title text NOT NULL,\n" +
//                " author text NOT NULL,\n" +
//                " isbn text NOT NULL UNIQUE,\n" +
//                " available integer NOT NULL DEFAULT 1\n" +
//                ");";
//
//        try (Statement stmt = connection.createStatement()) {
//            stmt.execute("DROP TABLE IF EXISTS books");
//            stmt.execute(createTableSQL);
//            System.out.println("تم إنشاء جدول books للاختبار بنجاح.");
//        } catch (SQLException e) {
//            throw e;
//        }
//    }
//
//    @AfterEach
//    void tearDown() throws SQLException {
//        if (connection != null) {
//            connection.close();
//        }
//        if (driverManagerMock != null) {
//            driverManagerMock.close();
//        }
//        new File("test_database.db").delete();
//    }
//
//    // ==================== Domain Model Tests ====================
//
//    @Test
//    void testConstructor() {
//        Book book = new Book(1, "Test Title", "Test Author", "1234567890", true);
//        assertEquals(1, book.getId());
//        assertEquals("Test Title", book.getTitle());
//        assertEquals("Test Author", book.getAuthor());
//        assertEquals("1234567890", book.getIsbn());
//        assertTrue(book.isAvailable());
//    }
//
//    @Test
//    void testGetters() {
//        Book book = new Book(1, "Title", "Author", "ISBN", false);
//        assertEquals(1, book.getId());
//        assertEquals("Title", book.getTitle());
//        assertEquals("Author", book.getAuthor());
//        assertEquals("ISBN", book.getIsbn());
//        assertFalse(book.isAvailable());
//    }
//
//    @Test
//    void testToStringWhenIsAvailableIsTrue() {
//        Book book = new Book(1, "Test Title", "Test Author", "1234567890", true);
//        book.returnBook();
//        String result = book.toString();
//        assertTrue(result.contains("ID: 1"));
//        assertTrue(result.contains("Title: 'Test Title'"));
//        assertTrue(result.contains("Author: 'Test Author'"));
//        assertTrue(result.contains("ISBN: 1234567890"));
//        assertTrue(result.contains("Available: Yes"));
//    }
//
//    @Test
//    void testToStringWhenIsAvailableIsFalse() {
//        Book book = new Book(1, "Test Title", "Test Author", "1234567890", false);
//        book.borrow();
//        String result = book.toString();
//        assertTrue(result.contains("ID: 1"));
//        assertTrue(result.contains("Title: 'Test Title'"));
//        assertTrue(result.contains("Author: 'Test Author'"));
//        assertTrue(result.contains("ISBN: 1234567890"));
//        assertTrue(result.contains("Available: No"));
//    }
//
//    // ==================== Internal Management Tests ====================
//
//    @Test
//    void testBorrow_WhenAvailable_SetsUnavailable() throws SQLException {
//        // Override the static stubbing for this test
//        Connection mockConn = mock(Connection.class);
//        PreparedStatement mockPstmt = mock(PreparedStatement.class);
//        when(mockConn.prepareStatement(eq("UPDATE books SET available = ? WHERE id = ?"))).thenReturn(mockPstmt);
//        when(mockPstmt.executeUpdate()).thenReturn(1);
//        when(DriverManager.getConnection(anyString())).thenReturn(mockConn);
//
//        Book book = new Book(1, "Title", "Author", "ISBN", true);
//        book.borrow();
//
//        assertFalse(book.isAvailable());
//        verify(mockPstmt).executeUpdate();
//
//        // Restore
//        when(DriverManager.getConnection(anyString())).thenReturn(connection);
//    }
//
//    @Test
//    void testBorrow_WhenNotAvailable_PrintsMessage() {
//        Book book = new Book(1, "Title", "Author", "ISBN", false);
//        book.borrow();
//        assertFalse(book.isAvailable());
//    }
//
//    @Test
//    void testReturnBook_WhenNotAvailable_SetsAvailable() throws SQLException {
//        // Override the static stubbing for this test
//        Connection mockConn = mock(Connection.class);
//        PreparedStatement mockPstmt = mock(PreparedStatement.class);
//        when(mockConn.prepareStatement(eq("UPDATE books SET available = ? WHERE id = ?"))).thenReturn(mockPstmt);
//        when(mockPstmt.executeUpdate()).thenReturn(1);
//        when(DriverManager.getConnection(anyString())).thenReturn(mockConn);
//
//        Book book = new Book(1, "Title", "Author", "ISBN", false);
//        book.returnBook();
//
//        assertTrue(book.isAvailable());
//        verify(mockPstmt).executeUpdate();
//
//        // Restore
//        when(DriverManager.getConnection(anyString())).thenReturn(connection);
//    }
//
//    @Test
//    void testReturnBook_WhenAvailable_PrintsMessage() {
//        Book book = new Book(1, "Title", "Author", "ISBN", true);
//        book.returnBook();
//        assertTrue(book.isAvailable());
//    }
//
//    @Test
//    void testUpdateAvailability_UpdatesFieldWhenIdValid() throws SQLException {
//        // Override the static stubbing for this test
//        Connection mockConn = mock(Connection.class);
//        PreparedStatement mockPstmt = mock(PreparedStatement.class);
//        when(mockConn.prepareStatement(eq("UPDATE books SET available = ? WHERE id = ?"))).thenReturn(mockPstmt);
//        when(mockPstmt.executeUpdate()).thenReturn(1);
//        when(DriverManager.getConnection(anyString())).thenReturn(mockConn);
//
//        Book book = new Book(1, "Title", "Author", "ISBN", true);
//        book.updateAvailability(false);
//
//        assertFalse(book.isAvailable());
//        verify(mockPstmt).executeUpdate();
//
//        // Restore
//        when(DriverManager.getConnection(anyString())).thenReturn(connection);
//    }
//
//    @Test
//    void testUpdateAvailability_WhenIdZero_PrintsMessage() {
//        Book book = new Book(0, "Title", "Author", "ISBN", true);
//        book.updateAvailability(false);
//        assertTrue(book.isAvailable());
//    }
//
//    @Test
//    void testUpdateAvailability_WhenNoRowsAffected_PrintsMessage() throws SQLException {
//        // Override the static stubbing for this test
//        Connection mockConn = mock(Connection.class);
//        PreparedStatement mockPstmt = mock(PreparedStatement.class);
//        when(mockConn.prepareStatement(eq("UPDATE books SET available = ? WHERE id = ?"))).thenReturn(mockPstmt);
//        when(mockPstmt.executeUpdate()).thenReturn(0);
//        when(DriverManager.getConnection(anyString())).thenReturn(mockConn);
//
//        Book book = new Book(999, "Title", "Author", "ISBN", true);
//        book.updateAvailability(false);
//
//        assertTrue(book.isAvailable());
//        verify(mockPstmt).executeUpdate();
//
//        // Restore
//        when(DriverManager.getConnection(anyString())).thenReturn(connection);
//    }
//
//    // ==================== DB Access Tests ====================
//
//    @Test
//    void testAddBook_SuccessfulAddition_ReturnsBookObject() throws SQLException {
//        Book addedBook = Book.addBook("Test Title", "Test Author", "1234567890");
//        assertNotNull(addedBook);
//        assertTrue(addedBook.getId() > 0);
//        assertEquals("Test Title", addedBook.getTitle());
//        assertTrue(addedBook.isAvailable());
//
//        try (Statement stmt = connection.createStatement();
//             ResultSet rs = stmt.executeQuery("SELECT * FROM books WHERE isbn = '1234567890'")) {
//            assertTrue(rs.next());
//            assertEquals("Test Title", rs.getString("title"));
//            assertEquals(1, rs.getInt("available"));
//        }
//    }
//
//    @Test
//    void testAddBook_WhenIsbnExists_ReturnsNull() throws SQLException {
//        Book.addBook("Existing Title", "Existing Author", "1234567890");
//        Book duplicateBook = Book.addBook("Duplicate Title", "Duplicate Author", "1234567890");
//        assertNull(duplicateBook);
//    }
//
//    @Test
//    void testAddBook_WhenSQLExceptionOccurs_ReturnsNull() throws SQLException {
//        // Stub getLogWriter first to allow SQLException creation
//        when(DriverManager.getLogWriter()).thenReturn(null);
//
//        // Create the exception now
//        SQLException dbError = new SQLException("DB Error");
//
//        // Override the static stubbing for this test
//        Connection mockConn = mock(Connection.class);
//        PreparedStatement mockPstmtCheck = mock(PreparedStatement.class);
//        ResultSet mockRs = mock(ResultSet.class);
//        when(mockConn.prepareStatement(eq("SELECT * FROM books WHERE isbn = ?"))).thenReturn(mockPstmtCheck);
//        when(mockPstmtCheck.executeQuery()).thenReturn(mockRs);
//        when(mockRs.next()).thenReturn(false);
//        PreparedStatement mockPstmtInsert = mock(PreparedStatement.class);
//        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPstmtInsert);
//        when(mockPstmtInsert.executeUpdate()).thenThrow(dbError);
//        when(DriverManager.getConnection(anyString())).thenReturn(mockConn);
//
//        Book book = Book.addBook("Title", "Author", "ISBN");
//
//        assertNull(book);
//
//        // Restore
//        when(DriverManager.getConnection(anyString())).thenReturn(connection);
//        when(DriverManager.getLogWriter()).thenCallRealMethod();
//    }
//
//    @Test
//    void testGetAllBooks_ReturnsAllBooks() throws SQLException {
//        Book.addBook("Book 1", "Author 1", "ISBN1");
//        Book.addBook("Book 2", "Author 2", "ISBN2");
//        List<Book> books = Book.getAllBooks();
//        assertEquals(2, books.size());
//        assertEquals("Book 1", books.get(0).getTitle());
//        assertEquals("Book 2", books.get(1).getTitle());
//    }
//
//    @Test
//    void testGetAllBooks_WhenEmptyDatabase_ReturnsEmptyList() {
//        List<Book> books = Book.getAllBooks();
//        assertTrue(books.isEmpty());
//    }
//
//    @Test
//    void testStaticBlock_CreatesTableSuccessfully() throws SQLException {
//        try (Statement stmt = connection.createStatement();
//             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='books'")) {
//            assertTrue(rs.next());
//        }
//    }
//
//    @Test
//    void testBorrow_UpdatesDatabaseAvailability() throws SQLException {
//        Book book = Book.addBook("Title", "Author", "ISBN");
//        book.borrow();
//        try (Statement stmt = connection.createStatement();
//             ResultSet rs = stmt.executeQuery("SELECT available FROM books WHERE id = " + book.getId())) {
//            assertTrue(rs.next());
//            assertEquals(0, rs.getInt("available"));
//        }
//        assertFalse(book.isAvailable());
//    }
//
//    @Test
//    void testReturnBook_UpdatesDatabaseAvailability() throws SQLException {
//        Book book = Book.addBook("Title", "Author", "ISBN");
//        book.borrow();
//        book.returnBook();
//        try (Statement stmt = connection.createStatement();
//             ResultSet rs = stmt.executeQuery("SELECT available FROM books WHERE id = " + book.getId())) {
//            assertTrue(rs.next());
//            assertEquals(1, rs.getInt("available"));
//        }
//        assertTrue(book.isAvailable());
//    }
//
//
//
//
//
//
//}