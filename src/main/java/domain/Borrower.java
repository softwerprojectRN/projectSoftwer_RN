package domain;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Borrower extends User {

    private List<MediaRecord> borrowedMedia = new ArrayList<>();
    private double fineBalance = 0.0;

    // constructor خاص (protected) للاستخدام الداخلي بعد الـ login
    public Borrower(String username, String passwordHash, String salt) {
        super(username, passwordHash, salt);
        loadBorrowedMedia();  // تحميل المواد المستعارة من الداتابيز
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
                    + " media_id INTEGER NOT NULL,\n"
                    + " media_type TEXT NOT NULL,\n"
                    + " media_title TEXT NOT NULL,\n"
                    + " borrow_date TEXT NOT NULL,\n"
                    + " due_date TEXT NOT NULL,\n"
                    + " returned INTEGER DEFAULT 0,\n"
                    + " return_date TEXT,\n"
                    + " fine REAL DEFAULT 0.0,\n"
                    + " FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,\n"
                    + " FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE\n"
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

    public void loadBorrowedMedia() {
        int userId = getUserId();
        if (userId == -1) return;

        Connection conn = connect();
        if (conn == null) return;

        String sql = "SELECT br.*, m.media_type FROM borrow_records br "
                + "JOIN media m ON br.media_id = m.id "
                + "WHERE br.user_id = ? AND br.returned = 0";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int mediaId = rs.getInt("media_id");
                String mediaType = rs.getString("media_type");
                String title = rs.getString("media_title");
                LocalDate dueDate = LocalDate.parse(rs.getString("due_date"));

                Media media = null;
                if (mediaType.equals("book")) {
                    // استعلام عن تفاصيل الكتاب
                    String bookSql = "SELECT b.author, b.isbn FROM books b WHERE b.id = ?";
                    try (PreparedStatement bookStmt = conn.prepareStatement(bookSql)) {
                        bookStmt.setInt(1, mediaId);
                        ResultSet bookRs = bookStmt.executeQuery();
                        if (bookRs.next()) {
                            media = new Book(mediaId, title, bookRs.getString("author"),
                                    bookRs.getString("isbn"), false);
                        }
                    }
                } else if (mediaType.equals("cd")) {
                    // استعلام عن تفاصيل القرص المدمج
                    String cdSql = "SELECT c.artist, c.genre, c.duration FROM cds c WHERE c.id = ?";
                    try (PreparedStatement cdStmt = conn.prepareStatement(cdSql)) {
                        cdStmt.setInt(1, mediaId);
                        ResultSet cdRs = cdStmt.executeQuery();
                        if (cdRs.next()) {
                            media = new CD(mediaId, title, cdRs.getString("artist"),
                                    cdRs.getString("genre"), cdRs.getInt("duration"), false);
                        }
                    }
                }

                if (media != null) {
                    MediaRecord record = new MediaRecord(media, dueDate, rs.getInt("id"));
                    borrowedMedia.add(record);
                }
            }
            System.out.println("تم تحميل " + borrowedMedia.size() + " مواد مستعارة للمستخدم " + getUsername());
        } catch (SQLException e) {
            System.out.println("Error loading borrowed media: " + e.getMessage());
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

    // =================== Media Borrow/Return ===================
    public void addBorrowRecord(Media media, LocalDate dueDate) {
        MediaRecord record = new MediaRecord(media, dueDate, 0);

        Connection conn = connect();
        if (conn == null) return;

        String sql = "INSERT INTO borrow_records (user_id, media_id, media_type, media_title, borrow_date, due_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, getUserId());
            pstmt.setInt(2, media.getId());
            pstmt.setString(3, media.getMediaType());
            pstmt.setString(4, media.getTitle());
            pstmt.setString(5, LocalDate.now().toString());
            pstmt.setString(6, dueDate.toString());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                record.setRecordId(generatedKeys.getInt(1));
                borrowedMedia.add(record);
            }
        } catch (SQLException e) {
            System.out.println("Error saving borrow record: " + e.getMessage());
        }
    }

    public void removeBorrowRecord(MediaRecord record, double mediaFine) {
        Connection conn = connect();
        if (conn == null) return;

        String sql = "UPDATE borrow_records SET returned = 1, return_date = ?, fine = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, LocalDate.now().toString());
            pstmt.setDouble(2, mediaFine);
            pstmt.setInt(3, record.getRecordId());
            if (pstmt.executeUpdate() > 0) {
                borrowedMedia.remove(record);
                fineBalance += mediaFine;
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

    // =================== MediaRecord Inner Class ===================
    public class MediaRecord {
        private Media media;
        private LocalDate dueDate;
        private int recordId;

        public MediaRecord(Media media, LocalDate dueDate, int recordId) {
            this.media = media;
            this.dueDate = dueDate;
            this.recordId = recordId;
        }

        public Media getMedia() { return media; }
        public LocalDate getDueDate() { return dueDate; }
        public int getRecordId() { return recordId; }
        public void setRecordId(int recordId) { this.recordId = recordId; }

        public boolean isOverdue() { return LocalDate.now().isAfter(dueDate); }

        public long getOverdueDays() {
            if (!isOverdue()) return 0;
            return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        }

        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }
    }



    public void generateOverdueReport() {
        List<MediaRecord> overdueItems = getOverdueMedia(); // استخدم الدالة المعدّلة
        if (overdueItems.isEmpty()) {
            System.out.println("ليس لديك مواد متأخرة.");
            return;
        }

        System.out.println("--- تقرير المواد المتأخرة ---");
        double totalFine = 0.0;

        for (MediaRecord record : overdueItems) {
            Media media = record.getMedia();
            long overdueDays = record.getOverdueDays();
            double finePerDay = BorrowingService.getFinePerDay(media.getMediaType());
            double itemFine = overdueDays * finePerDay;
            totalFine += itemFine;

            String mediaTypeArabic = media.getMediaType().equals("book") ? "كتاب" : "قرص مدمج";
            System.out.printf("- العنوان: '%s', النوع: %s, أيام التأخير: %d, الغرامة: %.2f\n",
                    media.getTitle(), mediaTypeArabic, overdueDays, itemFine);
        }

        System.out.println("---------------------------------");
        System.out.printf("إجمالي الغرامات المتأخرة: %.2f\n", totalFine);
    }


    // =================== Getters ===================
    public List<MediaRecord> getBorrowedMedia() { return new ArrayList<>(borrowedMedia); }

    public List<MediaRecord> getOverdueMedia() { //
        List<MediaRecord> overdue = new ArrayList<>();
        for (MediaRecord record : borrowedMedia) {
            if (record.isOverdue()) overdue.add(record);
        }
        return overdue;
    }




    public double getFineBalance() { return fineBalance; }

    public void setFineBalance(double fineBalance) { this.fineBalance = fineBalance; }

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