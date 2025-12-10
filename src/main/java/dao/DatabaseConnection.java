package dao;
import java.util.logging.Logger;
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
 * @author Library
 * @version 1.1
 */
public class DatabaseConnection {
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());
    /** JDBC URL for the SQLite database */
    private static final String URL = "jdbc:sqlite:database.db";

    /**
     * Attempts to establish a connection to the SQLite database.
     *
     * @return a {@link Connection} object if successful, or {@code null} if a connection cannot be established
     */

    private DatabaseConnection() {
        throw new IllegalStateException("Utility class");
    }
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            logger.severe("Database connection error: " + e.getMessage());
            return null;
        }
    }
}