package dao;
import java.util.logging.Logger;
import model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for BorrowRecord entity.
 * Manages database operations for borrowing records including tracking borrowed items,
 * returns, and overdue calculations.
 *
 * @author Library Management System
 * @version 1.0
 */
public class BorrowRecordDAO {
    private static final Logger logger = Logger.getLogger(BorrowRecordDAO.class.getName());
    /**
     * Initializes the borrow_records table in the database.
     * Creates the table with foreign key relationships to users and media tables.
     */
    public void initializeTable() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String sql = "CREATE TABLE IF NOT EXISTS borrow_records (\n" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                " user_id INTEGER NOT NULL,\n" +
                " media_id INTEGER NOT NULL,\n" +
                " media_type TEXT NOT NULL,\n" +
                " media_title TEXT NOT NULL,\n" +
                " borrow_date TEXT NOT NULL,\n" +
                " due_date TEXT NOT NULL,\n" +
                " returned INTEGER DEFAULT 0,\n" +
                " return_date TEXT,\n" +
                " fine REAL DEFAULT 0.0,\n" +
                " FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,\n" +
                " FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE\n" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("Borrow records table created successfully.");
        } catch (SQLException e) {
            logger.severe("Error creating borrow_records table: " + e.getMessage());
        }
    }

    /**
     * Inserts a new borrow record into the database.
     *
     * @param userId the ID of the user borrowing the media
     * @param mediaId the ID of the media being borrowed
     * @param mediaType the type of media (book or cd)
     * @param mediaTitle the title of the media
     * @param borrowDate the date the media was borrowed
     * @param dueDate the date the media is due to be returned
     * @return the generated record ID if successful, -1 otherwise
     */
    public int insert(int userId, int mediaId, String mediaType, String mediaTitle,
                      LocalDate borrowDate, LocalDate dueDate) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return -1;

        String sql = "INSERT INTO borrow_records (user_id, media_id, media_type, media_title, " +
                "borrow_date, due_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, mediaId);
            pstmt.setString(3, mediaType);
            pstmt.setString(4, mediaTitle);
            pstmt.setString(5, borrowDate.toString());
            pstmt.setString(6, dueDate.toString());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            logger.severe("Error inserting borrow record: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Marks a borrow record as returned.
     *
     * @param recordId the ID of the borrow record
     * @param returnDate the date the media was returned
     * @param fine the fine amount (if any) for late return
     * @return true if update was successful, false otherwise
     */
    public boolean markAsReturned(int recordId, LocalDate returnDate, double fine) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        String sql = "UPDATE borrow_records SET returned = 1, return_date = ?, fine = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, returnDate.toString());
            pstmt.setDouble(2, fine);
            pstmt.setInt(3, recordId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.severe("Error marking record as returned: " + e.getMessage());
            return false;
        }
    }

    /**
     * Finds all active (not returned) borrow records for a specific user.
     *
     * @param userId the ID of the user
     * @return List of MediaRecord objects representing active borrows
     */
    public List<MediaRecord> findActiveByUserId(int userId) {
        List<MediaRecord> records = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return records;

        String sql = "SELECT br.*, m.media_type FROM borrow_records br " +
                "JOIN media m ON br.media_id = m.id " +
                "WHERE br.user_id = ? AND br.returned = 0";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int mediaId = rs.getInt("media_id");
                String mediaType = rs.getString("media_type");
                String title = rs.getString("media_title");
                LocalDate dueDate = LocalDate.parse(rs.getString("due_date"));
                int recordId = rs.getInt("id");

                Media media = null;

                if (mediaType.equals("book")) {
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
                    records.add(new MediaRecord(recordId, media, dueDate));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error loading borrowed media: " + e.getMessage());
        }
        return records;
    }

    /**
     * Retrieves all users who have overdue books.
     *
     * @return List of UserWithOverdueBooks objects containing user information and overdue count
     */
    public List<UserWithOverdueBooks> getUsersWithOverdueBooks() {
        List<UserWithOverdueBooks> usersWithOverdueBooks = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return usersWithOverdueBooks;

        String sql = "SELECT u.id, u.username, COUNT(br.id) as overdue_count " +
                "FROM users u " +
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
            logger.severe("Error getting users with overdue books: " + e.getMessage());
        }
        return usersWithOverdueBooks;
    }

    /**
     * Finds all overdue borrow records for a specific user.
     *
     * @param userId the ID of the user
     * @return List of overdue MediaRecord objects
     */
    public List<MediaRecord> findOverdueByUserId(int userId) {
        List<MediaRecord> overdueRecords = new ArrayList<>();
        List<MediaRecord> allRecords = findActiveByUserId(userId);

        for (MediaRecord record : allRecords) {
            if (record.isOverdue()) {
                overdueRecords.add(record);
            }
        }
        return overdueRecords;
    }

    /**
     * Counts the number of active (not returned) borrow records for a user.
     *
     * @param userId the ID of the user
     * @return the count of active borrow records
     */
    public int countActiveByUserId(int userId) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return 0;

        String sql = "SELECT COUNT(*) as count FROM borrow_records " +
                "WHERE user_id = ? AND returned = 0";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            logger.severe("Error counting active records: " + e.getMessage());
        }
        return 0;
    }
}