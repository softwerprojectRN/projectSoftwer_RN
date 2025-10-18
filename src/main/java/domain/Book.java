package domain;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Book {

    private int id;  // معرف الكتاب في الداتابيز
    private String title;
    private String author;
    private String isbn;
    private boolean isAvailable;

    // دالة للاتصال بالداتابيز (SQLite)
    public static Connection connect() {
        String url = "jdbc:sqlite:database.db";  // نفس ملف الداتابيز
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    // كتلة static لإنشاء الجدول تلقائياً أول مرة
    static {
        Connection conn = connect();
        String sql = "CREATE TABLE IF NOT EXISTS books (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " title text NOT NULL,\n"
                + " author text NOT NULL,\n"
                + " isbn text NOT NULL UNIQUE,\n"
                + " available integer NOT NULL DEFAULT 1\n"  // 1 للمتاح، 0 للغير متاح
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("The books table has been created successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // constructor خاص (public) للاستخدام الداخلي (بعد الإضافة أو الاستعلام)
    public Book(int id, String title, String author, String isbn, boolean isAvailable) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.isAvailable = isAvailable;
    }

    // دالة static لإضافة كتاب جديد (Create)
    // ترجع Book object إذا نجح، أو null إذا كان الـ ISBN موجود
    public static Book addBook(String title, String author, String isbn) {
        // تحقق إذا الـ ISBN موجود بالفعل
        Connection conn = connect();
        String checkSql = "SELECT * FROM books WHERE isbn = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("The book is already available (ISBN:" + isbn + ")");
                return null;  // فشل الإضافة
            }
        } catch (SQLException e) {
            System.out.println("Error in ISBN verification" + e.getMessage());
            return null;
        }

        // إدراج الكتاب الجديد
        String sql = "INSERT INTO books (title, author, isbn, available) VALUES (?, ?, ?, 1)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, isbn);
            pstmt.executeUpdate();

            // الحصول على الـ ID المنشأ
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int newId = generatedKeys.getInt(1);
                System.out.println("The book has been added successfully: " + title);
                return new Book(newId, title, author, isbn, true);
            } else {
                System.out.println("Error in retrieving the book ID.");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Error in adding the book: " + e.getMessage());
            return null;
        }
    }

    // دالة static للحصول على جميع الكتب (Read)
    public static List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        Connection conn = connect();
        String sql = "SELECT * FROM books";
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
            System.out.println("Books were retrieved from the database: " + books.size());
        } catch (SQLException e) {
            System.out.println("Error in fetching books: " + e.getMessage());
        }
        return books;
    }

    // دالة لتحديث حالة التوافر (استخدمها في borrow و returnBook)
    public void updateAvailability(boolean available) {
        if (id == 0) {
            System.out.println("A book cannot be updated without an ID (not saved in the database).");
            return;
        }
        Connection conn = connect();
        String sql = "UPDATE books SET available = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, available ? 1 : 0);
            pstmt.setInt(2, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                this.isAvailable = available;
                System.out.println("The book's status has been updated: " + title);
            } else {
                System.out.println("The book has not been updated (ID not found).");
            }
        } catch (SQLException e) {
            System.out.println("Error in updating the book status:  " + e.getMessage());
        }
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public boolean isAvailable() { return isAvailable; }
    public int getId() { return id; }  // getter للـ ID

    public void borrow() {
        if (isAvailable) {
            updateAvailability(false);
        } else {
            System.out.println("The book is not available for borrow.");
        }
    }

    public void returnBook() {
        if (!isAvailable) {
            updateAvailability(true);
        } else {
            System.out.println("The book is already available.");
        }
    }

    /**
     * Provides a user-friendly string representation of the book.
     * @return A string with book details.
     */
    @Override
    public String toString() {
        String available = isAvailable ? "Yes" : "No";
        return "ID: " + id + ", Title: '" + title + "', Author: '" + author + "', ISBN: " + isbn + ", Available: " + available;
    }



}