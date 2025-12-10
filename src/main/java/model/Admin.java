package model;
import java.util.logging.Logger;

/**
 * Represents an administrator in the library system.
 *
 * <p>Extends {@link User} to provide administrative privileges, such as
 * managing users, sending reminders, and overseeing library operations.</p>
 *
 * <p>Provides methods for displaying admin information.</p>
 *
 * <p>Logging is done using {@link Logger}.</p>
 *
 * @author Library
 * @version 1.1
 */
public class Admin extends User {
    private static final Logger logger = Logger.getLogger(Admin.class.getName());
    /**
     * Constructs a new {@code Admin}.
     *
     * @param id the admin's unique identifier
     * @param username the admin's username
     * @param passwordHash the hashed password
     * @param salt the salt used for password hashing
     */
    public Admin(int id, String username, String passwordHash, String salt) {
        super(id, username, passwordHash, salt);
    }

    /**
     * Displays admin information to the console.
     */
    public void showAdminInfo() {
        logger.info("Admin username: " + getUsername());
    }
}