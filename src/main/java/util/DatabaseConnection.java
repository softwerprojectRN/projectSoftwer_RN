package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for establishing a connection to the SQLite database.
 *
 * Provides a single method to obtain a {@link Connection} object.
 * Handles SQL exceptions and logs errors if the connection fails.
 *
 * Usage example:
 * <pre>
 * Connection conn = DatabaseConnection.getConnection();
 * if (conn != null) {
 *     // Use the connection
 * }
 * </pre>
 *
 * Author: Library Management System
 * Version: 1.0
 */
public class DatabaseConnection {

    /** JDBC URL for the SQLite database */
    private static final String URL = "jdbc:sqlite:database.db";

    /**
     * Attempts to establish a connection to the SQLite database.
     *
     * @return a {@link Connection} object if successful, or {@code null} if a connection cannot be established
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            return null;
        }
    }
}