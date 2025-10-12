package domain;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Borrower extends User {

    private ArrayList<BookRecord> borrowedBooks = new ArrayList<>();
    private double fineBalance = 0.0;

    // دالة للاتصال بالداتابيز
    private static Connection connect() {
        String url = "jdbc:sqlite:database.db";
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            return null;
        }
    }

    // إنشاء جدول borrower تلقائياً إذا لم يكن موجود
    static {
        Connection conn = connect();
        String sql = "CREATE TABLE IF NOT EXISTS borrower (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " user_id INTEGER NOT NULL,\n"
                + " book_title TEXT NOT NULL,\n"
                + " book_isbn TEXT NOT NULL,\n"
                + " due_date TEXT NOT NULL,\n"
                + " returned INTEGER DEFAULT 0,\n"
                + " fine REAL DEFAULT 0.0,\n"
                + " FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE\n"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("جدول borrower تم إنشاؤه بنجاح.");
        } catch (SQLException e) {
            System.out.println("خطأ في إنشاء جدول borrower: " + e.getMessage());
        }
    }

    public Borrower(String username, String password) {
        super(username, password);
    }

    // استدعاء ID المستخدم من جدول users
    private int getUserId() {
        Connection conn = connect();
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, getUsername());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user id: " + e.getMessage());
        }
        return -1; // إذا لم يوجد
    }

    public void borrowBook(Book book) {
        if (!isLoggedIn()) {
            System.out.println("You must log in first to borrow books.");
            return;
        }
        if (fineBalance > 0) {
            System.out.println("You have unpaid fines. Please pay them before borrowing new books.");
            return;
        }

        if (book.isAvailable()) {
            book.borrow();
            LocalDate dueDate = LocalDate.now().plusDays(28);
            borrowedBooks.add(new BookRecord(book, dueDate));

            // إضافة السجل للداتابيز
            Connection conn = connect();
            String sql = "INSERT INTO borrower (user_id, book_title, book_isbn, due_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, getUserId());
                pstmt.setString(2, book.getTitle());
                pstmt.setString(3, book.getIsbn());
                pstmt.setString(4, dueDate.toString());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error saving borrow record: " + e.getMessage());
            }

            System.out.println("Book '" + book.getTitle() + "' borrowed successfully. Due date: " + dueDate);
        } else {
            System.out.println("Book '" + book.getTitle() + "' is already borrowed.");
        }
    }

    public void returnBook(Book book) {
        BookRecord recordToRemove = null;

        for (BookRecord record : borrowedBooks) {
            if (record.getBook().equals(book)) {
                book.returnBook();
                recordToRemove = record;

                // حساب الغرامة إذا متأخر
                if (record.isOverdue()) {
                    long overdueDays = record.getOverdueDays();
                    double fine = overdueDays * 1.0;
                    fineBalance += fine;
                    System.out.println("Book '" + book.getTitle() + "' is overdue by " + overdueDays +
                            " days. Fine added: " + fine);
                }

                System.out.println("Book '" + book.getTitle() + "' returned successfully.");

                // تحديث الداتابيز
                Connection conn = connect();
                String sql = "UPDATE borrower SET returned = 1, fine = ? WHERE user_id = ? AND book_isbn = ? AND returned = 0";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setDouble(1, fineBalance);
                    pstmt.setInt(2, getUserId());
                    pstmt.setString(3, book.getIsbn());
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("Error updating borrow record: " + e.getMessage());
                }

                break;
            }
        }

        if (recordToRemove != null) {
            borrowedBooks.remove(recordToRemove);
        } else {
            System.out.println("This book was not borrowed by you.");
        }
    }

    // كلاس داخلي لتخزين الكتاب وتاريخ الاسترجاع
    public class BookRecord {
        private Book book;
        private LocalDate dueDate;

        public BookRecord(Book book, LocalDate dueDate) {
            this.book = book;
            this.dueDate = dueDate;
        }

        public Book getBook() {
            return book;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public boolean isOverdue() {
            return LocalDate.now().isAfter(dueDate);
        }

        public long getOverdueDays() {
            if (!isOverdue()) return 0;
            return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        }
    }

    public List<BookRecord> getOverdueBooks() {
        List<BookRecord> overdue = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (BookRecord record : borrowedBooks) {
            if (today.isAfter(record.getDueDate())) {
                overdue.add(record);
            }
        }
        return overdue;
    }
}