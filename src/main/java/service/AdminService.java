package service;

import dao.AdminDAO;
import dao.UserDAO;
import model.Admin;
import model.UserWithOverdueBooks;
import util.EmailNotifier;
import util.EmailServer;
import util.PasswordUtil;

import java.util.List;

public class AdminService {
    private final AdminDAO adminDAO;
    private final UserDAO userDAO;
    private EmailServer emailServer;
    private EmailNotifier emailNotifier;

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

    public boolean unregisterUser(String username) {
        return userDAO.delete(username);
    }

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

    public void setEmailServer(EmailServer server) {
        this.emailServer = server;
        if (server == null) {
            this.emailNotifier = null;
        } else {
            this.emailNotifier = new EmailNotifier(server);
        }
    }

    public EmailServer getEmailServer() {
        return emailServer;
    }
}