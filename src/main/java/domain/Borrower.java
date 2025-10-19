package domain;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Borrower extends User {

    private List<BookRecord> borrowedBooks = new ArrayList<>();
    private double fineBalance = 0.0;

    // constructor خاص (protected) للاستخدام الداخلي بعد الـ login (يتوافق مع User)
    public Borrower(String username, String passwordHash, String salt) {
        super(username, passwordHash, salt);
        loadBorrowedBooks();  // تحميل الكتب المستعارة من الداتابيز
        loadFineBalance();    // تحميل رصيد الغرامات الكلي
    }

    // دالة للاتصال بالداتابيز
    public static Connection connect() {
        String url = "jdbc:sqlite:database.db";
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            return null;
        }
    }

    // إنشاء جدول borrow_records و user_fines تلقائياً إذا لم يكن موجود
    static {
        Connection conn = connect();
        if (conn == null) {
            System.out.println("Database not available, tables not created.");
        } else {
            String borrowSql = "CREATE TABLE IF NOT EXISTS borrow_records (\n"
                    + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + " user_id INTEGER NOT NULL,\n"
                    + " book_id INTEGER NOT NULL,\n"
                    + " book_title TEXT NOT NULL,\n"
                    + " book_isbn TEXT NOT NULL,\n"
                    + " borrow_date TEXT NOT NULL,\n"
                    + " due_date TEXT NOT NULL,\n"
                    + " returned INTEGER DEFAULT 0,\n"
                    + " return_date TEXT,\n"
                    + " fine REAL DEFAULT 0.0,\n"
                    + " FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,\n"
                    + " FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE\n"
                    + ");";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(borrowSql);
                System.out.println("جدول borrow_records تم إنشاؤه بنجاح.");
            } catch (SQLException e) {
                System.out.println("خطأ في إنشاء جدول borrow_records: " + e.getMessage());
            }

            String fineSql = "CREATE TABLE IF NOT EXISTS user_fines (\n"
                    + " user_id INTEGER PRIMARY KEY,\n"
                    + " total_fine REAL DEFAULT 0.0,\n"
                    + " FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE\n"
                    + ");";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(fineSql);
                System.out.println("جدول user_fines تم إنشاؤه بنجاح.");
            } catch (SQLException e) {
                System.out.println("خطأ في إنشاء جدول user_fines: " + e.getMessage());
            }
        }
    }

    // =================== دوال المساعدة ===================
    public int getUserId() {
        Connection conn = connect();
        if (conn == null) return -1;
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
        return -1;
    }

    public void loadBorrowedBooks() {
        int userId = getUserId();
        if (userId == -1) return;

        Connection conn = connect();
        if (conn == null) return;

        String sql = "SELECT br.*, b.id as book_id, b.title, b.isbn FROM borrow_records br "
                + "JOIN books b ON br.book_id = b.id "
                + "WHERE br.user_id = ? AND br.returned = 0";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("book_title");
                String isbn = rs.getString("book_isbn");
                LocalDate dueDate = LocalDate.parse(rs.getString("due_date"));
                Book book = new Book(bookId, title, null, isbn, false);
                BookRecord record = new BookRecord(book, dueDate, rs.getInt("id"));
                borrowedBooks.add(record);
            }
            System.out.println("تم تحميل " + borrowedBooks.size() + " كتب مستعارة للمستخدم " + getUsername());
        } catch (SQLException e) {
            System.out.println("Error loading borrowed books: " + e.getMessage());
        }
    }

    public void loadFineBalance() {
        int userId = getUserId();
        if (userId == -1) return;

        Connection conn = connect();
        if (conn == null) return;

        String sql = "SELECT total_fine FROM user_fines WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                fineBalance = rs.getDouble("total_fine");
            } else {
                String insertSql = "INSERT INTO user_fines (user_id, total_fine) VALUES (?, 0.0)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.executeUpdate();
                } catch (SQLException ex) {
                    System.out.println("Error initializing fines: " + ex.getMessage());
                }
                fineBalance = 0.0;
            }
        } catch (SQLException e) {
            System.out.println("Error loading fine balance: " + e.getMessage());
            fineBalance = 0.0;
        }
    }

    public void saveFineBalance() {
        int userId = getUserId();
        if (userId == -1) return;

        Connection conn = connect();
        if (conn == null) return;

        String sql = "UPDATE user_fines SET total_fine = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, fineBalance);
            pstmt.setInt(2, userId);
            if (pstmt.executeUpdate() == 0) {
                String insertSql = "INSERT INTO user_fines (user_id, total_fine) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setDouble(2, fineBalance);
                    insertStmt.executeUpdate();
                } catch (SQLException ex) {
                    System.out.println("Error saving fines: " + ex.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Error saving fine balance: " + e.getMessage());
        }
    }

    // =================== Book Borrow/Return ===================
    public void addBorrowRecord(Book book, LocalDate dueDate) {
        BookRecord record = new BookRecord(book, dueDate, 0);

        Connection conn = connect();
        if (conn == null) return;

        String sql = "INSERT INTO borrow_records (user_id, book_id, book_title, book_isbn, borrow_date, due_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, getUserId());
            pstmt.setInt(2, book.getId());
            pstmt.setString(3, book.getTitle());
            pstmt.setString(4, book.getIsbn());
            pstmt.setString(5, LocalDate.now().toString());
            pstmt.setString(6, dueDate.toString());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                record.setRecordId(generatedKeys.getInt(1));
                borrowedBooks.add(record);
            }
        } catch (SQLException e) {
            System.out.println("Error saving borrow record: " + e.getMessage());
        }
    }

    public void removeBorrowRecord(BookRecord record, double bookFine) {
        Connection conn = connect();
        if (conn == null) return;

        String sql = "UPDATE borrow_records SET returned = 1, return_date = ?, fine = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, LocalDate.now().toString());
            pstmt.setDouble(2, bookFine);
            pstmt.setInt(3, record.getRecordId());
            if (pstmt.executeUpdate() > 0) {
                borrowedBooks.remove(record);
                fineBalance += bookFine;
                saveFineBalance();
            }
        } catch (SQLException e) {
            System.out.println("Error updating borrow record: " + e.getMessage());
        }
    }

    public boolean payFine(double amount) {
        if (amount <= 0 || amount > fineBalance) {
            System.out.println("المبلغ غير صالح.");
            return false;
        }
        fineBalance -= amount;
        saveFineBalance();
        System.out.println("تم دفع " + amount + ". الرصيد الجديد: " + fineBalance);
        return true;
    }

    // =================== BookRecord Inner Class ===================
    public class BookRecord {
        private Book book;
        private LocalDate dueDate;
        private int recordId;

        public BookRecord(Book book, LocalDate dueDate, int recordId) {
            this.book = book;
            this.dueDate = dueDate;
            this.recordId = recordId;
        }

        public Book getBook() { return book; }
        public LocalDate getDueDate() { return dueDate; }
        public int getRecordId() { return recordId; }
        public void setRecordId(int recordId) { this.recordId = recordId; }

        public boolean isOverdue() { return LocalDate.now().isAfter(dueDate); }

        public long getOverdueDays() {
            if (!isOverdue()) return 0;
            return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        }
    }

    // =================== Getters ===================
    public List<BookRecord> getBorrowedBooks() { return new ArrayList<>(borrowedBooks); }

    public List<BookRecord> getOverdueBooks() {
        List<BookRecord> overdue = new ArrayList<>();
        for (BookRecord record : borrowedBooks) {
            if (record.isOverdue()) overdue.add(record);
        }
        return overdue;
    }
    public double getFineBalance() { return fineBalance; }

    public void setFineBalance(double fineBalance) { this.fineBalance = fineBalance;
    }



    // Method to get all users with overdue books
    public static List<UserWithOverdueBooks> getUsersWithOverdueBooks() {
        List<UserWithOverdueBooks> usersWithOverdueBooks = new ArrayList<>();
        Connection conn = connect();
        if (conn == null) return usersWithOverdueBooks;

        String sql = "SELECT u.id, u.username, COUNT(br.id) as overdue_count FROM users u " +
                "JOIN borrow_records br ON u.id = br.user_id " +
                "WHERE br.returned = 0 AND br.due_date < date('now') " +
                "GROUP BY u.id, u.username";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("id");
                String username = rs.getString("username");
                int overdueCount = rs.getInt("overdue_count");

                usersWithOverdueBooks.add(new UserWithOverdueBooks(userId, username, overdueCount));
            }
        } catch (SQLException e) {
            System.out.println("Error getting users with overdue books: " + e.getMessage());
        }

        return usersWithOverdueBooks;
    }

    // Inner class to represent a user with overdue books
    public static class UserWithOverdueBooks {
        private int userId;
        private String username;
        private int overdueCount;

        public UserWithOverdueBooks(int userId, String username, int overdueCount) {
            this.userId = userId;
            this.username = username;
            this.overdueCount = overdueCount;
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public int getOverdueCount() { return overdueCount; }
    }

    // ... (rest of the existing code)
}
