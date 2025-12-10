package dao;

import java.sql.*;

public class FineDAO extends BaseDAO {

    public void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS user_fines (\n" +
                " user_id INTEGER PRIMARY KEY,\n" +
                " total_fine REAL DEFAULT 0.0,\n" +
                " FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE\n" +
                ");";
        createTable(sql, "User fines");
    }

    public double getFineBalance(int userId) {
        Double balance = findOne("SELECT total_fine FROM user_fines WHERE user_id = ?",
                rs -> rs.getDouble("total_fine"), userId);
        if (balance == null) {
            initializeFine(userId);
            return 0.0;
        }
        return balance;
    }

    public boolean initializeFine(int userId) {
        return executeInsert("INSERT INTO user_fines (user_id, total_fine) VALUES (?, 0.0)", userId) > 0;
    }

    public boolean updateFine(int userId, double totalFine) {
        boolean updated = executeUpdate("UPDATE user_fines SET total_fine = ? WHERE user_id = ?",
                totalFine, userId);
        return updated || initializeFine(userId);
    }

    public boolean addFine(int userId, double amount) {
        return updateFine(userId, getFineBalance(userId) + amount);
    }

    public boolean payFine(int userId, double amount) {
        double currentFine = getFineBalance(userId);
        if (amount <= 0 || amount > currentFine) {
            logger.severe("Invalid payment amount.");
            return false;
        }
        return updateFine(userId, currentFine - amount);
    }

    public boolean clearFine(int userId) {
        return updateFine(userId, 0.0);
    }
}