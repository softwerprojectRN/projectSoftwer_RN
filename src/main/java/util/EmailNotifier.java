package util;

import model.User;

interface Observer {
    void notify(User user, String message);
}

public class EmailNotifier implements Observer {
    private EmailServer emailServer;

    public EmailNotifier(EmailServer emailServer) {
        this.emailServer = emailServer;
    }

    @Override
    public void notify(User user, String message) {
        if (emailServer == null) {
            System.err.println("Warning: Email server not available. Notification not sent.");
            return;
        }

        if (user == null || user.getUsername() == null) {
            System.err.println("Warning: Invalid user data. Notification not sent.");
            return;
        }

        try {
            String email = user.getUsername() + "@example.com"; // Assuming username is email
            emailServer.sendEmail(email, "Library Notification", message);
            System.out.println("Email notification sent to: " + email);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
}