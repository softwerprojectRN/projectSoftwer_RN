package util;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Represents an email server that can send emails and track sent messages.
 * <p>
 * Supports loading credentials from a .env file or directly via constructor.
 * Provides methods to send emails, view sent emails, and clear email history.
 * <p>
 * Usage example:
 * <pre>
 * EmailServer server = new EmailServer();
 * server.sendEmail("recipient@example.com", "Subject", "Body");
 * </pre>
 * </p>
 * Author: Library Management System
 * Version: 1.0
 */
public class EmailServer {
    private final String username;
    private final String password;
    private final List<Email> sentEmails = new ArrayList<>();



    /**
     * Inner class representing a sent email.
     */
    public static class Email {
        private final String to;
        private final String subject;
        private final String body;
        private final long timestamp;

        public Email(String to, String subject, String body) {
            this.to = to;
            this.subject = subject;
            this.body = body;
            this.timestamp = System.currentTimeMillis();
        }

        public String getTo() {
            return to;
        }

        public String getSubject() {
            return subject;
        }

        public String getBody() {
            return body;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Constructs an EmailServer and loads credentials from a .env file.
     * Throws IllegalStateException if credentials are missing.
     */
    public EmailServer() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            this.username = dotenv.get("EMAIL_USERNAME");
            this.password = dotenv.get("EMAIL_PASSWORD");

            if (username == null || username.isEmpty()) {
                throw new IllegalStateException("EMAIL_USERNAME not found in .env file");
            }
            if (password == null || password.isEmpty()) {
                throw new IllegalStateException("EMAIL_PASSWORD not found in .env file");
            }

            System.out.println("Email server initialized successfully");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize EmailServer: " + e.getMessage(), e);
        }
    }

    /**
     * Constructs an EmailServer with explicit username and password.
     *
     * @param username email username
     * @param password email password
     */
    public EmailServer(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        this.username = username;
        this.password = password;
    }

    /**
     * Sends an email to a recipient with the specified subject and body.
     * Tracks sent emails in memory.
     *
     * @param to recipient email address
     * @param subject email subject
     * @param body email body
     */
    public void sendEmail(String to, String subject, String body) {
        if (username == null || username.isEmpty()) {
            throw new IllegalStateException("Email username is not configured");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException("Email password is not configured");
        }
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException("Recipient email address cannot be null or empty");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email sent successfully to: " + to);

            sentEmails.add(new Email(to, subject, body));
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a copy of the list of sent emails.
     * @return list of sent emails
     */
    public List<Email> getSentEmails() {
        return new ArrayList<>(sentEmails);
    }

    /**
     * Clears the sent emails history.
     */
    public void clearSentEmails() {
        sentEmails.clear();
    }

    /**
     * Main method for testing the email server.
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            System.out.println("Current working directory: " + System.getProperty("user.dir"));

            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            String username = dotenv.get("EMAIL_USERNAME");
            String password = dotenv.get("EMAIL_PASSWORD");

            if (username == null || password == null) {
                System.err.println("Email credentials not found in .env file");
                return;
            }

            EmailServer emailService = new EmailServer(username, password);
            String subject = "Test Email";
            String body = "This is a test email from the Library System";

            emailService.sendEmail("s12216975@stu.najah.edu", subject, body);

            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}