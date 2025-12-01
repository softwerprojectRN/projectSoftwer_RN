package service;

import model.User;

/**
 * Observer interface for sending notifications to users.
 */
interface Observer {

    /**
     * Sends a notification message to the specified user.
     *
     * @param user the user to notify
     * @param message the message content
     */
    void notify(User user, String message);
}

/**
 * Sends email notifications to users via an {@link EmailServer}.
 * Implements the {@link Observer} interface.
 *
 * Usage assumes that the user's username can be used to construct an email address.
 * Example: username@example.com
 *
 * Handles null or invalid data gracefully and logs warnings or errors.
 *
 * Author: Library Management System
 * Version: 1.0
 */
public class EmailNotifier implements Observer {

    /** Email server used to send messages */
    private EmailServer emailServer;

    /**
     * Constructs an {@code EmailNotifier} with the given {@link EmailServer}.
     *
     * @param emailServer the email server to use for sending notifications
     */
    public EmailNotifier(EmailServer emailServer) {
        this.emailServer = emailServer;
    }

    /**
     * Sends an email notification to the specified user.
     *
     * @param user the user to notify
     * @param message the notification message
     * @throws RuntimeException if sending email fails
     */
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
            String email = user.getUsername() + "@example.com";
            emailServer.sendEmail(email, "Library Notification", message);
            System.out.println("Email notification sent to: " + email);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
}