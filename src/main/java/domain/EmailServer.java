package domain;

import java.util.ArrayList;
import java.util.List;

// Mock Email Server for testing
 public class EmailServer {
    private List<Email> sentEmails = new ArrayList<>();

    public void sendEmail(String to, String subject, String body) {
        Email email = new Email(to, subject, body);
        sentEmails.add(email);
        System.out.println("Email sent to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }

    public List<Email> getSentEmails() {
        return new ArrayList<>(sentEmails);
    }

    public void clearSentEmails() {
        sentEmails.clear();
    }

    // Inner class to represent an email
    public static class Email {
        private String to;
        private String subject;
        private String body;

        public Email(String to, String subject, String body) {
            this.to = to;
            this.subject = subject;
            this.body = body;
        }

        public String getTo() { return to; }
        public String getSubject() { return subject; }
        public String getBody() { return body; }
    }
}
