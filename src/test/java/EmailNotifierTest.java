import domain.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
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

       // Act
       emailNotifier.notify(null, message);

       // Assert - EmailNotifier now checks for null user and returns early
       // sendEmail should NOT be called when user is null
       verify(mockEmailServer, never()).sendEmail(
               anyString(),
               anyString(),
               anyString()
       );
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
               isNull()
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

   // Additional tests for complete coverage
   @Test
   public void testNotifyWithNullEmailServer() {
       // Arrange
       EmailNotifier notifierWithNullServer = new EmailNotifier(null);
       String message = "Test message";

       // Act
       notifierWithNullServer.notify(testUser, message);

       // Assert - Should return early without calling sendEmail
       // Since emailServer is null, sendEmail should never be called
       // (We can't verify on a null object, but we verify no exception is thrown)
       assertDoesNotThrow(() -> notifierWithNullServer.notify(testUser, message));
   }

   @Test
   public void testNotifyWithUserNullUsername() {
       // Arrange - Create a user with null username
       User userWithNullUsername = new User(null, "hash", "salt");
       String message = "Test message";

       // Act
       emailNotifier.notify(userWithNullUsername, message);

       // Assert - EmailNotifier checks for null username and returns early
       verify(mockEmailServer, never()).sendEmail(
               anyString(),
               anyString(),
               anyString()
       );
   }

   @Test
   public void testNotifyWithEmailServerException() {
       // Arrange
       String message = "Test message";
       doThrow(new RuntimeException("Email send failed")).when(mockEmailServer)
           .sendEmail(anyString(), anyString(), anyString());

       // Act & Assert
       RuntimeException exception = assertThrows(RuntimeException.class, () -> {
           emailNotifier.notify(testUser, message);
       });

       assertTrue(exception.getMessage().contains("Failed to send email notification"));
       verify(mockEmailServer, times(1)).sendEmail(
               anyString(),
               anyString(),
               anyString()
       );
   }

   @Test
   public void testNotifyWithEmptyUsername() {
       // Arrange
       User userWithEmptyUsername = new User("", "hash", "salt");
       String message = "Test message";
       String expectedEmail = "@example.com";

       // Act
       emailNotifier.notify(userWithEmptyUsername, message);

       // Assert - Empty username is allowed, email will be "@example.com"
       verify(mockEmailServer, times(1)).sendEmail(
               eq(expectedEmail),
               eq("part 3 Library Notification"),
               eq(message)
       );
   }

   @Test
   public void testNotifyConstructor() {
       // Arrange
       EmailServer server = mock(EmailServer.class);

       // Act
       EmailNotifier notifier = new EmailNotifier(server);

       // Assert
       assertNotNull(notifier);
   }
}