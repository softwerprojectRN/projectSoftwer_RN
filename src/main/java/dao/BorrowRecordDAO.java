package dao;

import model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


/**
 * {@code BorrowRecordDAO} is a Data Access Object (DAO) class responsible for
 * managing database operations related to borrow records of media items
 * (e.g., books, CDs) by users.
 *
 * <p>It extends {@link BaseDAO} to reuse common database operations such as
 * table creation, query execution, and result mapping.</p>
 *
 * <p>This class provides methods to:</p>
 * <ul>
 *     <li>Create the "borrow_records" table with necessary foreign key constraints.</li>
 *     <li>Insert new borrow records.</li>
 *     <li>Mark borrow records as returned and record fines.</li>
 *     <li>Retrieve active borrow records for a specific user.</li>
 *     <li>Retrieve overdue borrow records and users with overdue books.</li>
 *     <li>Count active borrow records for a specific user.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * BorrowRecordDAO borrowDAO = new BorrowRecordDAO();
 * borrowDAO.initializeTable();
 * int recordId = borrowDAO.insert(userId, mediaId, "book", "Java Programming",
 *                                  LocalDate.now(), LocalDate.now().plusDays(14));
 * borrowDAO.markAsReturned(recordId, LocalDate.now(), 0.0);
 * List<MediaRecord> active = borrowDAO.findActiveByUserId(userId);
 * List<UserWithOverdueBooks> overdueUsers = borrowDAO.getUsersWithOverdueBooks();
 * }
 * </pre>
 *
 * @author Library
 * @version 1.1
 *
 */
public class BorrowRecordDAO extends BaseDAO {
    /**
     * Initializes the "borrow_records" table in the database.
     * If the table already exists, no changes are made.
     * The table includes foreign keys referencing "users" and "media" tables.
     */
    public void initializeTable() {
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
        createTable(sql, "Borrow records");
    }
    /**
     * Inserts a new borrow record for a user.
     *
     * @param userId     the ID of the user borrowing the media
     * @param mediaId    the ID of the media item
     * @param mediaType  the type of media (e.g., "book", "cd")
     * @param mediaTitle the title of the media
     * @param borrowDate the date when the media was borrowed
     * @param dueDate    the due date for returning the media
     * @return the generated ID of the inserted borrow record; -1 if insertion fails
     */
    public int insert(int userId, int mediaId, String mediaType, String mediaTitle,
                      LocalDate borrowDate, LocalDate dueDate) {
        return executeInsert(
                "INSERT INTO borrow_records (user_id, media_id, media_type, media_title, borrow_date, due_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                userId, mediaId, mediaType, mediaTitle, borrowDate.toString(), dueDate.toString()
        );
    }
    /**
     * Marks a borrow record as returned and records any associated fine.
     *
     * @param recordId   the ID of the borrow record
     * @param returnDate the actual return date
     * @param fine       the fine amount for late return
     * @return {@code true} if the update was successful; {@code false} otherwise
     */
    public boolean markAsReturned(int recordId, LocalDate returnDate, double fine) {
        return executeUpdate("UPDATE borrow_records SET returned = 1, return_date = ?, fine = ? WHERE id = ?",
                returnDate.toString(), fine, recordId);
    }
    /**
     * Retrieves all active (not yet returned) borrow records for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of {@link MediaRecord} representing active borrow records
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
                Media media = fetchMediaDetails(conn, rs.getInt("media_id"), rs.getString("media_type"));
                if (media != null) {
                    records.add(new MediaRecord(
                            rs.getInt("id"),
                            media,
                            LocalDate.parse(rs.getString("due_date"))
                    ));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error loading borrowed media: " + e.getMessage());
        }
        return records;
    }
    /**
     * Fetches media details based on type.
     *
     * @param conn      the database connection
     * @param mediaId   the ID of the media
     * @param mediaType the type of media
     * @return a {@link Media} object if found; {@code null} otherwise
     * @throws SQLException if a database access error occurs
     */
    private Media fetchMediaDetails(Connection conn, int mediaId, String mediaType) throws SQLException {
        if ("book".equals(mediaType)) {
            return fetchBookDetails(conn, mediaId);
        } else if ("cd".equals(mediaType)) {
            return fetchCDDetails(conn, mediaId);
        }
        return null;
    }

    public Book fetchBookDetails(Connection conn, int mediaId) throws SQLException {
        String sql = "SELECT m.title, b.author, b.isbn FROM media m " +
                "JOIN books b ON m.id = b.id WHERE m.id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mediaId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Book(mediaId, rs.getString("title"),
                        rs.getString("author"), rs.getString("isbn"), false);
            }
        }
        return null;
    }

    public CD fetchCDDetails(Connection conn, int mediaId) throws SQLException {
        String sql = "SELECT m.title, c.artist, c.genre, c.duration FROM media m " +
                "JOIN cds c ON m.id = c.id WHERE m.id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mediaId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new CD(mediaId, rs.getString("title"), rs.getString("artist"),
                        rs.getString("genre"), rs.getInt("duration"), false);
            }
        }
        return null;
    }
    /**
     * Retrieves users who have overdue borrow records.
     *
     * @return a list of {@link UserWithOverdueBooks} representing users with overdue media
     */
    public List<UserWithOverdueBooks> getUsersWithOverdueBooks() {
        String sql = "SELECT u.id, u.username, COUNT(br.id) as overdue_count " +
                "FROM users u JOIN borrow_records br ON u.id = br.user_id " +
                "WHERE br.returned = 0 AND br.due_date < date('now') " +
                "GROUP BY u.id, u.username";

        return findMany(sql, rs -> new UserWithOverdueBooks(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getInt("overdue_count")
        ));
    }

    /**
     * Retrieves overdue borrow records for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of overdue {@link MediaRecord} objects
     */
    public List<MediaRecord> findOverdueByUserId(int userId) {
        List<MediaRecord> allRecords = findActiveByUserId(userId);
        List<MediaRecord> overdueRecords = new ArrayList<>();
        for (MediaRecord record : allRecords) {
            if (record.isOverdue()) {
                overdueRecords.add(record);
            }
        }
        return overdueRecords;
    }

    /**
     * Counts the number of active (not returned) borrow records for a specific user.
     *
     * @param userId the ID of the user
     * @return the number of active borrow records
     */
    public int countActiveByUserId(int userId) {
        return executeCount("SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND returned = 0", userId);
    }
}