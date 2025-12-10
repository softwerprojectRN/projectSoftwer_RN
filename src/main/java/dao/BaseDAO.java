package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Base DAO class providing common database operations.
 * Reduces code duplication across all DAO classes.
 */
public abstract class BaseDAO {
    protected final Logger logger;

    protected BaseDAO() {
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    /**
     * Executes a table creation SQL statement.
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
     * Executes a query and returns a single result using a mapper function.
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
     * Executes a query and returns multiple results using a mapper function.
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
     * Executes an insert and returns the generated key.
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
     * Executes an update/delete and returns success status.
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
     * Executes a count query.
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
     * Sets parameters on a prepared statement.
     */
    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
