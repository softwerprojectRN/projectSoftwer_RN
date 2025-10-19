package domain;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Book extends Media {
    private String author;
    private String isbn;

    // كتلة static لإنشاء جدول الكتب
    static {
        Connection conn = connect();
        String sql = "CREATE TABLE IF NOT EXISTS books (\n"
                + " id integer PRIMARY KEY,\n"
                + " author text NOT NULL,\n"
                + " isbn text NOT NULL UNIQUE,\n"
                + " FOREIGN KEY (id) REFERENCES media(id) ON DELETE CASCADE\n"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("The books table has been created successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Constructor
    public Book(int id, String title, String author, String isbn, boolean isAvailable) {
        super(id, title, isAvailable, "book");
        this.author = author;
        this.isbn = isbn;
    }

    // دالة static لإضافة كتاب جديد
    public static Book addBook(String title, String author, String isbn) {
        // تحقق إذا الـ ISBN موجود بالفعل
        Connection conn = connect();
        String checkSql = "SELECT b.* FROM books b JOIN media m ON b.id = m.id WHERE b.isbn = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("Book already exists (ISBN:" + isbn + ")");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Error checking ISBN: " + e.getMessage());
            return null;
        }

        // إدراج في جدول media أولاً
        String mediaSql = "INSERT INTO media (title, media_type, available) VALUES (?, 'book', 1)";
        try (PreparedStatement pstmt = conn.prepareStatement(mediaSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, title);
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int newId = generatedKeys.getInt(1);

                // ثم إدراج في جدول books
                String bookSql = "INSERT INTO books (id, author, isbn) VALUES (?, ?, ?)";
                try (PreparedStatement bookStmt = conn.prepareStatement(bookSql)) {
                    bookStmt.setInt(1, newId);
                    bookStmt.setString(2, author);
                    bookStmt.setString(3, isbn);
                    bookStmt.executeUpdate();

                    System.out.println("Book added successfully: " + title);
                    return new Book(newId, title, author, isbn, true);
                }
            } else {
                System.out.println("Error retrieving media ID.");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Error adding book: " + e.getMessage());
            return null;
        }
    }

    // دالة static للحصول على جميع الكتب
    public static List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        Connection conn = connect();
        String sql = "SELECT m.id, m.title, m.available, b.author, b.isbn " +
                "FROM media m JOIN books b ON m.id = b.id WHERE m.media_type = 'book'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String isbn = rs.getString("isbn");
                boolean available = rs.getInt("available") == 1;
                books.add(new Book(id, title, author, isbn, available));
            }
            System.out.println("Retrieved " + books.size() + " books from database.");
        } catch (SQLException e) {
            System.out.println("Error fetching books: " + e.getMessage());
        }
        return books;
    }

    // Getters
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }

    @Override
    public String toString() {
        return super.toString() + ", Author: '" + author + "', ISBN: " + isbn;
    }
}