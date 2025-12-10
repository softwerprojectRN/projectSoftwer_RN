package dao;

import model.Admin;
import java.sql.*;

/**
 * {@code AdminDAO} is a Data Access Object (DAO) class that handles
 * database operations related to the {@link Admin} model.
 * It extends {@link BaseDAO} to leverage common database operations
 * such as table creation, query execution, and result mapping.
 *
 * <p>This class provides methods to:</p>
 * <ul>
 *     <li>Create the "admins" table if it does not exist.</li>
 *     <li>Find an admin by username.</li>
 *     <li>Insert a new admin record.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * AdminDAO adminDAO = new AdminDAO();
 * adminDAO.initializeTable();
 * Admin admin = adminDAO.findByUsername("admin1");
 * boolean success = adminDAO.insert("admin2", "hashedPassword", "saltValue");
 * }
 * </pre>
 *
 * @author Library
 * @version 1.1
 */
public class AdminDAO extends BaseDAO {

    /**
     * Initializes the "admins" table in the database.
     * If the table already exists, no changes are made.
     */
    public void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS admins (\n" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                " username TEXT NOT NULL UNIQUE,\n" +
                " password_hash TEXT NOT NULL,\n" +
                " salt TEXT NOT NULL\n" +
                ");";
        createTable(sql, "Admins");
    }

    /**
     * Finds an {@link Admin} by its username.
     *
     * @param username the username to search for
     * @return the {@link Admin} object if found; {@code null} otherwise
     */
    public Admin findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, salt FROM admins WHERE username = ?";
        return findOne(sql, this::mapAdmin, username);
    }

    /**
     * Inserts a new admin record into the "admins" table.
     *
     * @param username     the username of the new admin (must be unique)
     * @param passwordHash the hashed password for the admin
     * @param salt         the salt used for hashing the password
     * @return {@code true} if the insert was successful; {@code false} otherwise
     */
    public boolean insert(String username, String passwordHash, String salt) {
        String sql = "INSERT INTO admins (username, password_hash, salt) VALUES (?, ?, ?)";
        return executeInsert(sql, username, passwordHash, salt) > 0;
    }

    /**
     * Maps a {@link ResultSet} row to an {@link Admin} object.
     *
     * @param rs the result set positioned at the current row
     * @return the mapped {@link Admin} object
     * @throws SQLException if a database access error occurs
     */
    private Admin mapAdmin(ResultSet rs) throws SQLException {
        return new Admin(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("salt")
        );
    }
}