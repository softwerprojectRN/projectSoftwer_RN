package model;
import java.util.logging.Logger;

/**
 * Represents an administrator user in the library system.
 * Extends User with administrative privileges.
 *
 * @author Library Management System
 * @version 1.0
 */
public class Admin extends User {
    private static final Logger logger = Logger.getLogger(Admin.class.getName());
    /**
     * Constructs an Admin object.
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