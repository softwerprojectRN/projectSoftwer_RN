import domain.Media;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class MediaTest {

    @Test
    public void testConnect() throws SQLException {
        // Mock connection
        Connection mockConnection = mock(Connection.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);

            // Call the method
            Connection result = Media.connect();

            // Verify
            assertNotNull(result);
            assertEquals(mockConnection, result);
            mockedDriverManager.verify(() -> DriverManager.getConnection("jdbc:sqlite:database.db"), times(1));
        }
    }

    @Test
    public void testConnectWithException() {
        // Mock exception
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            // Create the exception outside the when() call to avoid triggering code during stubbing
            SQLException exception = new SQLException("Connection failed");
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenThrow(exception);

            // Call the method
            Connection result = Media.connect();

            // Verify
            assertNull(result);
        }
    }

    @Test
    public void testUpdateAvailabilitySuccess() throws SQLException {
        // Create a concrete implementation of Media for testing
        Media media = new Media(1, "Test Book", true, "book") {};

        // Mock connection and prepared statement
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            // Call the method
            media.updateAvailability(false);

            // Verify
            assertFalse(media.isAvailable());
            verify(mockPreparedStatement).setInt(1, 0);
            verify(mockPreparedStatement).setInt(2, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    public void testUpdateAvailabilityFailure() throws SQLException {
        // Create a concrete implementation of Media for testing
        Media media = new Media(1, "Test Book", true, "book") {};

        // Mock connection and prepared statement
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(0);

            // Call the method
            media.updateAvailability(false);

            // Verify
            assertTrue(media.isAvailable()); // Should remain unchanged
            verify(mockPreparedStatement).setInt(1, 0);
            verify(mockPreparedStatement).setInt(2, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    public void testUpdateAvailabilityWithZeroId() {
        // Create a concrete implementation of Media for testing
        Media media = new Media(0, "Test Book", true, "book") {};

        // Call the method
        media.updateAvailability(false);

        // Verify
        assertTrue(media.isAvailable()); // Should remain unchanged
    }

    @Test
    public void testUpdateAvailabilityWithException() {
        // Create a concrete implementation of Media for testing
        Media media = new Media(1, "Test Book", true, "book") {};

        // Mock connection and prepared statement
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);
            try {
                when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Create the exception outside the when() call
            SQLException exception = new SQLException("Update failed");
            try {
                when(mockPreparedStatement.executeUpdate()).thenThrow(exception);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Call the method
            media.updateAvailability(false);

            // Verify
            assertTrue(media.isAvailable()); // Should remain unchanged
        }
    }

    @Test
    public void testBorrowWhenAvailable() throws SQLException {
        // Create a concrete implementation of Media for testing
        Media media = new Media(1, "Test Book", true, "book") {};

        // Mock connection and prepared statement
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            // Call the method
            media.borrow();

            // Verify
            assertFalse(media.isAvailable());
        }
    }

    @Test
    public void testBorrowWhenNotAvailable() {
        // Create a concrete implementation of Media for testing
        Media media = new Media(1, "Test Book", false, "book") {};

        // Call the method
        media.borrow();

        // Verify
        assertFalse(media.isAvailable()); // Should remain unchanged
    }

    @Test
    public void testReturnMediaWhenNotAvailable() throws SQLException {
        // Create a concrete implementation of Media for testing
        Media media = new Media(1, "Test Book", false, "book") {};

        // Mock connection and prepared statement
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            // Call the method
            media.returnMedia();

            // Verify
            assertTrue(media.isAvailable());
        }
    }

    @Test
    public void testReturnMediaWhenAvailable() {
        // Create a concrete implementation of Media for testing
        Media media = new Media(1, "Test Book", true, "book") {};

        // Call the method
        media.returnMedia();

        // Verify
        assertTrue(media.isAvailable()); // Should remain unchanged
    }

    @Test
    public void testToStringTrue() {
        // Create a concrete implementation of Media for testing
        Media media = new Media(1, "Test Book", true, "book") {};

        // Call the method
        String result = media.toString();

        // Verify
        assertEquals("ID: 1, Title: 'Test Book', Type: book, Available: Yes", result);
    }

    @Test
    public void testToStringFalse() {
        // Create a concrete implementation of Media for testing
        Media media = new Media(1, "Test Book", false, "book") {};

        // Call the method
        String result = media.toString();

        // Verify
        assertEquals("ID: 1, Title: 'Test Book', Type: book, Available: No", result);
    }
}