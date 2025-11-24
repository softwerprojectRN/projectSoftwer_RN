package service;

import dao.AdminDAO;
import dao.UserDAO;
import model.Admin;
import model.UserWithOverdueBooks;
import util.EmailNotifier;
import util.EmailServer;
import util.PasswordUtil;

import java.util.List;

/**
 * Service class responsible for admin-related operations such as registration,
 * login, user management, and sending overdue reminders.
 *
 * @author Library Management System
 * @version 1.0
 */
public class AdminService {

    /** DAO for managing admin records. */
    private final AdminDAO adminDAO;

    /** DAO for managing user records. */
    private final UserDAO userDAO;

    /** Email server instance used to send notifications. */
    private EmailServer emailServer;

    /** Email notifier utility wrapping the email server. */
    private EmailNotifier emailNotifier;

    /**
     * Constructs an AdminService instance, initializes required DAOs,
     * and attempts to initialize the email server.
     */
    public AdminService() {
        this.adminDAO = new AdminDAO();
        this.userDAO = new UserDAO();
        this.adminDAO.initializeTable();

        try {
            this.emailServer = new EmailServer();
            this.emailNotifier = new EmailNotifier(emailServer);
        } catch (Exception e) {
            System.err.println("Warning: Email server initialization failed: " + e.getMessage());
            this.emailServer = null;
            this.emailNotifier = null;
        }
    }

    /**
     * Registers a new admin with a username and password.
     *
     * @param username The desired username for the admin.
     * @param password The password for the admin account.
     * @return The newly created Admin object, or null if registration fails.
     */
    public Admin register(String username, String password) {
        Admin existing = adminDAO.findByUsername(username);
        if (existing != null) {
            System.out.println("Admin already exists: " + username);
            return null;
        }

        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(password, salt);

        if (adminDAO.insert(username, passwordHash, salt)) {
            System.out.println("Admin registered successfully: " + username);
            return adminDAO.findByUsername(username);
        }

        return null;
    }

    /**
     * Logs an admin into the system.
     *
     * @param username The username of the admin.
     * @param password The password provided by the admin.
     * @return The logged-in Admin object, or null if authentication fails.
     */
    public Admin login(String username, String password) {
        Admin admin = adminDAO.findByUsername(username);

        if (admin == null) {
            System.out.println("Admin not found.");
            return null;
        }

        String inputHash = PasswordUtil.hashPassword(password, admin.getSalt());

        if (admin.getPasswordHash().equals(inputHash)) {
            admin.setLoggedIn(true);
            System.out.println("Admin login successful, welcome " + username + "!");
            return admin;
        } else {
            System.out.println("Invalid credentials.");
            return null;
        }
    }

    /**
     * Deletes a user account from the system.
     *
     * @param username The username of the user to be deleted.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean unregisterUser(String username) {
        return userDAO.delete(username);
    }

    /**
     * Sends email reminders to all users who have overdue books.
     *
     * @param borrowingService The borrowing service used to fetch overdue records.
     */
    public void sendOverdueReminders(BorrowingService borrowingService) {
        if (emailNotifier == null) {
            System.out.println("Warning: Email server not available. Cannot send reminders.");
            return;
        }

        List<UserWithOverdueBooks> usersWithOverdueBooks = borrowingService.getUsersWithOverdueBooks();

        for (UserWithOverdueBooks userInfo : usersWithOverdueBooks) {
            String message = "You have " + userInfo.getOverdueCount() + " overdue book(s).";
            try {
                model.User user = userDAO.findByUsername(userInfo.getUsername());
                if (user != null) {
                    emailNotifier.notify(user, message);
                }
            } catch (Exception e) {
                System.err.println("Failed to send email to " + userInfo.getUsername() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Sets a new email server for sending notifications.
     *
     * @param server The EmailServer instance to use.
     */
    public void setEmailServer(EmailServer server) {
        this.emailServer = server;
        if (server == null) {
            this.emailNotifier = null;
        } else {
            this.emailNotifier = new EmailNotifier(server);
        }
    }

    /**
     * Retrieves the currently configured email server.
     *
     * @return The EmailServer instance.
     */
    public EmailServer getEmailServer() {
        return emailServer;
    }
}
