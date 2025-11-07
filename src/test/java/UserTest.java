import domain.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserTest {

    private MockedStatic<DriverManager> mockedDriverManager;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private Statement mockStatement;

    @BeforeEach
    void setUp() throws SQLException {
        mockedDriverManager = Mockito.mockStatic(DriverManager.class, invocation -> null);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockStatement = mock(Statement.class);

        mockedDriverManager.when(() -> DriverManager.getConnection("jdbc:sqlite:database.db"))
                .thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Assume success for inserts
        when(mockStatement.execute(anyString())).thenReturn(false); // For CREATE TABLE
    }

    @AfterEach
    void tearDown() {
        if (mockedDriverManager != null) {
            mockedDriverManager.close();
        }
    }

    @Test
    void testRegister_NewUser_Success() throws SQLException {
        // Arrange
        String username = "testuser";
        String password = "testpass";
        when(mockResultSet.next()).thenReturn(false); // User does not exist

        // Act
        User user = User.register(username, password);

        // Assert
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        verify(mockPreparedStatement).executeQuery();
        verify(mockPreparedStatement).executeUpdate();
        verify(mockConnection).prepareStatement(endsWith("WHERE username = ?"));
        verify(mockConnection).prepareStatement(endsWith("VALUES (?, ?, ?)"));
    }

    @Test
    void testRegister_ExistingUser_Failure() throws SQLException {
        // Arrange
        String username = "existinguser";
        String password = "testpass";
        when(mockResultSet.next()).thenReturn(true); // User exists

        // Act
        User user = User.register(username, password);

        // Assert
        assertNull(user);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testRegister_DatabaseError_Failure() throws SQLException {
        // Arrange
        String username = "testuser";
        String password = "testpass";
        SQLException dbError = new SQLException("DB Error");
        when(mockConnection.prepareStatement(anyString())).thenThrow(dbError);

        // Act
        User user = User.register(username, password);

        // Assert
        assertNull(user);
    }

    @Test
    void testRegister_InsertDatabaseError_Failure() throws SQLException {
        // Arrange
        String username = "newuser";
        String password = "testpass";
        SQLException insertError = new SQLException("Insert DB Error");
        when(mockResultSet.next()).thenReturn(false); // User does not exist, select succeeds
        // First prepareStatement (select) returns mock, second (insert) throws
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement).thenThrow(insertError);

        // Act
        User user = User.register(username, password);

        // Assert
        assertNull(user);
        verify(mockPreparedStatement, times(1)).executeQuery(); // Only select query executed
        verify(mockConnection, times(2)).prepareStatement(anyString()); // Both prepares attempted
    }

    @Test
    void testLogin_ValidCredentials_Success() throws SQLException {
        // Arrange
        String username = "validuser";
        String password = "validpass";
        String salt = "dGVzdHNhbHQ="; // Base64 of "testsalt"
        String storedHash = User.hashPassword(password, salt);

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password_hash")).thenReturn(storedHash);
        when(mockResultSet.getString("salt")).thenReturn(salt);

        // Act
        User user = User.login(username, password);

        // Assert
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertTrue(user.isLoggedIn());
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testLogin_InvalidPassword_Failure() throws SQLException {
        // Arrange
        String username = "user";
        String correctPassword = "correctpass";
        String wrongPassword = "wrongpass";
        String salt = "dGVzdHNhbHQ="; // Base64 of "testsalt"
        String storedHash = User.hashPassword(correctPassword, salt);

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password_hash")).thenReturn(storedHash);
        when(mockResultSet.getString("salt")).thenReturn(salt);

        // Act
        User user = User.login(username, wrongPassword);

        // Assert
        assertNull(user);
    }

    @Test
    void testLogin_NonExistentUser_Failure() throws SQLException {
        // Arrange
        String username = "nonexistent";
        String password = "pass";

        when(mockResultSet.next()).thenReturn(false);

        // Act
        User user = User.login(username, password);

        // Assert
        assertNull(user);
    }

    @Test
    void testLogin_DatabaseError_Failure() throws SQLException {
        // Arrange
        String username = "user";
        String password = "pass";
        SQLException dbError = new SQLException("Login Error");
        when(mockConnection.prepareStatement(anyString())).thenThrow(dbError);

        // Act
        User user = User.login(username, password);

        // Assert
        assertNull(user);
    }

    @Test
    void testLogout_LoggedInUser_Success() {
        // Arrange
        User user = new User("test", "hash", "salt");
        user.setLoggedIn(true);

        // Act
        user.logout();

        // Assert
        assertFalse(user.isLoggedIn());
    }

    @Test
    void testLogout_NotLoggedIn_NoOp() {
        // Arrange
        User user = new User("test", "hash", "salt");
        user.setLoggedIn(false);

        // Act
        user.logout();

        // Assert
        assertFalse(user.isLoggedIn());
    }

    @Test
    void testIsLoggedIn() {
        // Arrange
        User user = new User("test", "hash", "salt");
        user.setLoggedIn(true);

        // Act & Assert
        assertTrue(user.isLoggedIn());

        user.setLoggedIn(false);
        assertFalse(user.isLoggedIn());
    }

    @Test
    void testGetUsername() {
        // Arrange
        String username = "myuser";
        User user = new User(username, "hash", "salt");

        // Act & Assert
        assertEquals(username, user.getUsername());
    }

    @Test
    void testSetUsername() {
        // Arrange
        User user = new User("old", "hash", "salt");

        // Act
        user.setUsername("newuser");

        // Assert
        assertEquals("newuser", user.getUsername());
    }

    @Test
    void testGetPasswordHash() {
        // Arrange
        String hash = "myhash";
        User user = new User("user", hash, "salt");

        // Act & Assert
        assertEquals(hash, user.getPasswordHash());
    }

    @Test
    void testHashPassword_WithSalt() {
        // Arrange
        String password = "testpass";
        String salt = "testsalt";

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedBytes = md.digest(password.getBytes());
            String expectedHash = Base64.getEncoder().encodeToString(hashedBytes);

            // Act
            String actualHash = User.hashPassword(password, salt);

            // Assert
            assertEquals(expectedHash, actualHash);
        } catch (NoSuchAlgorithmException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testHashPassword_NoSuchAlgorithmException_ThrowsRuntimeException() throws NoSuchAlgorithmException {
        // Arrange & Act & Assert
        try (MockedStatic<MessageDigest> mockedDigest = Mockito.mockStatic(MessageDigest.class)) {
            mockedDigest.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("Algorithm not found"));

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                User.hashPassword("testpass", "testsalt");
            });

            assertTrue(exception.getMessage().contains("Hashing error"));
        }
    }

    @Test
    void testGenerateSalt_NotEmpty() {
        // Act
        String salt = User.generateSalt();

        // Assert
        assertNotNull(salt);
        assertFalse(salt.isEmpty());
        assertEquals(24, salt.length()); // Base64 of 16 bytes
    }

    @Test
    void testConstructor_Protected() {
        // Act
        User user = new User("user", "hash", "salt");

        // Assert
        assertEquals("user", user.getUsername());
        assertEquals("hash", user.getPasswordHash());
        assertFalse(user.isLoggedIn());
    }

    @Test
    void testPublicConstructor() {
        // Act
        User user = new User("user", "pass"); // Note: This sets passwordHash to plain password, as per code

        // Assert
        assertEquals("user", user.getUsername());
        assertEquals("pass", user.getPasswordHash());
        assertFalse(user.isLoggedIn());
    }

    @Test
    void testConnect_ReturnsMockConnection() throws SQLException {
        mockedDriverManager.reset();
        mockedDriverManager.when(() -> DriverManager.getConnection("jdbc:sqlite:database.db"))
                .thenReturn(mockConnection);

        Connection connection = User.connect();

        assertNotNull(connection);
        assertEquals(mockConnection, connection);
        mockedDriverManager.verify(() -> DriverManager.getConnection("jdbc:sqlite:database.db"), times(1));
    }

    @Test
    void testConnect_DatabaseError_ReturnsNull() throws SQLException {
        mockedDriverManager.reset();
        mockedDriverManager.when(() -> DriverManager.getConnection("jdbc:sqlite:database.db"))
                .thenAnswer(invocation -> { throw new SQLException("Connection failure"); });

        Connection connection = User.connect();

        assertNull(connection);
        mockedDriverManager.verify(() -> DriverManager.getConnection("jdbc:sqlite:database.db"), times(1));
    }

    @Test
    void testLogout_MultipleCalls() {
        // Arrange
        User user = new User("test", "hash", "salt");
        user.setLoggedIn(true);

        // Act
        user.logout();
        boolean firstLogout = user.isLoggedIn();
        user.logout();
        boolean secondLogout = user.isLoggedIn();

        // Assert
        assertFalse(firstLogout);
        assertFalse(secondLogout);
    }

    @Test
    void testSetUsername_NullInput() {
        // Arrange
        User user = new User("old", "hash", "salt");

        // Act
        user.setUsername(null);

        // Assert
        assertNull(user.getUsername());
    }

    @Test
    void testSetUsername_EmptyInput() {
        // Arrange
        User user = new User("old", "hash", "salt");

        // Act
        user.setUsername("");

        // Assert
        assertEquals("", user.getUsername());
    }

    @Test
    void testRegister_SelectQueryFails() throws SQLException {
        // Arrange
        String username = "testuser";
        String password = "testpass";
        SQLException selectError = new SQLException("Select failed");
        when(mockPreparedStatement.executeQuery()).thenThrow(selectError);

        // Act
        User user = User.register(username, password);

        // Assert
        assertNull(user);
    }

    @Test
    void testLogin_ResultSetClosed() throws SQLException {
        // Arrange
        String username = "user";
        String password = "pass";
        SQLException resultSetError = new SQLException("ResultSet closed");
        when(mockResultSet.next()).thenThrow(resultSetError);

        // Act
        User user = User.login(username, password);

        // Assert
        assertNull(user);
    }

    @Test
    void testHashPassword_DifferentSaltsProduceDifferentHashes() {
        // Arrange
        String password = "testpass";
        String salt1 = "salt1";
        String salt2 = "salt2";

        // Act
        String hash1 = User.hashPassword(password, salt1);
        String hash2 = User.hashPassword(password, salt2);

        // Assert
        assertNotEquals(hash1, hash2);
    }

    @Test
    void testHashPassword_SameInputsProduceSameHash() {
        // Arrange
        String password = "testpass";
        String salt = "testsalt";

        // Act
        String hash1 = User.hashPassword(password, salt);
        String hash2 = User.hashPassword(password, salt);

        // Assert
        assertEquals(hash1, hash2);
    }

    @Test
    void testRegister_NullUsername() throws SQLException {
        // Arrange
        String password = "testpass";
        when(mockResultSet.next()).thenReturn(false); // User does not exist

        // Act
        User user = User.register(null, password);

        // Assert
        // The current implementation doesn't check for null username, so it will create a user
        // This test documents the current behavior
        assertNotNull(user);
        assertNull(user.getUsername());
    }

    @Test
    void testRegister_NullPassword() throws SQLException {
        // Arrange
        String username = "testuser";
        when(mockResultSet.next()).thenReturn(false); // User does not exist

        // Act & Assert
        // The current implementation doesn't handle null password, so it will throw NPE
        assertThrows(NullPointerException.class, () -> {
            User.register(username, null);
        });
    }

    @Test
    void testRegister_EmptyUsername() throws SQLException {
        // Arrange
        String username = "";
        String password = "testpass";
        when(mockResultSet.next()).thenReturn(false); // User does not exist

        // Act
        User user = User.register(username, password);

        // Assert
        // The current implementation doesn't check for empty username, so it will create a user
        // This test documents the current behavior
        assertNotNull(user);
        assertEquals("", user.getUsername());
    }

    @Test
    void testRegister_EmptyPassword() throws SQLException {
        // Arrange
        String username = "testuser";
        String password = "";
        when(mockResultSet.next()).thenReturn(false); // User does not exist

        // Act
        User user = User.register(username, password);

        // Assert
        // The current implementation doesn't check for empty password, so it will create a user
        // This test documents the current behavior
        assertNotNull(user);
        assertEquals(username, user.getUsername());
    }

    @Test
    void testLogin_NullUsername() throws SQLException {
        // Arrange
        String password = "testpass";
        when(mockResultSet.next()).thenReturn(false); // User not found

        // Act
        User user = User.login(null, password);

        // Assert
        assertNull(user);
    }

    @Test
    void testLogin_NullPassword() throws SQLException {
        // Arrange
        String username = "testuser";
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password_hash")).thenReturn("hash");
        when(mockResultSet.getString("salt")).thenReturn("salt");

        // Act & Assert
        // The current implementation doesn't handle null password, so it will throw NPE
        assertThrows(NullPointerException.class, () -> {
            User.login(username, null);
        });
    }

    @Test
    void testLogin_DatabaseReturnsNullHash() throws SQLException {
        // Arrange
        String username = "user";
        String password = "pass";
        String salt = "dGVzdHNhbHQ="; // Base64 of "testsalt"

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password_hash")).thenReturn(null);
        when(mockResultSet.getString("salt")).thenReturn(salt);

        // Act & Assert
        // The current implementation doesn't handle null hash, so it will throw NPE
        assertThrows(NullPointerException.class, () -> {
            User.login(username, password);
        });
    }

    @Test
    void testLogin_DatabaseReturnsNullSalt() throws SQLException {
        // Arrange
        String username = "user";
        String password = "pass";
        String storedHash = User.hashPassword(password, "testsalt");

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password_hash")).thenReturn(storedHash);
        when(mockResultSet.getString("salt")).thenReturn(null);

        // Act & Assert
        // The current implementation doesn't handle null salt, so it will throw NPE
        assertThrows(NullPointerException.class, () -> {
            User.login(username, password);
        });
    }

    @Test
    void testLogin_DatabaseReturnsEmptyHash() throws SQLException {
        // Arrange
        String username = "user";
        String password = "pass";
        String salt = "dGVzdHNhbHQ="; // Base64 of "testsalt"

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password_hash")).thenReturn("");
        when(mockResultSet.getString("salt")).thenReturn(salt);

        // Act
        User user = User.login(username, password);

        // Assert
        assertNull(user);
    }

    @Test
    void testLogin_DatabaseReturnsEmptySalt() throws SQLException {
        // Arrange
        String username = "user";
        String password = "pass";
        String storedHash = User.hashPassword(password, "testsalt");

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password_hash")).thenReturn(storedHash);
        when(mockResultSet.getString("salt")).thenReturn("");

        // Act
        User user = User.login(username, password);

        // Assert
        assertNull(user);
    }

//    @Test
//    void testStaticInitializer_HandlesSQLExceptionGracefully() throws Exception {
//        // Close existing static mock from setUp and create a dedicated one for this scenario
//        if (mockedDriverManager != null) {
//            mockedDriverManager.close();
//            mockedDriverManager = null;
//        }
//
//        Connection failingConnection = mock(Connection.class);
//
//        try (MockedStatic<DriverManager> localMock = Mockito.mockStatic(DriverManager.class)) {
//            localMock.when(() -> DriverManager.getConnection("jdbc:sqlite:database.db"))
//                    .thenReturn(failingConnection);
//            when(failingConnection.createStatement()).thenThrow(new SQLException("createStatement failure"));
//
//            URL classesUrl = User.class.getProtectionDomain().getCodeSource().getLocation();
//            try (URLClassLoader loader = new URLClassLoader(new URL[]{classesUrl}, null)) {
//                Class<?> reloadedUser = Class.forName("domain.User", true, loader);
//                assertNotNull(reloadedUser);
//            }
//        }
//
//        // Re-establish the shared mock state expected by subsequent tests
//        mockedDriverManager = Mockito.mockStatic(DriverManager.class, invocation -> null);
//        mockedDriverManager.when(() -> DriverManager.getConnection("jdbc:sqlite:database.db"))
//                .thenReturn(mockConnection);
//    }
}