package domain;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailServer {

    private final String username;
    private final String password;
    private final List<Email> sentEmails = new ArrayList<>();

    // Inner class to represent a sent email
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

    // Constructor that loads credentials from .env file
    public EmailServer() {
        try {
            Dotenv dotenv = Dotenv.load();
            this.username = dotenv.get("EMAIL_USERNAME");
            this.password = dotenv.get("EMAIL_PASSWORD");
            
            // Validate credentials
            if (username == null || username.isEmpty()) {
                throw new IllegalStateException("EMAIL_USERNAME not found in .env file");
            }
            if (password == null || password.isEmpty()) {
                throw new IllegalStateException("EMAIL_PASSWORD not found in .env file");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize EmailServer: " + e.getMessage(), e);
        }
    }

    public EmailServer(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void sendEmail(String to, String subject, String body) {
        // Validate credentials before attempting to send
        if (username == null || username.isEmpty()) {
            throw new IllegalStateException("Email username is not configured");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException("Email password is not configured");
        }
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException("Recipient email address cannot be null or empty");
        }

        // إعداد خصائص SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // إنشاء جلسة مع المصادقة
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // إنشاء رسالة
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            // إرسال الرسالة
            Transport.send(message);
            System.out.println("تم إرسال البريد بنجاح إلى: " + to);
            
            // Track the sent email
            sentEmails.add(new Email(to, subject, body));
        } catch (MessagingException e) {
            throw new RuntimeException("فشل في إرسال البريد: " + e.getMessage(), e);
        }
    }

    // Method to get list of sent emails (returns a copy to prevent external modification)
    public List<Email> getSentEmails() {
        return new ArrayList<>(sentEmails);
    }

    // Method to clear sent emails history
    public void clearSentEmails() {
        sentEmails.clear();
    }

    public static void run() {
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        Dotenv dotenv = Dotenv.load();
        String username = dotenv.get("EMAIL_USERNAME");
        String password = dotenv.get("EMAIL_PASSWORD");

        EmailServer emailService = new EmailServer(username, password);
        String subject = "hi ";
        String body = "hi";
        emailService.sendEmail("s12216975@stu.najah.edu", subject, body);
    }

    public static void main(String []s) {
        run();
    }
}