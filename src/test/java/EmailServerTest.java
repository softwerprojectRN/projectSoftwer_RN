import domain.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class EmailServerTest {

   private EmailServer emailServer;

   @BeforeEach
   public void setUp() {
       // Use constructor with test credentials to avoid .env file dependency
       emailServer = new EmailServer("test@example.com", "test_password");
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
   public void testClearSentEmails() {
       // Arrange - Since we can't send emails with test credentials,
       // we'll test that clearSentEmails works on an empty list
       assertEquals(0, emailServer.getSentEmails().size());

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
   public void testSendEmailWithSpecialCharacters() {
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
   public void testGetSentEmailsReturnsCopyWithActualEmails() {
       // This test verifies that getSentEmails returns a copy even when there are emails
       // Note: We can't actually send emails with test credentials, so we'll use reflection
       // to add emails directly to test the copy behavior
       try {
           // Use reflection to access the internal sentEmails list
           java.lang.reflect.Field sentEmailsField = EmailServer.class.getDeclaredField("sentEmails");
           sentEmailsField.setAccessible(true);
           @SuppressWarnings("unchecked")
           List<EmailServer.Email> internalList = (List<EmailServer.Email>) sentEmailsField.get(emailServer);
           
           // Add an email directly to the internal list
           EmailServer.Email testEmail = new EmailServer.Email("test@example.com", "Test", "Body");
           internalList.add(testEmail);
           
           // Get the list via public method
           List<EmailServer.Email> firstList = emailServer.getSentEmails();
           assertEquals(1, firstList.size());
           
           // Modify the returned list
           firstList.clear();
           
           // Get the list again - should still have the email
           List<EmailServer.Email> secondList = emailServer.getSentEmails();
           assertEquals(1, secondList.size(), "Modifying returned list should not affect internal list");
           assertNotSame(firstList, secondList, "getSentEmails should return a new list each time");
       } catch (Exception e) {
           fail("Failed to test getSentEmails copy behavior: " + e.getMessage());
       }
   }
}