import domain.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class EmailNotifierTest {

    @Mock
    private EmailServer mockEmailServer;

    private EmailNotifier emailNotifier;
    private User testUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        emailNotifier = new EmailNotifier(mockEmailServer);
        testUser = new User("john_doe","1234");
    }

    @Test
    public void testNotifySendsEmailWithCorrectAddress() {
        // Arrange
        String message = "Your book is due tomorrow";
        String expectedEmail = "john_doe@example.com";

        // Act
        emailNotifier.notify(testUser, message);

        // Assert
        verify(mockEmailServer, times(1)).sendEmail(
                eq(expectedEmail),
                eq("part 3 Library Notification"),
                eq(message)
        );
    }

    @Test
    public void testNotifySendsEmailWithCorrectSubject() {
        // Arrange
        String message = "Your book is due tomorrow";

        // Act
        emailNotifier.notify(testUser, message);

        // Assert
        verify(mockEmailServer, times(1)).sendEmail(
                anyString(),
                eq("part 3 Library Notification"),
                eq(message)
        );
    }

    @Test
    public void testNotifySendsEmailWithCorrectMessage() {
        // Arrange
        String message = "Your book is due tomorrow";

        // Act
        emailNotifier.notify(testUser, message);

        // Assert
        verify(mockEmailServer, times(1)).sendEmail(
                anyString(),
                anyString(),
                eq(message)
        );
    }

    @Test
    public void testNotifyWithDifferentUser() {
        // Arrange
        User differentUser = new User("jane_smith","1234");
        String message = "Your reservation is ready";
        String expectedEmail = "jane_smith@example.com";

        // Act
        emailNotifier.notify(differentUser, message);

        // Assert
        verify(mockEmailServer, times(1)).sendEmail(
                eq(expectedEmail),
                eq("part 3 Library Notification"),
                eq(message)
        );
    }

    @Test
    public void testNotifyWithEmptyMessage() {
        // Arrange
        String emptyMessage = "";

        // Act
        emailNotifier.notify(testUser, emptyMessage);

        // Assert
        verify(mockEmailServer, times(1)).sendEmail(
                anyString(),
                anyString(),
                eq(emptyMessage)
        );
    }

    @Test
    public void testNotifyWithNullUser() {
        // Arrange
        String message = "Test message";

        // Act & Assert
        try {
            emailNotifier.notify(null, message);
            // If no exception is thrown, verify that sendEmail was called
            verify(mockEmailServer, times(1)).sendEmail(
                    anyString(),
                    anyString(),
                    eq(message)
            );
        } catch (NullPointerException e) {
            // Expected behavior if null user causes an exception
        }
    }

    @Test
    public void testNotifyWithNullMessage() {
        // Arrange

        // Act
        emailNotifier.notify(testUser, null);

        // Assert
        verify(mockEmailServer, times(1)).sendEmail(
                anyString(),
                anyString(),
                eq(null)
        );
    }

    @Test
    public void testNotifyCallsEmailServerExactlyOnce() {
        // Arrange
        String message = "Test message";

        // Act
        emailNotifier.notify(testUser, message);

        // Assert
        verify(mockEmailServer, times(1)).sendEmail(
                anyString(),
                anyString(),
                anyString()
        );
    }
}