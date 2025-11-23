package dao;

import model.Book;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public void initializeTable() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String sql = "CREATE TABLE IF NOT EXISTS books (\n" +
                "  id INTEGER PRIMARY KEY,\n" +
                "  author TEXT NOT NULL,\n" +
                "  isbn TEXT NOT NULL UNIQUE,\n" +
                "  FOREIGN KEY (id) REFERENCES media(id) ON DELETE CASCADE\n" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Books table created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating books table: " + e.getMessage());
        }
    }

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
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getInt("available") == 1
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding book: " + e.getMessage());
        }
        return null;
    }

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
            System.err.println("Error inserting book: " + e.getMessage());
            return -1;
        }
    }

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
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getInt("available") == 1
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching books: " + e.getMessage());
        }
        return books;
    }

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
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getInt("available") == 1
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error searching books by title: " + e.getMessage());
        }
        return books;
    }

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
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getInt("available") == 1
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error searching books by author: " + e.getMessage());
        }
        return books;
    }

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
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getInt("available") == 1
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error searching books by ISBN: " + e.getMessage());
        }
        return books;
    }
}