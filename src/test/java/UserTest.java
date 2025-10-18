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
        mockedDriverManager = Mockito.mockStatic(DriverManager.class);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockStatement = mock(Statement.class);

        mockedDriverManager.when(() -> DriverManager.getLogWriter()).thenReturn(null);
        mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);
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
}