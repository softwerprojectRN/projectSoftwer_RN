package service;

import dao.UserDAO;
import model.User;
import util.PasswordUtil;

public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
        this.userDAO.initializeTable();
    }

    public User register(String username, String password) {
        // Validate input
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            System.err.println("Error: Username and password cannot be empty");
            return null;
        }

        // Check if user already exists
        User existing = userDAO.findByUsername(username);
        if (existing != null) {
            System.out.println("User already exists: " + username);
            return null;
        }

        // Generate salt and hash password
        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(password, salt);

        // Insert into database
        if (userDAO.insert(username, passwordHash, salt)) {
            System.out.println("User registered successfully: " + username);
            return userDAO.findByUsername(username);
        }

        return null;
    }

    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);

        if (user == null) {
            System.out.println("User not found.");
            return null;
        }

        // Hash the input password and compare
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