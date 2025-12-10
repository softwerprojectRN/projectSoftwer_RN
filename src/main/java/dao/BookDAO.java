package dao;

import model.Book;
import java.sql.*;
import java.util.List;

/**
 * {@code BookDAO} is a Data Access Object (DAO) class responsible for
 * managing database operations related to the {@link Book} model.
 * It extends {@link BaseDAO} to utilize common database operations such as
 * table creation, query execution, and result mapping.
 *
 * <p>This class provides methods to:</p>
 * <ul>
 *     <li>Initialize the "books" table with a foreign key relationship to "media".</li>
 *     <li>Insert a new book record.</li>
 *     <li>Find a book by ISBN.</li>
 *     <li>Retrieve all books.</li>
 *     <li>Search books by title, author, or ISBN pattern.</li>
 * </ul>
 *
 * <p>The class uses a {@code BASE_QUERY} to join the "media" and "books" tables
 * to provide comprehensive information about each book.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * BookDAO bookDAO = new BookDAO();
 * bookDAO.initializeTable();
 * Book book = bookDAO.findByISBN("978-3-16-148410-0");
 * List<Book> allBooks = bookDAO.findAll();
 * List<Book> searchResults = bookDAO.searchByAuthor("J.K. Rowling");
 * }
 * </pre>
 *
 * @author Library
 * @version 1.1
 */
public class BookDAO extends BaseDAO {
    /**
     * Base SQL query joining "media" and "books" tables.
     * Used to simplify SELECT queries.
     */
    private static final String BASE_QUERY =
            "SELECT m.id, m.title, m.available, b.author, b.isbn " +
                    "FROM media m JOIN books b ON m.id = b.id ";

    /**
     * Initializes the "books" table in the database.
     * The table has a foreign key referencing the "media" table.
     * If the table already exists, no changes are made.
     */
    public void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS books (\n" +
                " id INTEGER PRIMARY KEY,\n" +
                " author TEXT NOT NULL,\n" +
                " isbn TEXT NOT NULL UNIQUE,\n" +
                " FOREIGN KEY (id) REFERENCES media(id) ON DELETE CASCADE\n" +
                ");";
        createTable(sql, "Books");
    }

    /**
     * Finds a {@link Book} by its ISBN.
     *
     * @param isbn the ISBN of the book
     * @return the {@link Book} object if found; {@code null} otherwise
     */
    public Book findByISBN(String isbn) {
        return findOne(BASE_QUERY + "WHERE b.isbn = ?", this::mapBook, isbn);
    }

    /**
     * Inserts a new book into the "books" table.
     *
     * @param mediaId the ID of the corresponding media record
     * @param author  the author of the book
     * @param isbn    the unique ISBN of the book
     * @return the generated key for the inserted book; -1 if insertion fails
     */
    public int insert(int mediaId, String author, String isbn) {
        return executeInsert("INSERT INTO books (id, author, isbn) VALUES (?, ?, ?)",
                mediaId, author, isbn);
    }

    /**
     * Retrieves all books from the database.
     *
     * @return a list of {@link Book} objects
     */
    public List<Book> findAll() {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'book'", this::mapBook);
    }

    /**
     * Searches for books whose title contains the specified string.
     *
     * @param title the title pattern to search for
     * @return a list of matching {@link Book} objects
     */
    public List<Book> searchByTitle(String title) {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'book' AND m.title LIKE ?",
                this::mapBook, "%" + title + "%");
    }

    /**
     * Searches for books whose author contains the specified string.
     *
     * @param author the author pattern to search for
     * @return a list of matching {@link Book} objects
     */
    public List<Book> searchByAuthor(String author) {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'book' AND b.author LIKE ?",
                this::mapBook, "%" + author + "%");
    }

    /**
     * Searches for books whose ISBN matches a specified pattern.
     *
     * @param isbn the ISBN pattern to search for
     * @return a list of matching {@link Book} objects
     */
    public List<Book> searchByISBNPattern(String isbn) {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'book' AND b.isbn LIKE ?",
                this::mapBook, "%" + isbn + "%");
    }

    /**
     * Maps a {@link ResultSet} row to a {@link Book} object.
     *
     * @param rs the result set positioned at the current row
     * @return the mapped {@link Book} object
     * @throws SQLException if a database access error occurs
     */
    private Book mapBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("isbn"),
                rs.getInt("available") == 1
        );
    }
}