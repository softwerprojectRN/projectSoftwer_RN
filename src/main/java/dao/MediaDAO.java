package dao;

import java.sql.*;

public class MediaDAO extends BaseDAO {

    public void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS media (\n" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                " title TEXT NOT NULL,\n" +
                " media_type TEXT NOT NULL,\n" +
                " available INTEGER NOT NULL DEFAULT 1\n" +
                ");";
        createTable(sql, "Media");
    }

    public int insert(String title, String mediaType) {
        return executeInsert("INSERT INTO media (title, media_type, available) VALUES (?, ?, 1)",
                title, mediaType);
    }

    public boolean updateAvailability(int mediaId, boolean available) {
        return executeUpdate("UPDATE media SET available = ? WHERE id = ?",
                available ? 1 : 0, mediaId);
    }
}