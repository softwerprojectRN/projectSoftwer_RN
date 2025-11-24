package model;

/**
 * Represents a user with overdue books.
 * Used for reporting and notification purposes.
 *
 * @author Library Management System
 * @version 1.0
 */
public class UserWithOverdueBooks {
    /** The user's ID */
    private int userId;

    /** The username */
    private String username;

    /** The count of overdue items */
    private int overdueCount;

    /**
     * Constructs a UserWithOverdueBooks object.
     *
     * @param userId the user's ID
     * @param username the username
     * @param overdueCount the number of overdue items
     */
    public UserWithOverdueBooks(int userId, String username, int overdueCount) {
        this.userId = userId;
        this.username = username;
        this.overdueCount = overdueCount;
    }

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the overdue count.
     *
     * @return the number of overdue items
     */
    public int getOverdueCount() {
        return overdueCount;
    }
}