import domain.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class EmailServerTest {

    private EmailServer emailServer;

    @BeforeEach
    public void setUp() {
        emailServer = new EmailServer();
    }

    @Test
    public void testSendEmailAddsEmailToList() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // Act
        emailServer.sendEmail(to, subject, body);

        // Assert
        List<EmailServer.Email> sentEmails = emailServer.getSentEmails();
        assertEquals(1, sentEmails.size());

        EmailServer.Email sentEmail = sentEmails.get(0);
        assertEquals(to, sentEmail.getTo());
        assertEquals(subject, sentEmail.getSubject());
        assertEquals(body, sentEmail.getBody());
    }

    @Test
    public void testSendMultipleEmails() {
        // Arrange
        String[] recipients = {"user1@example.com", "user2@example.com", "user3@example.com"};
        String subject = "Library Notification";
        String body = "Your book is due soon";

        // Act
        for (String recipient : recipients) {
            emailServer.sendEmail(recipient, subject, body);
        }

        // Assert
        List<EmailServer.Email> sentEmails = emailServer.getSentEmails();
        assertEquals(3, sentEmails.size());

        for (int i = 0; i < recipients.length; i++) {
            EmailServer.Email sentEmail = sentEmails.get(i);
            assertEquals(recipients[i], sentEmail.getTo());
            assertEquals(subject, sentEmail.getSubject());
            assertEquals(body, sentEmail.getBody());
        }
    }

    @Test
    public void testGetSentEmailsReturnsCopy() {
        // Arrange
        emailServer.sendEmail("test@example.com", "Subject", "Body");
        List<EmailServer.Email> firstList = emailServer.getSentEmails();

        // Act
        firstList.clear(); // Modify the returned list

        // Assert
        List<EmailServer.Email> secondList = emailServer.getSentEmails();
        assertEquals(1, secondList.size()); // Original list should not be affected
    }

    @Test
    public void testClearSentEmails() {
        // Arrange
        emailServer.sendEmail("test1@example.com", "Subject 1", "Body 1");
        emailServer.sendEmail("test2@example.com", "Subject 2", "Body 2");
        assertEquals(2, emailServer.getSentEmails().size());

        // Act
        emailServer.clearSentEmails();

        // Assert
        assertEquals(0, emailServer.getSentEmails().size());
    }

    @Test
    public void testClearEmptyEmailList() {
        // Arrange - list is already empty

        // Act
        emailServer.clearSentEmails();

        // Assert
        assertEquals(0, emailServer.getSentEmails().size());
    }

    @Test
    public void testEmailClassProperties() {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // Act
        EmailServer.Email email = new EmailServer.Email(to, subject, body);

        // Assert
        assertEquals(to, email.getTo());
        assertEquals(subject, email.getSubject());
        assertEquals(body, email.getBody());
    }

    @Test
    public void testSendEmailWithEmptyFields() {
        // Arrange
        String to = "";
        String subject = "";
        String body = "";

        // Act
        emailServer.sendEmail(to, subject, body);

        // Assert
        List<EmailServer.Email> sentEmails = emailServer.getSentEmails();
        assertEquals(1, sentEmails.size());

        EmailServer.Email sentEmail = sentEmails.get(0);
        assertEquals(to, sentEmail.getTo());
        assertEquals(subject, sentEmail.getSubject());
        assertEquals(body, sentEmail.getBody());
    }

    @Test
    public void testSendEmailWithNullValues() {
        // Arrange
        String to = null;
        String subject = null;
        String body = null;

        // Act
        emailServer.sendEmail(to, subject, body);

        // Assert
        List<EmailServer.Email> sentEmails = emailServer.getSentEmails();
        assertEquals(1, sentEmails.size());

        EmailServer.Email sentEmail = sentEmails.get(0);
        assertEquals(to, sentEmail.getTo());
        assertEquals(subject, sentEmail.getSubject());
        assertEquals(body, sentEmail.getBody());
    }

    @Test
    public void testSendEmailWithSpecialCharacters() {
        // Arrange
        String to = "test+special@example.com";
        String subject = "Subject with émojis 📚";
        String body = "Body with\nnewlines and\ttabs and \"quotes\"";

        // Act
        emailServer.sendEmail(to, subject, body);

        // Assert
        List<EmailServer.Email> sentEmails = emailServer.getSentEmails();
        assertEquals(1, sentEmails.size());

        EmailServer.Email sentEmail = sentEmails.get(0);
        assertEquals(to, sentEmail.getTo());
        assertEquals(subject, sentEmail.getSubject());
        assertEquals(body, sentEmail.getBody());
    }
}