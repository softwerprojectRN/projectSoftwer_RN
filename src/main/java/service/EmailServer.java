package service;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * A service class responsible for sending emails and maintaining a record
 * of previously sent messages.
 *
 * <p>This class supports two initialization modes:</p>
 * <ul>
 *     <li>Automatically loading credentials from a <b>.env</b> file.</li>
 *     <li>Explicitly receiving credentials through the constructor.</li>
 * </ul>
 *
 * <p>
 * The class uses Jakarta Mail API to handle SMTP communication and provides
 * functionality for sending messages, retrieving email history, and clearing logs.
 * </p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>
 *     EmailServer server = new EmailServer();
 *     server.sendEmail("recipient@example.com", "Subject", "Message body");
 * </pre>
 *
 * @author Library
 * @version 1.1
 */

public class EmailServer {
    private final String username;
    private final String password;
    private final List<Email> sentEmails = new ArrayList<>();



    /**
     * Represents a record of a sent email.
     *
     * <p>This class stores the recipient address, subject, body content,
     * and the time the message was sent. Instances are created automatically
     * whenever {@code sendEmail()} is successfully executed.</p>
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
     * Initializes the EmailServer by loading SMTP credentials from a .env file.
     *
     * <p>The method expects two environment variables:</p>
     * <ul>
     *     <li><b>EMAIL_USERNAME</b> – the sender email address</li>
     *     <li><b>EMAIL_PASSWORD</b> – the corresponding password</li>
     * </ul>
     *
     * <p>If either value is missing or empty, the constructor throws
     * an {@link IllegalStateException} to prevent misconfigured usage.</p>
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
     * Constructs an EmailServer using manually provided SMTP credentials.
     *
     * @param username the email address used for sending messages
     * @param password the password or app-specific key for SMTP authentication
     *
     * @throws IllegalArgumentException if any parameter is null or empty
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
     * Sends an email message to a specified recipient using the configured
     * SMTP credentials. The email is transmitted using TLS encryption via
     * Gmail's SMTP server.
     *
     * <p>After a successful send, the message is added to the internal
     * history list for later retrieval.</p>
     *
     * @param to       the recipient's email address; must not be null or empty
     * @param subject  the subject line of the email
     * @param body     the message content; if null, an empty body is used
     *
     * @throws IllegalStateException if SMTP credentials are not configured
     * @throws IllegalArgumentException if recipient is invalid
     * @throws RuntimeException if the email fails to send due to SMTP errors
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
        if (body == null) {
            body = "";
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
     * Returns a snapshot of all previously sent emails.
     *
     * <p>The returned list is a defensive copy to prevent external modification
     * of the internal email history.</p>
     *
     * @return a list containing all sent email records
     */

    public List<Email> getSentEmails() {
        return new ArrayList<>(sentEmails);
    }

    /**
     * Clears the entire history of sent emails.
     *
     * <p>This operation does not affect SMTP settings or service behavior—only
     * the stored log is removed.</p>
     */

    public void clearSentEmails() {
        sentEmails.clear();
    }



}