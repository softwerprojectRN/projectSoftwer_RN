package model;

/**
 * Represents a library user who currently has overdue books.
 *
 * <p>This class is primarily used for reporting and sending notifications
 * about overdue items.</p>
 *
 * @author Library Management
 * @version 1.1
 */
public class UserWithOverdueBooks {
    /** The unique ID of the user. */
    private int userId;

    /** The username of the user. */
    private String username;

    /** The number of overdue items for this user. */
    private int overdueCount;

    /**
     * Constructs a {@code UserWithOverdueBooks} instance.
     *
     * @param userId the user's unique identifier
     * @param username the user's username
     * @param overdueCount the count of overdue items
     */
    public UserWithOverdueBooks(int userId, String username, int overdueCount) {
        this.userId = userId;
        this.username = username;
        this.overdueCount = overdueCount;
    }

    /**
     * Returns the user's unique ID.
     *
     * @return the user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Returns the user's username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the number of overdue items for this user.
     *
     * @return the overdue item count
     */
    public int getOverdueCount() {
        return overdueCount;
    }
}