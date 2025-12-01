package service;

import dao.UserDAO;
import model.User;
import dao.PasswordUtil;

/**
 * Service class that handles user registration and login operations.
 * Provides validation, password hashing, and communication with {@link UserDAO}.
 *
 * @author Library Management System
 * @version 1.0
 */
public class UserService {

    /** Data Access Object responsible for user-related database operations */
    private final UserDAO userDAO;

    /**
     * Constructs a new {@code UserService} instance and initializes the users table.
     */
    public UserService() {
        this.userDAO = new UserDAO();
        this.userDAO.initializeTable();
    }

    /**
     * Registers a new user after validating inputs and hashing the password.
     *
     * @param username the username chosen by the user (cannot be empty)
     * @param password the raw password to be hashed and stored securely
     * @return the created {@link User} object if successful, otherwise {@code null}
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
     * Authenticates a user by verifying their username and password.
     *
     * @param username the username of the user attempting to log in
     * @param password the raw password entered by the user
     * @return the logged-in {@link User} object if credentials are valid, otherwise {@code null}
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
