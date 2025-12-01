
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import service.EmailServer;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServerTest {

    private EmailServer emailServerWithCredentials;

    @BeforeEach
    void setUp() {
        emailServerWithCredentials = new EmailServer("test@example.com", "password123");
    }

    @Test
    void testConstructorWithValidCredentials() {
        assertEquals("test@example.com", getUsername(emailServerWithCredentials));
        assertEquals("password123", getPassword(emailServerWithCredentials));
    }

    @Test
    void testConstructorWithNullUsername() {
        assertThrows(IllegalArgumentException.class, () -> new EmailServer(null, "password"));
    }

    @Test
    void testConstructorWithEmptyUsername() {
        assertThrows(IllegalArgumentException.class, () -> new EmailServer("", "password"));
    }

    @Test
    void testConstructorWithNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> new EmailServer("test@example.com", null));
    }

    @Test
    void testConstructorWithEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> new EmailServer("test@example.com", ""));
    }

    @Test
    void testConstructorWithDotenv() {
        try (MockedStatic<Dotenv> dotenvMockedStatic = mockStatic(Dotenv.class)) {
            DotenvBuilder mockBuilder = mock(DotenvBuilder.class);
            Dotenv mockDotenv = mock(Dotenv.class);

            dotenvMockedStatic.when(Dotenv::configure).thenReturn(mockBuilder);
            when(mockBuilder.ignoreIfMissing()).thenReturn(mockBuilder);
            when(mockBuilder.load()).thenReturn(mockDotenv);

            when(mockDotenv.get("EMAIL_USERNAME")).thenReturn("test@example.com");
            when(mockDotenv.get("EMAIL_PASSWORD")).thenReturn("password123");

            EmailServer server = new EmailServer();
            assertEquals("test@example.com", getUsername(server));
            assertEquals("password123", getPassword(server));
        }
    }

    @Test
    void testConstructorWithMissingDotenvUsername() {
        try (MockedStatic<Dotenv> dotenvMockedStatic = mockStatic(Dotenv.class)) {
            DotenvBuilder mockBuilder = mock(DotenvBuilder.class);
            Dotenv mockDotenv = mock(Dotenv.class);

            dotenvMockedStatic.when(Dotenv::configure).thenReturn(mockBuilder);
            when(mockBuilder.ignoreIfMissing()).thenReturn(mockBuilder);
            when(mockBuilder.load()).thenReturn(mockDotenv);

            when(mockDotenv.get("EMAIL_USERNAME")).thenReturn(null);
            when(mockDotenv.get("EMAIL_PASSWORD")).thenReturn("password123");

            IllegalStateException exception = assertThrows(IllegalStateException.class, EmailServer::new);
            // CORRECTED: The exception message is wrapped by the try-catch block in the constructor.
            assertEquals("Failed to initialize EmailServer: EMAIL_USERNAME not found in .env file", exception.getMessage());
        }
    }

    @Test
    void testConstructorWithMissingDotenvPassword() {
        try (MockedStatic<Dotenv> dotenvMockedStatic = mockStatic(Dotenv.class)) {
            DotenvBuilder mockBuilder = mock(DotenvBuilder.class);
            Dotenv mockDotenv = mock(Dotenv.class);

            dotenvMockedStatic.when(Dotenv::configure).thenReturn(mockBuilder);
            when(mockBuilder.ignoreIfMissing()).thenReturn(mockBuilder);
            when(mockBuilder.load()).thenReturn(mockDotenv);

            when(mockDotenv.get("EMAIL_USERNAME")).thenReturn("test@example.com");
            when(mockDotenv.get("EMAIL_PASSWORD")).thenReturn(null);

            IllegalStateException exception = assertThrows(IllegalStateException.class, EmailServer::new);
            // CORRECTED: The exception message is wrapped by the try-catch block in the constructor.
            assertEquals("Failed to initialize EmailServer: EMAIL_PASSWORD not found in .env file", exception.getMessage());
        }
    }

    @Test
    void testSendEmailSuccess() throws MessagingException {
        try (MockedStatic<Transport> transportMockedStatic = mockStatic(Transport.class)) {
            transportMockedStatic.when(() -> Transport.send(any())).thenAnswer(invocation -> null);

            emailServerWithCredentials.sendEmail("recipient@example.com", "Test Subject", "Test Body");

            List<EmailServer.Email> sentEmails = emailServerWithCredentials.getSentEmails();
            assertEquals(1, sentEmails.size());

            EmailServer.Email sentEmail = sentEmails.get(0);
            assertEquals("recipient@example.com", sentEmail.getTo());
            assertEquals("Test Subject", sentEmail.getSubject());
            assertEquals("Test Body", sentEmail.getBody());
            assertTrue(sentEmail.getTimestamp() > 0);

            transportMockedStatic.verify(() -> Transport.send(any()), times(1));
        }
    }

    @Test
    void testSendEmailWithNullRecipient() {
        assertThrows(IllegalArgumentException.class, () ->
                emailServerWithCredentials.sendEmail(null, "Test Subject", "Test Body"));
    }

    @Test
    void testSendEmailWithEmptyRecipient() {
        assertThrows(IllegalArgumentException.class, () ->
                emailServerWithCredentials.sendEmail("", "Test Subject", "Test Body"));
    }

    @Test
    void testSendEmailWithNullSubject() {
        try (MockedStatic<Transport> transportMockedStatic = mockStatic(Transport.class)) {
            transportMockedStatic.when(() -> Transport.send(any())).thenAnswer(invocation -> null);

            emailServerWithCredentials.sendEmail("recipient@example.com", null, "Test Body");

            List<EmailServer.Email> sentEmails = emailServerWithCredentials.getSentEmails();
            assertEquals(1, sentEmails.size());

            EmailServer.Email sentEmail = sentEmails.get(0);
            assertNull(sentEmail.getSubject());
        }
    }

    @Test
    void testSendEmailWithNullBody() {
        try (MockedStatic<Transport> transportMockedStatic = mockStatic(Transport.class)) {
            transportMockedStatic.when(() -> Transport.send(any())).thenAnswer(invocation -> null);

            emailServerWithCredentials.sendEmail("recipient@example.com", "Test Subject", null);

            List<EmailServer.Email> sentEmails = emailServerWithCredentials.getSentEmails();
            assertEquals(1, sentEmails.size());

            EmailServer.Email sentEmail = sentEmails.get(0);
            // CORRECTED: The EmailServer converts null bodies to empty strings.
            assertEquals("", sentEmail.getBody());
        }
    }

    @Test
    void testSendEmailFailure() throws MessagingException {
        try (MockedStatic<Transport> transportMockedStatic = mockStatic(Transport.class)) {
            transportMockedStatic.when(() -> Transport.send(any()))
                    .thenThrow(new MessagingException("Failed to send email"));

            assertThrows(RuntimeException.class, () ->
                    emailServerWithCredentials.sendEmail("recipient@example.com", "Test Subject", "Test Body"));

            List<EmailServer.Email> sentEmails = emailServerWithCredentials.getSentEmails();
            assertEquals(0, sentEmails.size());
        }
    }

    @Test
    void testGetSentEmailsReturnsCopy() {
        try (MockedStatic<Transport> transportMockedStatic = mockStatic(Transport.class)) {
            transportMockedStatic.when(() -> Transport.send(any())).thenAnswer(invocation -> null);

            emailServerWithCredentials.sendEmail("recipient1@example.com", "Subject 1", "Body 1");
            emailServerWithCredentials.sendEmail("recipient2@example.com", "Subject 2", "Body 2");

            List<EmailServer.Email> sentEmails = emailServerWithCredentials.getSentEmails();
            assertEquals(2, sentEmails.size());

            // Modify the returned list
            sentEmails.clear();

            // Verify the original list is not affected
            List<EmailServer.Email> sentEmailsAgain = emailServerWithCredentials.getSentEmails();
            assertEquals(2, sentEmailsAgain.size());
        }
    }

    @Test
    void testClearSentEmails() {
        try (MockedStatic<Transport> transportMockedStatic = mockStatic(Transport.class)) {
            transportMockedStatic.when(() -> Transport.send(any())).thenAnswer(invocation -> null);

            emailServerWithCredentials.sendEmail("recipient1@example.com", "Subject 1", "Body 1");
            emailServerWithCredentials.sendEmail("recipient2@example.com", "Subject 2", "Body 2");

            assertEquals(2, emailServerWithCredentials.getSentEmails().size());

            emailServerWithCredentials.clearSentEmails();
            assertEquals(0, emailServerWithCredentials.getSentEmails().size());
        }
    }

    @Test
    void testEmailClass() {
        EmailServer.Email email = new EmailServer.Email("recipient@example.com", "Test Subject", "Test Body");

        assertEquals("recipient@example.com", email.getTo());
        assertEquals("Test Subject", email.getSubject());
        assertEquals("Test Body", email.getBody());
        assertTrue(email.getTimestamp() > 0);
    }

    @Test
    void testEmailClassWithNullValues() {
        EmailServer.Email email = new EmailServer.Email(null, null, null);

        assertNull(email.getTo());
        assertNull(email.getSubject());
        assertNull(email.getBody());
        assertTrue(email.getTimestamp() > 0);
    }

    // Helper method to access private fields for testing
    private String getUsername(EmailServer server) {
        try {
            Field usernameField = EmailServer.class.getDeclaredField("username");
            usernameField.setAccessible(true);
            return (String) usernameField.get(server);
        } catch (Exception e) {
            fail("Failed to access username field: " + e.getMessage());
            return null;
        }
    }

    private String getPassword(EmailServer server) {
        try {
            Field passwordField = EmailServer.class.getDeclaredField("password");
            passwordField.setAccessible(true);
            return (String) passwordField.get(server);
        } catch (Exception e) {
            fail("Failed to access password field: " + e.getMessage());
            return null;
        }
    }
}