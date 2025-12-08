package dao;

import model.Book;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Data Access Object for Book entity.
 * Manages database operations for books including CRUD operations and search functionality.
 *
 * @author Library Management System
 * @version 1.0
 */
public class BookDAO {
    private static final String COL_TITLE = "title";
    private static final String COL_AUTHOR = "author";
    private static final String COL_AVAILABLE = "available";
    private static final String COL_ISBN = "isbn";
    private static final String COL_ID = "id";
    private static final Logger logger = Logger.getLogger(BookDAO.class.getName());
    /**
     * Initializes the books table in the database.
     * Creates the table with a foreign key relationship to the media table.
     */
    public void initializeTable() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String sql = "CREATE TABLE IF NOT EXISTS books (\n" +
                " id INTEGER PRIMARY KEY,\n" +
                " author TEXT NOT NULL,\n" +
                " isbn TEXT NOT NULL UNIQUE,\n" +
                " FOREIGN KEY (id) REFERENCES media(id) ON DELETE CASCADE\n" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("Books table created successfully.");
        } catch (SQLException e) {
            logger.severe("Error creating books table: " + e.getMessage());
        }
    }

    /**
     * Finds a book by its ISBN.
     *
     * @param isbn the ISBN to search for
     * @return Book object if found, null otherwise
     */
    public Book findByISBN(String isbn) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        String sql = "SELECT m.id, m.title, m.available, b.author, b.isbn " +
                "FROM media m JOIN books b ON m.id = b.id WHERE b.isbn = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Book(
                        rs.getInt(COL_ID),
                        rs.getString(COL_TITLE),
                        rs.getString(COL_AUTHOR),
                        rs.getString(COL_ISBN),
                        rs.getInt(COL_AVAILABLE) == 1
                );
            }
        } catch (SQLException e) {
            logger.severe("Error finding book: " + e.getMessage());
        }
        return null;
    }

    /**
     * Inserts a new book record into the database.
     *
     * @param mediaId the ID from the media table
     * @param author the book's author
     * @param isbn the book's ISBN
     * @return the media ID if successful, -1 otherwise
     */
    public int insert(int mediaId, String author, String isbn) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return -1;

        String sql = "INSERT INTO books (id, author, isbn) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mediaId);
            pstmt.setString(2, author);
            pstmt.setString(3, isbn);
            pstmt.executeUpdate();
            return mediaId;
        } catch (SQLException e) {
            logger.severe("Error inserting book: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Retrieves all books from the database.
     *
     * @return List of all Book objects
     */
    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return books;

        String sql = "SELECT m.id, m.title, m.available, b.author, b.isbn " +
                "FROM media m JOIN books b ON m.id = b.id WHERE m.media_type = 'book'";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(new Book(
                        rs.getInt(COL_ID),
                        rs.getString(COL_TITLE),
                        rs.getString(COL_AUTHOR),
                        rs.getString(COL_ISBN),
                        rs.getInt(COL_AVAILABLE) == 1
                ));
            }
        } catch (SQLException e) {
            logger.severe("Error fetching books: " + e.getMessage());
        }
        return books;
    }

    /**
     * Searches for books by title using pattern matching.
     *
     * @param title the title or partial title to search for
     * @return List of matching Book objects
     */
    public List<Book> searchByTitle(String title) {
        List<Book> books = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return books;

        String sql = "SELECT m.id, m.title, m.available, b.author, b.isbn " +
                "FROM media m JOIN books b ON m.id = b.id " +
                "WHERE m.media_type = 'book' AND m.title LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + title + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                books.add(new Book(
                        rs.getInt(COL_ID),
                        rs.getString(COL_TITLE),
                        rs.getString(COL_AUTHOR),
                        rs.getString(COL_ISBN),
                        rs.getInt(COL_AVAILABLE) == 1
                ));
            }
        } catch (SQLException e) {
            logger.severe("Error searching books by title: " + e.getMessage());
        }
        return books;
    }

    /**
     * Searches for books by author using pattern matching.
     *
     * @param author the author name or partial name to search for
     * @return List of matching Book objects
     */
    public List<Book> searchByAuthor(String author) {
        List<Book> books = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return books;

        String sql = "SELECT m.id, m.title, m.available, b.author, b.isbn " +
                "FROM media m JOIN books b ON m.id = b.id " +
                "WHERE m.media_type = 'book' AND b.author LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + author + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                books.add(new Book(
                        rs.getInt(COL_ID),
                        rs.getString(COL_TITLE),
                        rs.getString(COL_AUTHOR),
                        rs.getString(COL_ISBN),
                        rs.getInt(COL_AVAILABLE) == 1
                ));
            }
        } catch (SQLException e) {
            logger.severe("Error searching books by author: " + e.getMessage());
        }
        return books;
    }

    /**
     * Searches for books by ISBN pattern.
     *
     * @param isbn the ISBN or partial ISBN to search for
     * @return List of matching Book objects
     */
    public List<Book> searchByISBNPattern(String isbn) {
        List<Book> books = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return books;

        String sql = "SELECT m.id, m.title, m.available, b.author, b.isbn " +
                "FROM media m JOIN books b ON m.id = b.id " +
                "WHERE m.media_type = 'book' AND b.isbn LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + isbn + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                books.add(new Book(
                        rs.getInt(COL_ID),
                        rs.getString(COL_TITLE),
                        rs.getString(COL_AUTHOR),
                        rs.getString(COL_ISBN),
                        rs.getInt(COL_AVAILABLE) == 1
                ));
            }
        } catch (SQLException e) {
            logger.severe("Error searching books by ISBN: " + e.getMessage());
        }
        return books;
    }
}