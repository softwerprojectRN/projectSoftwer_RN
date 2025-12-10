package dao;

import model.User;
import java.sql.*;

public class UserDAO extends BaseDAO {

    public void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (\n" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                " username TEXT NOT NULL UNIQUE,\n" +
                " password_hash TEXT NOT NULL,\n" +
                " salt TEXT NOT NULL\n" +
                ");";
        createTable(sql, "Users");
    }

    public User findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, salt FROM users WHERE username = ?";
        return findOne(sql, this::mapUser, username);
    }

    public boolean insert(String username, String passwordHash, String salt) {
        String sql = "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)";
        return executeInsert(sql, username, passwordHash, salt) > 0;
    }

    public boolean delete(String username) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            conn.setAutoCommit(false);

            Integer userId = findOne("SELECT id FROM users WHERE username = ?",
                    rs -> rs.getInt("id"), username);
            if (userId == null) {
                conn.rollback();
                return false;
            }

            executeUpdate("DELETE FROM borrow_records WHERE user_id = ?", userId);
            executeUpdate("DELETE FROM user_fines WHERE user_id = ?", userId);
            boolean deleted = executeUpdate("DELETE FROM users WHERE id = ?", userId);

            if (deleted) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            logger.severe("Error deleting user: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                logger.severe("Rollback error: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                logger.severe("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("salt")
        );
    }
}