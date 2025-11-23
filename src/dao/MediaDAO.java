package dao;

import util.DatabaseConnection;

import java.sql.*;

public class MediaDAO {

    public void initializeTable() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String sql = "CREATE TABLE IF NOT EXISTS media (\n" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "  title TEXT NOT NULL,\n" +
                "  media_type TEXT NOT NULL,\n" +
                "  available INTEGER NOT NULL DEFAULT 1\n" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Media table created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating media table: " + e.getMessage());
        }
    }

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
            System.err.println("Error inserting media: " + e.getMessage());
        }
        return -1;
    }

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
            System.err.println("Error updating media availability: " + e.getMessage());
            return false;
        }
    }
}