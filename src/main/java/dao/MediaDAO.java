package dao;
import java.util.logging.Logger;
import java.sql.*;

/**
 * Data Access Object for Media entity.
 * Manages database operations for the base media table that both books and CDs inherit from.
 *
 * @author Library Management System
 * @version 1.0
 */
public class MediaDAO {
    private static final Logger logger = Logger.getLogger(MediaDAO.class.getName());
    /**
     * Initializes the media table in the database.
     * Creates the base table for all media types.
     */
    public void initializeTable() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String sql = "CREATE TABLE IF NOT EXISTS media (\n" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                " title TEXT NOT NULL,\n" +
                " media_type TEXT NOT NULL,\n" +
                " available INTEGER NOT NULL DEFAULT 1\n" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("Media table created successfully.");
        } catch (SQLException e) {
            logger.severe("Error creating media table: " + e.getMessage());
        }
    }

    /**
     * Inserts a new media record into the database.
     *
     * @param title the title of the media
     * @param mediaType the type of media (book or cd)
     * @return the generated media ID if successful, -1 otherwise
     */
    public int insert(String title, String mediaType) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return -1;

        String sql = "INSERT INTO media (title, media_type, available) VALUES (?, ?, 1)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, title);
            pstmt.setString(2, mediaType);
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            logger.severe("Error inserting media: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates the availability status of a media item.
     *
     * @param mediaId the ID of the media
     * @param available true if available, false if borrowed
     * @return true if update was successful, false otherwise
     */
    public boolean updateAvailability(int mediaId, boolean available) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        String sql = "UPDATE media SET available = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, available ? 1 : 0);
            pstmt.setInt(2, mediaId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.severe("Error updating media availability: " + e.getMessage());
            return false;
        }
    }
}