package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * {@code BaseDAO} is an abstract base class providing common database operations
 * that can be reused by all DAO (Data Access Object) classes. It simplifies
 * database interaction by handling connection management, SQL execution, and
 * result mapping. This class aims to reduce boilerplate code and provide
 * a consistent logging and error handling approach for database operations.
 *
 * <p>Features provided by this class include:</p>
 * <ul>
 *     <li>Creating tables.</li>
 *     <li>Executing SELECT queries returning single or multiple results.</li>
 *     <li>Executing INSERT statements with retrieval of generated keys.</li>
 *     <li>Executing UPDATE and DELETE statements with success status.</li>
 *     <li>Counting records in a table.</li>
 *     <li>Parameter binding for prepared statements.</li>
 *     <li>Functional interface for mapping {@link ResultSet} to domain objects.</li>
 * </ul>
 *
 * <p>All database operations log success or failure messages using
 * {@link java.util.logging.Logger}.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * public class UserDAO extends BaseDAO {
 *     public User findUserById(int id) {
 *         return findOne("SELECT * FROM users WHERE id = ?", rs -> {
 *             return new User(rs.getInt("id"), rs.getString("name"));
 *         }, id);
 *     }
 * }
 * }
 * </pre>
 *
 * @author Library
 * @version 1.1
 */
public abstract class BaseDAO {
    /**
     * Logger instance for logging database operations and errors.
     */
    protected final Logger logger;

    /**
     * Constructs a new {@code BaseDAO} and initializes the logger
     * for the specific DAO class.
     */
    protected BaseDAO() {
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    /**
     * Executes a table creation SQL statement.
     *
     * @param sql       the SQL statement to create a table
     * @param tableName the name of the table for logging purposes
     */
    protected void createTable(String sql, String tableName) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info(tableName + " table created successfully.");
        } catch (SQLException e) {
            logger.severe("Error creating " + tableName + " table: " + e.getMessage());
        }
    }

    /**
     * Executes a SELECT query that returns a single result.
     *
     * @param sql    the SQL query to execute
     * @param mapper a {@link ResultSetMapper} to map the result set to a domain object
     * @param params optional parameters for the prepared statement
     * @param <T>    the type of the domain object
     * @return the mapped object if a result exists; {@code null} otherwise
     */
    protected <T> T findOne(String sql, ResultSetMapper<T> mapper, Object... params) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapper.map(rs);
            }
        } catch (SQLException e) {
            logger.severe("Error executing query: " + e.getMessage());
        }
        return null;
    }

    /**
     * Executes a SELECT query that returns multiple results.
     *
     * @param sql    the SQL query to execute
     * @param mapper a {@link ResultSetMapper} to map each row to a domain object
     * @param params optional parameters for the prepared statement
     * @param <T>    the type of the domain objects
     * @return a list of mapped objects; empty if no results found
     */
    protected <T> List<T> findMany(String sql, ResultSetMapper<T> mapper, Object... params) {
        List<T> results = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return results;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(mapper.map(rs));
            }
        } catch (SQLException e) {
            logger.severe("Error executing query: " + e.getMessage());
        }
        return results;
    }

    /**
     * Executes an INSERT statement and returns the generated key.
     *
     * @param sql    the INSERT SQL statement
     * @param params optional parameters for the prepared statement
     * @return the generated key if insertion succeeds; -1 otherwise
     */
    protected int executeInsert(String sql, Object... params) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return -1;

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(pstmt, params);
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            logger.severe("Error inserting record: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Executes an UPDATE or DELETE statement.
     *
     * @param sql    the SQL statement to execute
     * @param params optional parameters for the prepared statement
     * @return {@code true} if at least one row was affected; {@code false} otherwise
     */
    protected boolean executeUpdate(String sql, Object... params) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error updating record: " + e.getMessage());
            return false;
        }
    }

    /**
     * Executes a COUNT query.
     *
     * @param sql    the SQL query returning a single integer count
     * @param params optional parameters for the prepared statement
     * @return the number of records; 0 if an error occurs
     */
    protected int executeCount(String sql, Object... params) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return 0;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.severe("Error counting records: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Sets parameters on a {@link PreparedStatement}.
     *
     * @param pstmt  the prepared statement
     * @param params the parameters to set
     * @throws SQLException if a database access error occurs
     */
    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

    /**
     * Functional interface for mapping a {@link ResultSet} row to a domain object.
     *
     * @param <T> the type of the mapped object
     */
    @FunctionalInterface
    public interface ResultSetMapper<T> {
        /**
         * Maps the current row of the {@link ResultSet} to a domain object.
         *
         * @param rs the result set positioned at the current row
         * @return the mapped object
         * @throws SQLException if a database access error occurs
         */
        T map(ResultSet rs) throws SQLException;
    }
}
