package dao;

import model.User;
import util.DatabaseConnection;
import util.PasswordUtil;

import java.sql.*;

public class UserDAO {

    public void initializeTable() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String sql = "CREATE TABLE IF NOT EXISTS users (\n" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "  username TEXT NOT NULL UNIQUE,\n" +
                "  password_hash TEXT NOT NULL,\n" +
                "  salt TEXT NOT NULL\n" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Users table created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating users table: " + e.getMessage());
        }
    }

    public User findByUsername(String username) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("salt")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(String username, String passwordHash, String salt) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        String sql = "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.setString(3, salt);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String username) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            conn.setAutoCommit(false);

            // Get user ID
            int userId = -1;
            String findSql = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(findSql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("id");
                } else {
                    conn.rollback();
                    return false;
                }
            }

            // Delete related records
            String deleteBorrowSql = "DELETE FROM borrow_records WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteBorrowSql)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }

            String deleteFinesSql = "DELETE FROM user_fines WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteFinesSql)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }

            // Delete user
            String deleteUserSql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteUserSql)) {
                pstmt.setInt(1, userId);
                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback error: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }
}