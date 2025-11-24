import dao.*;

import model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookDAOTest {

    private BookDAO bookDAO;

    @BeforeEach
    void setUp() {
        bookDAO = new BookDAO();
    }

    // ------------------ initializeTable() ------------------

    @Test
    void testInitializeTable_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        when(mockConn.createStatement()).thenReturn(mockStmt);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            bookDAO.initializeTable();

            verify(mockStmt, times(1)).execute(anyString());
            String output = outContent.toString();
            assertTrue(output.contains("Books table created successfully."));
        } finally {
            System.setOut(System.out);
        }
    }

    @Test
    void testInitializeTable_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        when(mockConn.createStatement()).thenReturn(mockStmt);
        doThrow(new SQLException("Create table failed")).when(mockStmt).execute(anyString());

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            bookDAO.initializeTable();

            String output = errContent.toString();
            assertTrue(output.contains("Error creating books table: Create table failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testInitializeTable_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            assertDoesNotThrow(() -> bookDAO.initializeTable());
        }
    }

    // ------------------ findByISBN() ------------------

    @Test
    void testFindByISBN_found() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("id")).thenReturn(1);
        when(mockRs.getString("title")).thenReturn("Title");
        when(mockRs.getString("author")).thenReturn("Author");
        when(mockRs.getString("isbn")).thenReturn("123456");
        when(mockRs.getInt("available")).thenReturn(1);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            Book book = bookDAO.findByISBN("123456");

            assertNotNull(book);
            assertEquals(1, book.getId());
            assertEquals("Title", book.getTitle());
            assertEquals("Author", book.getAuthor());
            assertEquals("123456", book.getIsbn());
            assertTrue(book.isAvailable());
        }
    }

    @Test
    void testFindByISBN_notFound() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            Book book = bookDAO.findByISBN("123456");
            assertNull(book);
        }
    }

    @Test
    void testFindByISBN_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);

            Book book = bookDAO.findByISBN("123456");
            assertNull(book);
        }
    }

    @Test
    void testFindByISBN_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenThrow(new SQLException("Query failed"));

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            Book book = bookDAO.findByISBN("123456");
            assertNull(book);

            String output = errContent.toString();
            assertTrue(output.contains("Error finding book: Query failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    // ------------------ insert() ------------------

    @Test
    void testInsert_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            int id = bookDAO.insert(1, "Author", "123456");
            assertEquals(1, id);
            verify(mockStmt).setInt(1, 1);
            verify(mockStmt).setString(2, "Author");
            verify(mockStmt).setString(3, "123456");
            verify(mockStmt).executeUpdate();
        }
    }

    @Test
    void testInsert_failure() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        doThrow(new SQLException("Insert failed")).when(mockStmt).executeUpdate();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            int id = bookDAO.insert(1, "Author", "123456");
            assertEquals(-1, id);

            String output = errContent.toString();
            assertTrue(output.contains("Error inserting book: Insert failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testInsert_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            int id = bookDAO.insert(1, "Author", "123456");
            assertEquals(-1, id);
        }
    }

    // ------------------ findAll() ------------------

    @Test
    void testFindAll_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.createStatement()).thenReturn(mockStmt);
        when(mockStmt.executeQuery(anyString())).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(1);
        when(mockRs.getString("title")).thenReturn("Title");
        when(mockRs.getString("author")).thenReturn("Author");
        when(mockRs.getString("isbn")).thenReturn("123456");
        when(mockRs.getInt("available")).thenReturn(1);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            List<Book> books = bookDAO.findAll();
            assertEquals(1, books.size());
            assertEquals("Title", books.get(0).getTitle());
        }
    }

    @Test
    void testFindAll_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            List<Book> books = bookDAO.findAll();
            assertTrue(books.isEmpty());
        }
    }

    @Test
    void testFindAll_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        when(mockConn.createStatement()).thenReturn(mockStmt);
        when(mockStmt.executeQuery(anyString())).thenThrow(new SQLException("Query failed"));

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            List<Book> books = bookDAO.findAll();
            assertTrue(books.isEmpty());
            assertTrue(errContent.toString().contains("Error fetching books: Query failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    // ------------------ searchByTitle() ------------------

    @Test
    void testSearchByTitle_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(1);
        when(mockRs.getString("title")).thenReturn("Title");
        when(mockRs.getString("author")).thenReturn("Author");
        when(mockRs.getString("isbn")).thenReturn("123456");
        when(mockRs.getInt("available")).thenReturn(1);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            List<Book> books = bookDAO.searchByTitle("Title");
            assertEquals(1, books.size());
            assertEquals("Title", books.get(0).getTitle());
        }
    }

    @Test
    void testSearchByTitle_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            List<Book> books = bookDAO.searchByTitle("Title");
            assertTrue(books.isEmpty());
        }
    }

    // ------------------ searchByAuthor() ------------------

    @Test
    void testSearchByAuthor_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(1);
        when(mockRs.getString("title")).thenReturn("Title");
        when(mockRs.getString("author")).thenReturn("Author");
        when(mockRs.getString("isbn")).thenReturn("123456");
        when(mockRs.getInt("available")).thenReturn(1);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            List<Book> books = bookDAO.searchByAuthor("Author");
            assertEquals(1, books.size());
            assertEquals("Author", books.get(0).getAuthor());
        }
    }

    @Test
    void testSearchByAuthor_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            List<Book> books = bookDAO.searchByAuthor("Author");
            assertTrue(books.isEmpty());
        }
    }

    // ------------------ searchByISBNPattern() ------------------

    @Test
    void testSearchByISBNPattern_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(1);
        when(mockRs.getString("title")).thenReturn("Title");
        when(mockRs.getString("author")).thenReturn("Author");
        when(mockRs.getString("isbn")).thenReturn("123456");
        when(mockRs.getInt("available")).thenReturn(1);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            List<Book> books = bookDAO.searchByISBNPattern("123");
            assertEquals(1, books.size());
            assertEquals("123456", books.get(0).getIsbn());
        }
    }

    @Test
    void testSearchByISBNPattern_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            List<Book> books = bookDAO.searchByISBNPattern("123");
            assertTrue(books.isEmpty());
        }
    }
}

