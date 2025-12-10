
import dao.BookDAO;
import dao.DatabaseConnection;
import model.Book;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    private BookDAO bookDAO;
    private MockedStatic<DatabaseConnection> mockedDatabaseConnection;

    @BeforeEach
    void setUp() {
        // Mock the static DatabaseConnection.getConnection() method
        mockedDatabaseConnection = mockStatic(DatabaseConnection.class);
        mockedDatabaseConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
        bookDAO = new BookDAO();
    }

    @AfterEach
    void tearDown() {
        if (mockedDatabaseConnection != null) {
            mockedDatabaseConnection.close();
        }
    }

    @Test
    void testInitializeTable() throws SQLException {
        // Setup
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        // Execute
        bookDAO.initializeTable();

        // Verify
        verify(mockStatement).execute(contains("CREATE TABLE IF NOT EXISTS books"));
    }

    @Test
    void testInitializeTableWithSQLException() throws SQLException {
        // Setup
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        doThrow(new SQLException("Table creation failed")).when(mockStatement).execute(anyString());

        // Execute - should not throw an exception
        bookDAO.initializeTable();

        // Verify
        verify(mockStatement).execute(contains("CREATE TABLE IF NOT EXISTS books"));
    }

    @Test
    void testFindByISBN() throws SQLException {
        // Setup
        String isbn = "1234567890";
        int expectedId = 1;
        String expectedTitle = "Test Book";
        String expectedAuthor = "Test Author";
        boolean expectedAvailable = true;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(expectedId);
        when(mockResultSet.getString("title")).thenReturn(expectedTitle);
        when(mockResultSet.getString("author")).thenReturn(expectedAuthor);
        when(mockResultSet.getString("isbn")).thenReturn(isbn);
        when(mockResultSet.getInt("available")).thenReturn(1);

        // Execute
        Book result = bookDAO.findByISBN(isbn);

        // Verify
        assertNotNull(result);
        assertEquals(expectedId, result.getId());
        assertEquals(expectedTitle, result.getTitle());
        assertEquals(expectedAuthor, result.getAuthor());
        assertEquals(isbn, result.getIsbn());
        assertEquals(expectedAvailable, result.isAvailable());

        verify(mockPreparedStatement).setObject(1, isbn);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testFindByISBNNotFound() throws SQLException {
        // Setup
        String isbn = "nonexistent";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Execute
        Book result = bookDAO.findByISBN(isbn);

        // Verify
        assertNull(result);
        verify(mockPreparedStatement).setObject(1, isbn);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testInsert() throws SQLException {
        // Setup
        int mediaId = 1;
        String author = "Test Author";
        String isbn = "1234567890";
        int expectedId = 2;

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(expectedId);

        // Execute
        int result = bookDAO.insert(mediaId, author, isbn);

        // Verify
        assertEquals(expectedId, result);
        verify(mockPreparedStatement).setObject(1, mediaId);
        verify(mockPreparedStatement).setObject(2, author);
        verify(mockPreparedStatement).setObject(3, isbn);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testInsertFailure() throws SQLException {
        // Setup
        int mediaId = 1;
        String author = "Test Author";
        String isbn = "1234567890";

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Execute
        int result = bookDAO.insert(mediaId, author, isbn);

        // Verify
        assertEquals(-1, result);
        verify(mockPreparedStatement).setObject(1, mediaId);
        verify(mockPreparedStatement).setObject(2, author);
        verify(mockPreparedStatement).setObject(3, isbn);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testFindAll() throws SQLException {
        // Setup
        Book[] expectedBooks = {
                new Book(1, "Book 1", "Author 1", "1111111111", true),
                new Book(2, "Book 2", "Author 2", "2222222222", false),
                new Book(3, "Book 3", "Author 3", "3333333333", true)
        };

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2, 3);
        when(mockResultSet.getString("title")).thenReturn("Book 1", "Book 2", "Book 3");
        when(mockResultSet.getString("author")).thenReturn("Author 1", "Author 2", "Author 3");
        when(mockResultSet.getString("isbn")).thenReturn("1111111111", "2222222222", "3333333333");
        when(mockResultSet.getInt("available")).thenReturn(1, 0, 1);

        // Execute
        List<Book> result = bookDAO.findAll();

        // Verify
        assertNotNull(result);
        assertEquals(3, result.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(expectedBooks[i].getId(), result.get(i).getId());
            assertEquals(expectedBooks[i].getTitle(), result.get(i).getTitle());
            assertEquals(expectedBooks[i].getAuthor(), result.get(i).getAuthor());
            assertEquals(expectedBooks[i].getIsbn(), result.get(i).getIsbn());
            assertEquals(expectedBooks[i].isAvailable(), result.get(i).isAvailable());
        }

        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testSearchByTitle() throws SQLException {
        // Setup
        String title = "Test";
        Book[] expectedBooks = {
                new Book(1, "Test Book 1", "Author 1", "1111111111", true),
                new Book(2, "Test Book 2", "Author 2", "2222222222", false)
        };

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("title")).thenReturn("Test Book 1", "Test Book 2");
        when(mockResultSet.getString("author")).thenReturn("Author 1", "Author 2");
        when(mockResultSet.getString("isbn")).thenReturn("1111111111", "2222222222");
        when(mockResultSet.getInt("available")).thenReturn(1, 0);

        // Execute
        List<Book> result = bookDAO.searchByTitle(title);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        for (int i = 0; i < 2; i++) {
            assertEquals(expectedBooks[i].getId(), result.get(i).getId());
            assertEquals(expectedBooks[i].getTitle(), result.get(i).getTitle());
            assertEquals(expectedBooks[i].getAuthor(), result.get(i).getAuthor());
            assertEquals(expectedBooks[i].getIsbn(), result.get(i).getIsbn());
            assertEquals(expectedBooks[i].isAvailable(), result.get(i).isAvailable());
        }

        verify(mockPreparedStatement).setObject(1, "%" + title + "%");
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testSearchByAuthor() throws SQLException {
        // Setup
        String author = "Test";
        Book expectedBook = new Book(1, "Test Book", "Test Author", "1111111111", true);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("title")).thenReturn("Test Book");
        when(mockResultSet.getString("author")).thenReturn("Test Author");
        when(mockResultSet.getString("isbn")).thenReturn("1111111111");
        when(mockResultSet.getInt("available")).thenReturn(1);

        // Execute
        List<Book> result = bookDAO.searchByAuthor(author);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedBook.getId(), result.get(0).getId());
        assertEquals(expectedBook.getTitle(), result.get(0).getTitle());
        assertEquals(expectedBook.getAuthor(), result.get(0).getAuthor());
        assertEquals(expectedBook.getIsbn(), result.get(0).getIsbn());
        assertEquals(expectedBook.isAvailable(), result.get(0).isAvailable());

        verify(mockPreparedStatement).setObject(1, "%" + author + "%");
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testSearchByISBNPattern() throws SQLException {
        // Setup
        String isbnPattern = "123";
        Book[] expectedBooks = {
                new Book(1, "Book 1", "Author 1", "1234567890", true),
                new Book(2, "Book 2", "Author 2", "1234567891", false)
        };

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("title")).thenReturn("Book 1", "Book 2");
        when(mockResultSet.getString("author")).thenReturn("Author 1", "Author 2");
        when(mockResultSet.getString("isbn")).thenReturn("1234567890", "1234567891");
        when(mockResultSet.getInt("available")).thenReturn(1, 0);

        // Execute
        List<Book> result = bookDAO.searchByISBNPattern(isbnPattern);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        for (int i = 0; i < 2; i++) {
            assertEquals(expectedBooks[i].getId(), result.get(i).getId());
            assertEquals(expectedBooks[i].getTitle(), result.get(i).getTitle());
            assertEquals(expectedBooks[i].getAuthor(), result.get(i).getAuthor());
            assertEquals(expectedBooks[i].getIsbn(), result.get(i).getIsbn());
            assertEquals(expectedBooks[i].isAvailable(), result.get(i).isAvailable());
        }

        verify(mockPreparedStatement).setObject(1, "%" + isbnPattern + "%");
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testSearchMethodsReturnEmptyList() throws SQLException {
        // Setup
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Execute
        List<Book> result1 = bookDAO.searchByTitle("Nonexistent");
        List<Book> result2 = bookDAO.searchByAuthor("Nonexistent");
        List<Book> result3 = bookDAO.searchByISBNPattern("Nonexistent");

        // Verify
        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
        assertTrue(result3.isEmpty());
    }
}