package dao;

import model.Book;
import java.sql.*;
import java.util.List;

public class BookDAO extends BaseDAO {
    private static final String BASE_QUERY =
            "SELECT m.id, m.title, m.available, b.author, b.isbn " +
                    "FROM media m JOIN books b ON m.id = b.id ";

    public void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS books (\n" +
                " id INTEGER PRIMARY KEY,\n" +
                " author TEXT NOT NULL,\n" +
                " isbn TEXT NOT NULL UNIQUE,\n" +
                " FOREIGN KEY (id) REFERENCES media(id) ON DELETE CASCADE\n" +
                ");";
        createTable(sql, "Books");
    }

    public Book findByISBN(String isbn) {
        return findOne(BASE_QUERY + "WHERE b.isbn = ?", this::mapBook, isbn);
    }

    public int insert(int mediaId, String author, String isbn) {
        return executeInsert("INSERT INTO books (id, author, isbn) VALUES (?, ?, ?)",
                mediaId, author, isbn);
    }

    public List<Book> findAll() {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'book'", this::mapBook);
    }

    public List<Book> searchByTitle(String title) {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'book' AND m.title LIKE ?",
                this::mapBook, "%" + title + "%");
    }

    public List<Book> searchByAuthor(String author) {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'book' AND b.author LIKE ?",
                this::mapBook, "%" + author + "%");
    }

    public List<Book> searchByISBNPattern(String isbn) {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'book' AND b.isbn LIKE ?",
                this::mapBook, "%" + isbn + "%");
    }

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