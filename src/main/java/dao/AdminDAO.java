package dao;

import model.Admin;
import util.DatabaseConnection;

import java.sql.*;

public class AdminDAO {

    public void initializeTable() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String sql = "CREATE TABLE IF NOT EXISTS admins (\n" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "  username TEXT NOT NULL UNIQUE,\n" +
                "  password_hash TEXT NOT NULL,\n" +
                "  salt TEXT NOT NULL\n" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Admins table created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating admins table: " + e.getMessage());
        }
    }

    public Admin findByUsername(String username) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        String sql = "SELECT * FROM admins WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Admin(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("salt")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding admin: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(String username, String passwordHash, String salt) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        String sql = "INSERT INTO admins (username, password_hash, salt) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.setString(3, salt);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting admin: " + e.getMessage());
            return false;
        }
    }
}