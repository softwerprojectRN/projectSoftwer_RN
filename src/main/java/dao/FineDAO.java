package dao;

/**
 * {@code FineDAO} is a Data Access Object (DAO) class responsible for
 * managing database operations related to user fines.
 * It extends {@link BaseDAO} to utilize common database operations such as
 * table creation, query execution, and result mapping.
 *
 * <p>This class provides methods to:</p>
 * <ul>
 *     <li>Initialize the "user_fines" table.</li>
 *     <li>Retrieve the fine balance for a specific user.</li>
 *     <li>Initialize a fine record for a user.</li>
 *     <li>Update, add, or pay fines.</li>
 *     <li>Clear all fines for a user.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * FineDAO fineDAO = new FineDAO();
 * fineDAO.initializeTable();
 * double balance = fineDAO.getFineBalance(userId);
 * fineDAO.addFine(userId, 5.0);
 * fineDAO.payFine(userId, 3.0);
 * fineDAO.clearFine(userId);
 * }
 * </pre>
 *
 * @author Library
 * @version 1.1
 *
 */
public class FineDAO extends BaseDAO {

    /**
     * Initializes the "user_fines" table in the database.
     * If the table already exists, no changes are made.
     * The table has a foreign key referencing the "users" table.
     */
    public void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS user_fines (\n" +
                " user_id INTEGER PRIMARY KEY,\n" +
                " total_fine REAL DEFAULT 0.0,\n" +
                " FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE\n" +
                ");";
        createTable(sql, "User fines");
    }

    /**
     * Retrieves the total fine balance for a specific user.
     * If the user has no fine record, it initializes one with a balance of 0.0.
     *
     * @param userId the ID of the user
     * @return the total fine balance
     */
    public double getFineBalance(int userId) {
        Double balance = findOne("SELECT total_fine FROM user_fines WHERE user_id = ?",
                rs -> rs.getDouble("total_fine"), userId);
        if (balance == null) {
            initializeFine(userId);
            return 0.0;
        }
        return balance;
    }

    /**
     * Initializes a fine record for a user with a balance of 0.0.
     *
     * @param userId the ID of the user
     * @return {@code true} if initialization was successful; {@code false} otherwise
     */
    public boolean initializeFine(int userId) {
        return executeInsert("INSERT INTO user_fines (user_id, total_fine) VALUES (?, 0.0)", userId) > 0;
    }

    /**
     * Updates the fine balance for a specific user.
     * If the user has no fine record, it initializes one with the given totalFine.
     *
     * @param userId    the ID of the user
     * @param totalFine the new total fine amount
     * @return {@code true} if update or initialization was successful; {@code false} otherwise
     */
    public boolean updateFine(int userId, double totalFine) {
        boolean updated = executeUpdate("UPDATE user_fines SET total_fine = ? WHERE user_id = ?",
                totalFine, userId);
        return updated || initializeFine(userId);
    }

    /**
     * Adds a specified amount to the user's current fine balance.
     *
     * @param userId the ID of the user
     * @param amount the amount to add
     * @return {@code true} if the operation was successful; {@code false} otherwise
     */
    public boolean addFine(int userId, double amount) {
        return updateFine(userId, getFineBalance(userId) + amount);
    }

    /**
     * Pays a specified amount from the user's fine balance.
     * Validates that the payment amount is positive and does not exceed the current balance.
     *
     * @param userId the ID of the user
     * @param amount the amount to pay
     * @return {@code true} if the payment was successful; {@code false} otherwise
     */
    public boolean payFine(int userId, double amount) {
        double currentFine = getFineBalance(userId);
        if (amount <= 0 || amount > currentFine) {
            logger.severe("Invalid payment amount.");
            return false;
        }
        return updateFine(userId, currentFine - amount);
    }

    /**
     * Clears all fines for a specific user by setting the fine balance to 0.0.
     *
     * @param userId the ID of the user
     * @return {@code true} if the operation was successful; {@code false} otherwise
     */
    public boolean clearFine(int userId) {
        return updateFine(userId, 0.0);
    }
}