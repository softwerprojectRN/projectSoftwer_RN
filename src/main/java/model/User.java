package model;

/**
 * Represents a user in the library system.
 * Base class for Admin and Borrower.
 *
 * @author Library Management System
 * @version 1.0
 */
public class User {
    /** The unique identifier for the user */
    private int id;

    /** The username */
    private String username;

    /** The hashed password */
    private String passwordHash;

    /** The salt used for password hashing */
    private String salt;

    /** The login status */
    private boolean loggedIn;

    /**
     * Constructs a User object.
     *
     * @param id the user's unique identifier
     * @param username the username
     * @param passwordHash the hashed password
     * @param salt the salt for password hashing
     */
    public User(int id, String username, String passwordHash, String salt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.loggedIn = false;
    }

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the user ID.
     *
     * @param id the new user ID
     */
    public void setId(int id) {
        this.id = id;
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
     * Sets the username.
     *
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password hash.
     *
     * @return the hashed password
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the password hash.
     *
     * @param passwordHash the new password hash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Gets the salt.
     *
     * @return the salt
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Sets the salt.
     *
     * @param salt the new salt
     */
    public void setSalt(String salt) {
        this.salt = salt;
    }

    /**
     * Checks if the user is logged in.
     *
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Sets the login status.
     *
     * @param loggedIn the new login status
     */
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    /**
     * Logs out the user.
     */
    public void logout() {
        if (this.loggedIn) {
            this.loggedIn = false;
            System.out.println("Logged out successfully.");
        } else {
            System.out.println("You are not logged in yet.");
        }
    }
}