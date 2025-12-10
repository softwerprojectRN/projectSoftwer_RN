package service;

import dao.UserDAO;
import model.User;
import dao.PasswordUtil;

/**
 * Service class responsible for handling user-related business logic,
 * including registration, authentication, input validation, and secure
 * password hashing.
 * <p>
 * This class communicates with {@link UserDAO} to perform database operations
 * and ensures that user data is processed securely before storage.
 * </p>
 * <p>
 * Upon creation, this service automatically initializes the users table
 * through the underlying {@code UserDAO}.
 * </p>
 *
 * @version 1.1
 */
public class UserService {

    /**
     * Data Access Object used to interact with the user persistence layer.
     * Handles operations such as finding users, inserting new records,
     * and retrieving stored authentication data.
     */
    private final UserDAO userDAO;

    /**
     * Constructs a new {@code UserService} instance.
     * <p>
     * During initialization, this constructor creates a {@link UserDAO} object
     * and triggers the setup of the underlying users table if it does not
     * already exist.
     * </p>
     */

    public UserService() {
        this.userDAO = new UserDAO();
        this.userDAO.initializeTable();
    }

    /**
     * Registers a new user after validating input fields and securely hashing
     * the provided password with a generated salt.
     * <p>
     * Registration fails if:
     * <ul>
     *   <li>The username or password is empty or null</li>
     *   <li>The username already exists in the database</li>
     *   <li>The database operation fails</li>
     * </ul>
     * </p>
     *
     * @param username the desired username chosen by the user (cannot be empty)
     * @param password the raw password to be hashed and stored securely
     * @return the newly created {@link User}, or {@code null} if registration fails
     */

    public User register(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            System.err.println("Error: Username and password cannot be empty");
            return null;
        }

        User existing = userDAO.findByUsername(username);
        if (existing != null) {
            System.out.println("User already exists: " + username);
            return null;
        }

        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(password, salt);

        if (userDAO.insert(username, passwordHash, salt)) {
            System.out.println("User registered successfully: " + username);
            return userDAO.findByUsername(username);
        }

        return null;
    }
    /**
     * Authenticates a user by verifying the existence of the username and
     * comparing the hashed form of the entered password with the stored hash.
     * <p>
     * Login fails if:
     * <ul>
     *   <li>The username does not exist in the database</li>
     *   <li>The password does not match the stored hash</li>
     * </ul>
     * </p>
     *
     * @param username the username of the account attempting to log in
     * @param password the raw password provided by the user
     * @return the authenticated {@link User} with its state updated to logged in,
     *         or {@code null} if authentication fails
     */
    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);

        if (user == null) {
            System.out.println("User not found.");
            return null;
        }

        String inputHash = PasswordUtil.hashPassword(password, user.getSalt());

        if (user.getPasswordHash().equals(inputHash)) {
            user.setLoggedIn(true);
            System.out.println("Login successful, welcome " + username + "!");
            return user;
        } else {
            System.out.println("Invalid username or password.");
            return null;
        }
    }
}