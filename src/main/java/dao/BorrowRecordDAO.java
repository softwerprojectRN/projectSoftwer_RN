package dao;

import model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDAO extends BaseDAO {

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

    public int insert(int userId, int mediaId, String mediaType, String mediaTitle,
                      LocalDate borrowDate, LocalDate dueDate) {
        return executeInsert(
                "INSERT INTO borrow_records (user_id, media_id, media_type, media_title, borrow_date, due_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                userId, mediaId, mediaType, mediaTitle, borrowDate.toString(), dueDate.toString()
        );
    }

    public boolean markAsReturned(int recordId, LocalDate returnDate, double fine) {
        return executeUpdate("UPDATE borrow_records SET returned = 1, return_date = ?, fine = ? WHERE id = ?",
                returnDate.toString(), fine, recordId);
    }

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

    private Media fetchMediaDetails(Connection conn, int mediaId, String mediaType) throws SQLException {
        if ("book".equals(mediaType)) {
            return fetchBookDetails(conn, mediaId);
        } else if ("cd".equals(mediaType)) {
            return fetchCDDetails(conn, mediaId);
        }
        return null;
    }

    private Book fetchBookDetails(Connection conn, int mediaId) throws SQLException {
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

    private CD fetchCDDetails(Connection conn, int mediaId) throws SQLException {
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

    public int countActiveByUserId(int userId) {
        return executeCount("SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND returned = 0", userId);
    }
}