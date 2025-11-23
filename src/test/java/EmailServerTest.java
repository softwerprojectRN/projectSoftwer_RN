import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmailServerTest {

    private EmailServer emailServer;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    public void setUp() {
        // Use constructor with test credentials to avoid .env file dependency
        emailServer = new EmailServer("test@example.com", "test_password");
        // Capture System.out for testing
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
    }

    @AfterEach
    public void tearDown() {
        // Restore System.out after each test
        System.setOut(originalOut);
    }

    @Test
    public void testSendEmailSuccessfully_AddsEmailToList() throws MessagingException {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";
        
        // Mock Transport.send() to do nothing (simulate successful send)
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);
            
            // Act
            emailServer.sendEmail(to, subject, body);
            
            // Assert
            List<EmailServer.Email> sentEmails = emailServer.getSentEmails();
            assertEquals(1, sentEmails.size());
            assertEquals(to, sentEmails.get(0).getTo());
            assertEquals(subject, sentEmails.get(0).getSubject());
            assertEquals(body, sentEmails.get(0).getBody());
            
            // Verify Transport.send was called
            mockedTransport.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    @Test
    public void testSendEmailAddsEmailToList() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // Act & Assert - Note: This will fail to actually send email with test credentials,
        // but we can test that the validation and structure work correctly
        // The email is only added to the list AFTER successful sending
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailServer.sendEmail(to, subject, body);
        });
        
        // Verify exception is about sending failure, not validation
        assertTrue(exception.getMessage().contains("فشل في إرسال البريد") || 
                   exception.getMessage().contains("Failed to send email"));
        
        // Email should not be in list since sending failed
        assertEquals(0, emailServer.getSentEmails().size());
    }

    @Test
    public void testSendMultipleEmails() {
        // Arrange
        String[] recipients = {"user1@example.com", "user2@example.com", "user3@example.com"};
        String subject = "Library Notification";
        String body = "Your book is due soon";

        // Act & Assert - All will fail to send with test credentials
        for (String recipient : recipients) {
            assertThrows(RuntimeException.class, () -> {
                emailServer.sendEmail(recipient, subject, body);
            });
        }

        // No emails should be in list since all sending attempts failed
        assertEquals(0, emailServer.getSentEmails().size());
    }

    @Test
    public void testSendMultipleEmailsSuccessfully() throws MessagingException {
        // Arrange
        String[] recipients = {"user1@example.com", "user2@example.com", "user3@example.com"};
        String subject = "Library Notification";
        String body = "Your book is due soon";

        // Mock Transport.send() to simulate successful sends
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);
            
            // Act
            for (String recipient : recipients) {
                emailServer.sendEmail(recipient, subject, body);
            }

            // Assert
            List<EmailServer.Email> sentEmails = emailServer.getSentEmails();
            assertEquals(3, sentEmails.size());
            mockedTransport.verify(() -> Transport.send(any(Message.class)), times(3));
        }
    }

    @Test
    public void testGetSentEmailsReturnsCopy() {
        // Arrange - Since we can't actually send emails with test credentials,
        // we'll test the copy behavior with an empty list first
        List<EmailServer.Email> firstList = emailServer.getSentEmails();

        // Act
        firstList.clear(); // Modify the returned list

        // Assert - Original list should not be affected (both are empty in this case)
        List<EmailServer.Email> secondList = emailServer.getSentEmails();
        assertEquals(0, secondList.size()); // Both lists are empty, but copy behavior is verified
        
        // Test that getSentEmails returns a new list each time
        assertNotSame(firstList, secondList, "getSentEmails should return a new list each time");
    }

    @Test
    public void testClearSentEmails() throws MessagingException {
        // Arrange - Send an email first using mocked Transport
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);
            emailServer.sendEmail("test@example.com", "Test", "Body");
            assertEquals(1, emailServer.getSentEmails().size());

            // Act
            emailServer.clearSentEmails();

            // Assert
            assertEquals(0, emailServer.getSentEmails().size());
        }
    }

    @Test
    public void testEmailClassProperties() {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // Act
        long beforeCreation = System.currentTimeMillis();
        EmailServer.Email email = new EmailServer.Email(to, subject, body);
        long afterCreation = System.currentTimeMillis();

        // Assert
        assertEquals(to, email.getTo());
        assertEquals(subject, email.getSubject());
        assertEquals(body, email.getBody());
        assertTrue(email.getTimestamp() >= beforeCreation && email.getTimestamp() <= afterCreation,
            "Timestamp should be set to current time");
    }

    @Test
    public void testEmailServerConstructorWithCredentials() {
        // Arrange
        String username = "testuser@example.com";
        String password = "testpass123";

        // Act
        EmailServer server = new EmailServer(username, password);

        // Assert
        assertNotNull(server);
        // Verify it can validate (will fail to send but validates structure)
        assertThrows(RuntimeException.class, () -> {
            server.sendEmail("recipient@example.com", "Test", "Body");
        });
    }

    @Test
    public void testEmailServerNoArgConstructor_WithValidEnv() {
        // Arrange - Mock Dotenv to return valid credentials
        try (MockedStatic<Dotenv> mockedDotenv = Mockito.mockStatic(Dotenv.class)) {
            Dotenv mockDotenv = mock(Dotenv.class);
            when(mockDotenv.get("EMAIL_USERNAME")).thenReturn("envuser@example.com");
            when(mockDotenv.get("EMAIL_PASSWORD")).thenReturn("envpassword123");
            mockedDotenv.when(Dotenv::load).thenReturn(mockDotenv);

            // Act
            EmailServer server = new EmailServer();

            // Assert
            assertNotNull(server);
            // Verify it can be used (will fail to send but validates structure)
            assertThrows(RuntimeException.class, () -> {
                server.sendEmail("recipient@example.com", "Test", "Body");
            });
        }
    }

    @Test
    public void testEmailServerNoArgConstructor_MissingUsername() {
        // Arrange - Mock Dotenv to return null username
        try (MockedStatic<Dotenv> mockedDotenv = Mockito.mockStatic(Dotenv.class)) {
            Dotenv mockDotenv = mock(Dotenv.class);
            when(mockDotenv.get("EMAIL_USERNAME")).thenReturn(null);
            when(mockDotenv.get("EMAIL_PASSWORD")).thenReturn("envpassword123");
            mockedDotenv.when(Dotenv::load).thenReturn(mockDotenv);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                new EmailServer();
            });
            assertTrue(exception.getMessage().contains("EMAIL_USERNAME not found in .env file"));
        }
    }

    @Test
    public void testEmailServerNoArgConstructor_EmptyUsername() {
        // Arrange - Mock Dotenv to return empty username
        try (MockedStatic<Dotenv> mockedDotenv = Mockito.mockStatic(Dotenv.class)) {
            Dotenv mockDotenv = mock(Dotenv.class);
            when(mockDotenv.get("EMAIL_USERNAME")).thenReturn("");
            when(mockDotenv.get("EMAIL_PASSWORD")).thenReturn("envpassword123");
            mockedDotenv.when(Dotenv::load).thenReturn(mockDotenv);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                new EmailServer();
            });
            assertTrue(exception.getMessage().contains("EMAIL_USERNAME not found in .env file"));
        }
    }

    @Test
    public void testEmailServerNoArgConstructor_MissingPassword() {
        // Arrange - Mock Dotenv to return null password
        try (MockedStatic<Dotenv> mockedDotenv = Mockito.mockStatic(Dotenv.class)) {
            Dotenv mockDotenv = mock(Dotenv.class);
            when(mockDotenv.get("EMAIL_USERNAME")).thenReturn("envuser@example.com");
            when(mockDotenv.get("EMAIL_PASSWORD")).thenReturn(null);
            mockedDotenv.when(Dotenv::load).thenReturn(mockDotenv);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                new EmailServer();
            });
            assertTrue(exception.getMessage().contains("EMAIL_PASSWORD not found in .env file"));
        }
    }

    @Test
    public void testEmailServerNoArgConstructor_EmptyPassword() {
        // Arrange - Mock Dotenv to return empty password
        try (MockedStatic<Dotenv> mockedDotenv = Mockito.mockStatic(Dotenv.class)) {
            Dotenv mockDotenv = mock(Dotenv.class);
            when(mockDotenv.get("EMAIL_USERNAME")).thenReturn("envuser@example.com");
            when(mockDotenv.get("EMAIL_PASSWORD")).thenReturn("");
            mockedDotenv.when(Dotenv::load).thenReturn(mockDotenv);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                new EmailServer();
            });
            assertTrue(exception.getMessage().contains("EMAIL_PASSWORD not found in .env file"));
        }
    }

    @Test
    public void testEmailServerNoArgConstructor_DotenvLoadException() {
        // Arrange - Mock Dotenv.load() to throw exception
        try (MockedStatic<Dotenv> mockedDotenv = Mockito.mockStatic(Dotenv.class)) {
            mockedDotenv.when(Dotenv::load).thenThrow(new RuntimeException("Failed to load .env file"));

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                new EmailServer();
            });
            assertTrue(exception.getMessage().contains("Failed to initialize EmailServer"));
        }
    }

    @Test
    public void testSendEmailWithEmptyFields() {
        // Arrange
        String to = "";
        String subject = "";
        String body = "";

        // Act & Assert - should throw IllegalArgumentException for empty recipient
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailServer.sendEmail(to, subject, body);
        });
        
        assertTrue(exception.getMessage().contains("Recipient email address cannot be null or empty"));
        assertEquals(0, emailServer.getSentEmails().size());
    }

    @Test
    public void testSendEmailWithNullValues() {
        // Arrange
        String to = null;
        String subject = null;
        String body = null;

        // Act & Assert - should throw IllegalArgumentException for null recipient
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailServer.sendEmail(to, subject, body);
        });
        
        assertTrue(exception.getMessage().contains("Recipient email address cannot be null or empty"));
        assertEquals(0, emailServer.getSentEmails().size());
    }

    @Test
    public void testSendEmailWithSpecialCharacters() throws MessagingException {
        // Arrange
        String to = "test+special@example.com";
        String subject = "Subject with émojis 📚";
        String body = "Body with\nnewlines and\ttabs and \"quotes\"";

        // Mock Transport.send() to simulate successful send
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);
            
            // Act
            emailServer.sendEmail(to, subject, body);
            
            // Assert
            List<EmailServer.Email> sentEmails = emailServer.getSentEmails();
            assertEquals(1, sentEmails.size());
            assertEquals(to, sentEmails.get(0).getTo());
            assertEquals(subject, sentEmails.get(0).getSubject());
            assertEquals(body, sentEmails.get(0).getBody());
        }
    }

    @Test
    public void testSendEmailWithSpecialCharacters_Failure() {
        // Arrange
        String to = "test+special@example.com";
        String subject = "Subject with émojis 📚";
        String body = "Body with\nnewlines and\ttabs and \"quotes\"";

        // Act & Assert - Will fail to send with test credentials, but validates input handling
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailServer.sendEmail(to, subject, body);
        });
        
        // Verify exception is about sending failure, not validation
        assertTrue(exception.getMessage().contains("فشل في إرسال البريد") || 
                   exception.getMessage().contains("Failed to send email"));
        
        // Email should not be in list since sending failed
        assertEquals(0, emailServer.getSentEmails().size());
    }

    @Test
    public void testSendEmailWithNullUsername() {
        // Arrange - Create EmailServer with null username
        EmailServer serverWithNullUsername = new EmailServer(null, "password");

        // Act & Assert - should throw IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            serverWithNullUsername.sendEmail("recipient@example.com", "Subject", "Body");
        });
        
        assertTrue(exception.getMessage().contains("Email username is not configured"));
        assertEquals(0, serverWithNullUsername.getSentEmails().size());
    }

    @Test
    public void testSendEmailWithEmptyUsername() {
        // Arrange - Create EmailServer with empty username
        EmailServer serverWithEmptyUsername = new EmailServer("", "password");

        // Act & Assert - should throw IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            serverWithEmptyUsername.sendEmail("recipient@example.com", "Subject", "Body");
        });
        
        assertTrue(exception.getMessage().contains("Email username is not configured"));
        assertEquals(0, serverWithEmptyUsername.getSentEmails().size());
    }

    @Test
    public void testSendEmailWithNullPassword() {
        // Arrange - Create EmailServer with null password
        EmailServer serverWithNullPassword = new EmailServer("user@example.com", null);

        // Act & Assert - should throw IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            serverWithNullPassword.sendEmail("recipient@example.com", "Subject", "Body");
        });
        
        assertTrue(exception.getMessage().contains("Email password is not configured"));
        assertEquals(0, serverWithNullPassword.getSentEmails().size());
    }

    @Test
    public void testSendEmailWithEmptyPassword() {
        // Arrange - Create EmailServer with empty password
        EmailServer serverWithEmptyPassword = new EmailServer("user@example.com", "");

        // Act & Assert - should throw IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            serverWithEmptyPassword.sendEmail("recipient@example.com", "Subject", "Body");
        });
        
        assertTrue(exception.getMessage().contains("Email password is not configured"));
        assertEquals(0, serverWithEmptyPassword.getSentEmails().size());
    }

    @Test
    public void testSendEmailWithMessagingException() throws MessagingException {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // Mock Transport.send() to throw MessagingException
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("SMTP server error"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                emailServer.sendEmail(to, subject, body);
            });
            
            assertTrue(exception.getMessage().contains("فشل في إرسال البريد"));
            assertEquals(0, emailServer.getSentEmails().size());
        }
    }

    @Test
    public void testGetSentEmailsReturnsCopyWithActualEmails() throws MessagingException {
        // This test verifies that getSentEmails returns a copy even when there are emails
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);
            
            // Send an email
            emailServer.sendEmail("test@example.com", "Test", "Body");
            
            // Get the list via public method
            List<EmailServer.Email> firstList = emailServer.getSentEmails();
            assertEquals(1, firstList.size());
            
            // Modify the returned list
            firstList.clear();
            
            // Get the list again - should still have the email
            List<EmailServer.Email> secondList = emailServer.getSentEmails();
            assertEquals(1, secondList.size(), "Modifying returned list should not affect internal list");
            assertNotSame(firstList, secondList, "getSentEmails should return a new list each time");
        }
    }

    @Test
    public void testRun() {
        // Arrange - Mock Dotenv and Transport to allow run() to execute
        try (MockedStatic<Dotenv> mockedDotenv = Mockito.mockStatic(Dotenv.class);
             MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            
            Dotenv mockDotenv = mock(Dotenv.class);
            when(mockDotenv.get("EMAIL_USERNAME")).thenReturn("test@example.com");
            when(mockDotenv.get("EMAIL_PASSWORD")).thenReturn("testpassword");
            mockedDotenv.when(Dotenv::load).thenReturn(mockDotenv);
            
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);
            
            // Capture System.out
            System.setOut(new PrintStream(outContent));
            
            // Act
            EmailServer.run();
            
            // Assert - Verify that run() executed without throwing exception
            // (The output will contain the working directory message)
            String output = outContent.toString();
            assertTrue(output.contains("Current working directory:") || output.length() > 0);
            
            // Reset System.out
            System.setOut(originalOut);
        }
    }

    @Test
    public void testMain() {
        // Arrange - Mock Dotenv and Transport to allow main() to execute
        try (MockedStatic<Dotenv> mockedDotenv = Mockito.mockStatic(Dotenv.class);
             MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            
            Dotenv mockDotenv = mock(Dotenv.class);
            when(mockDotenv.get("EMAIL_USERNAME")).thenReturn("test@example.com");
            when(mockDotenv.get("EMAIL_PASSWORD")).thenReturn("testpassword");
            mockedDotenv.when(Dotenv::load).thenReturn(mockDotenv);
            
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(invocation -> null);
            
            // Capture System.out
            System.setOut(new PrintStream(outContent));
            
            // Act
            EmailServer.main(new String[]{});
            
            // Assert - Verify that main() executed without throwing exception
            // (The output will contain the working directory message)
            String output = outContent.toString();
            assertTrue(output.contains("Current working directory:") || output.length() > 0);
            
            // Reset System.out
            System.setOut(originalOut);
        }
    }
}
