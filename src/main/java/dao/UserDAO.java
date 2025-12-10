package dao;

import model.User;
import java.sql.*;


/**
 * {@code UserDAO} is a Data Access Object (DAO) class responsible for
 * managing database operations related to the {@link User} model.
 * It extends {@link BaseDAO} to reuse common database operations such as
 * table creation, query execution, and result mapping.
 *
 * <p>This class provides methods to:</p>
 * <ul>
 *     <li>Initialize the "users" table.</li>
 *     <li>Find a user by username.</li>
 *     <li>Insert a new user.</li>
 *     <li>Delete a user along with their borrow records and fines.</li>
 * </ul>
 *
 * <p>The "users" table stores user credentials including username, hashed password, and salt.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * UserDAO userDAO = new UserDAO();
 * userDAO.initializeTable();
 * userDAO.insert("john_doe", "hashedPassword", "saltValue");
 * User user = userDAO.findByUsername("john_doe");
 * boolean deleted = userDAO.delete("john_doe");
 * }
 * </pre>
 *
 * @author Library
 * @version 1.1
 *
 */
public class UserDAO extends BaseDAO {

    /**
     * Initializes the "users" table in the database.
     * The table includes an auto-increment primary key, unique username, password hash, and salt.
     * If the table already exists, no changes are made.
     */
    public void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (\n" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                " username TEXT NOT NULL UNIQUE,\n" +
                " password_hash TEXT NOT NULL,\n" +
                " salt TEXT NOT NULL\n" +
                ");";
        createTable(sql, "Users");
    }

    /**
     * Finds a user by their username.
     *
     * @param username the username of the user
     * @return the {@link User} object if found; {@code null} otherwise
     */
    public User findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, salt FROM users WHERE username = ?";
        return findOne(sql, this::mapUser, username);
    }

    /**
     * Inserts a new user into the "users" table.
     *
     * @param username     the username of the new user
     * @param passwordHash the hashed password of the user
     * @param salt         the salt used for hashing
     * @return {@code true} if insertion was successful; {@code false} otherwise
     */
    public boolean insert(String username, String passwordHash, String salt) {
        String sql = "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)";
        return executeInsert(sql, username, passwordHash, salt) > 0;
    }

    /**
     * Deletes a user and their associated borrow records and fines.
     * The deletion is performed within a transaction to ensure consistency.
     *
     * @param username the username of the user to delete
     * @return {@code true} if deletion was successful; {@code false} otherwise
     */
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

    /**
     * Maps a {@link ResultSet} row to a {@link User} object.
     *
     * @param rs the result set positioned at the current row
     * @return the mapped {@link User} object
     * @throws SQLException if a database access error occurs
     */
    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("salt")
        );
    }
}