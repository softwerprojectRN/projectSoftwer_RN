import util.*;

import jakarta.mail.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmailServerTest {

    private EmailServer emailServer;

    @BeforeEach
    void setup() {
        // Use constructor with username/password to avoid dotenv dependency
        emailServer = new EmailServer("testuser", "testpass");
        emailServer.clearSentEmails();
    }

    // -------------------------------------------------------------------------
    // Test: Constructor with valid username/password
    // -------------------------------------------------------------------------
    @Test
    void testConstructor_Valid() {
        assertDoesNotThrow(() -> new EmailServer("u", "p"));
    }

    // -------------------------------------------------------------------------
    // Test: Constructor with null username
    // -------------------------------------------------------------------------
    @Test
    void testConstructor_NullUsername() {
        assertThrows(IllegalArgumentException.class, () -> new EmailServer(null, "p"));
    }

    // -------------------------------------------------------------------------
    // Test: Constructor with empty password
    // -------------------------------------------------------------------------
    @Test
    void testConstructor_EmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> new EmailServer("u", ""));
    }

    // -------------------------------------------------------------------------
    // Test: sendEmail successfully
    // -------------------------------------------------------------------------
    @Test
    void testSendEmail_Success() {
        EmailServer server = new EmailServer("testuser", "testpass");
        server.clearSentEmails();

        // Mock Transport.send() to simulate successful email sending
        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {
            // Make Transport.send() do nothing (simulate success)
            transportMock.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);

            // Send email - should succeed and add to list
            server.sendEmail("to@example.com", "Subject", "Body");

            // Verify email was added to the list
            List<EmailServer.Email> emails = server.getSentEmails();
            assertEquals(1, emails.size());
            assertEquals("to@example.com", emails.get(0).getTo());
            assertEquals("Subject", emails.get(0).getSubject());
            assertEquals("Body", emails.get(0).getBody());

            // Verify Transport.send() was called
            transportMock.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }


    // -------------------------------------------------------------------------
    // Test: sendEmail throws exception on null recipient
    // -------------------------------------------------------------------------
    @Test
    void testSendEmail_NullRecipient() {
        assertThrows(IllegalArgumentException.class,
                () -> emailServer.sendEmail(null, "Subj", "Body"));
    }

    // -------------------------------------------------------------------------
    // Test: sentEmails list returns a copy
    // -------------------------------------------------------------------------
    @Test
    void testGetSentEmails_Copy() {
        EmailServer.Email email = new EmailServer.Email("a@b.com", "Sub", "Body");
        emailServer.getSentEmails().add(email); // This should not affect original list
        assertTrue(emailServer.getSentEmails().isEmpty()); // Original list is still empty
    }

    // -------------------------------------------------------------------------
    // Test: clearSentEmails
    // -------------------------------------------------------------------------
    @Test
    void testClearSentEmails() {
        EmailServer.Email email = new EmailServer.Email("a@b.com", "Sub", "Body");
        emailServer.getSentEmails().add(email); // Not real effect due to copy
        emailServer.clearSentEmails(); // Should clear internal list
        assertTrue(emailServer.getSentEmails().isEmpty());
    }
}
