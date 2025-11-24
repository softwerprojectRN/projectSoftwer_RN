package dao;

import util.DatabaseConnection;
import java.sql.*;

/**
 * Data Access Object for Fine entity.
 * Manages database operations for user fines including balance tracking and payment processing.
 *
 * @author Library Management System
 * @version 1.0
 */
public class FineDAO {

    /**
     * Initializes the user_fines table in the database.
     * Creates the table with a foreign key relationship to the users table.
     */
    public void initializeTable() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String sql = "CREATE TABLE IF NOT EXISTS user_fines (\n" +
                " user_id INTEGER PRIMARY KEY,\n" +
                " total_fine REAL DEFAULT 0.0,\n" +
                " FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE\n" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("User fines table created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating user_fines table: " + e.getMessage());
        }
    }

    /**
     * Retrieves the current fine balance for a user.
     * If no record exists, initializes one with zero balance.
     *
     * @param userId the ID of the user
     * @return the current fine balance
     */
    public double getFineBalance(int userId) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return 0.0;

        String sql = "SELECT total_fine FROM user_fines WHERE user_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_fine");
            } else {
                initializeFine(userId);
                return 0.0;
            }
        } catch (SQLException e) {
            System.err.println("Error getting fine balance: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Initializes a fine record for a new user with zero balance.
     *
     * @param userId the ID of the user
     * @return true if initialization was successful, false otherwise
     */
    public boolean initializeFine(int userId) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        String sql = "INSERT INTO user_fines (user_id, total_fine) VALUES (?, 0.0)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error initializing fine: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the total fine balance for a user.
     *
     * @param userId the ID of the user
     * @param totalFine the new total fine amount
     * @return true if update was successful, false otherwise
     */
    public boolean updateFine(int userId, double totalFine) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        String sql = "UPDATE user_fines SET total_fine = ? WHERE user_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, totalFine);
            pstmt.setInt(2, userId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                return initializeFine(userId);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating fine: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a fine amount to the user's existing balance.
     *
     * @param userId the ID of the user
     * @param amount the fine amount to add
     * @return true if addition was successful, false otherwise
     */
    public boolean addFine(int userId, double amount) {
        double currentFine = getFineBalance(userId);
        return updateFine(userId, currentFine + amount);
    }

    /**
     * Processes a fine payment by deducting the amount from user's balance.
     *
     * @param userId the ID of the user
     * @param amount the payment amount
     * @return true if payment was successful, false otherwise
     */
    public boolean payFine(int userId, double amount) {
        double currentFine = getFineBalance(userId);

        if (amount <= 0 || amount > currentFine) {
            System.err.println("Invalid payment amount.");
            return false;
        }
        return updateFine(userId, currentFine - amount);
    }

    /**
     * Clears all fines for a user by setting balance to zero.
     *
     * @param userId the ID of the user
     * @return true if clearing was successful, false otherwise
     */
    public boolean clearFine(int userId) {
        return updateFine(userId, 0.0);
    }
}