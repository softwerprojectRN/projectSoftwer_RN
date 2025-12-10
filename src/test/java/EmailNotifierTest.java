import service.*;

import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class EmailNotifierTest {

    private EmailServer emailServer;
    private EmailNotifier emailNotifier;

    @BeforeEach
    void setup() {
        emailServer = mock(EmailServer.class);
        emailNotifier = new EmailNotifier(emailServer);
    }

    // -------------------------------------------------------------------------
    // Case 1: Email server is null
    // -------------------------------------------------------------------------
    @Test
    void testNotify_EmailServerNull() {
        EmailNotifier notifier = new EmailNotifier(null);
        User user = new User(1, "john", "pass", "salt");

        // Should not throw an exception
        assertDoesNotThrow(() -> notifier.notify(user, "Test message"));
    }

    // -------------------------------------------------------------------------
    // Case 2: User is null
    // -------------------------------------------------------------------------
    @Test
    void testNotify_UserNull() {
        assertDoesNotThrow(() -> emailNotifier.notify(null, "Hello"));
        verify(emailServer, never()).sendEmail(anyString(), anyString(), anyString());
    }

    // -------------------------------------------------------------------------
    // Case 3: Username is null
    // -------------------------------------------------------------------------
    @Test
    void testNotify_UsernameNull() {
        User user = new User(1, null, "hash", "salt");

        assertDoesNotThrow(() -> emailNotifier.notify(user, "Hello"));
        verify(emailServer, never()).sendEmail(anyString(), anyString(), anyString());
    }

    // -------------------------------------------------------------------------
    // Case 4: Successful email sending
    // -------------------------------------------------------------------------
    @Test
    void testNotify_Success() {
        User user = new User(10, "rahaf", "hash", "salt");

        assertDoesNotThrow(() -> emailNotifier.notify(user, "Your book is overdue!"));

        verify(emailServer, times(1))
                .sendEmail("rahaf@example.com", "Library Notification", "Your book is overdue!");
    }

    // -------------------------------------------------------------------------
    // Case 5: Email server throws exception
    // -------------------------------------------------------------------------
    @Test
    void testNotify_EmailServerThrowsException() {
        User user = new User(10, "rahaf", "hash", "salt");

        doThrow(new RuntimeException("SMTP Error"))
                .when(emailServer)
                .sendEmail(anyString(), anyString(), anyString());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                emailNotifier.notify(user, "Error test")
        );

        assertTrue(ex.getMessage().contains("Failed to send email notification"));
    }
}
