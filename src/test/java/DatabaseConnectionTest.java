
import dao.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DatabaseConnectionTest {

    private MockedStatic<java.sql.DriverManager> mockedDriverManager;

    @BeforeEach
    void setUp() {
        mockedDriverManager = mockStatic(java.sql.DriverManager.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedDriverManager != null) {
            mockedDriverManager.close();
        }
    }

    @Test
    void testGetConnectionSuccess() throws SQLException {
        // Mock a successful connection
        Connection mockConnection = mock(Connection.class);
        mockedDriverManager.when(() -> java.sql.DriverManager.getConnection(anyString()))
                .thenReturn(mockConnection);

        // Call the method
        Connection result = DatabaseConnection.getConnection();

        // Verify the result
        assertNotNull(result);
        assertEquals(mockConnection, result);

        // Verify that DriverManager.getConnection was called with the correct URL
        mockedDriverManager.verify(() -> java.sql.DriverManager.getConnection("jdbc:sqlite:database.db"));
    }

    @Test
    void testGetConnectionFailure() throws SQLException {
        // Mock a failed connection
        SQLException mockException = new SQLException("Connection failed");
        mockedDriverManager.when(() -> java.sql.DriverManager.getConnection(anyString()))
                .thenThrow(mockException);

        // Call the method
        Connection result = DatabaseConnection.getConnection();

        // Verify the result
        assertNull(result);

        // Verify that DriverManager.getConnection was called
        mockedDriverManager.verify(() -> java.sql.DriverManager.getConnection("jdbc:sqlite:database.db"));
    }

    @Test
    void testPrivateConstructor() {
        // Test that the private constructor throws an exception
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            try {
                // Use reflection to access the private constructor
                java.lang.reflect.Constructor<DatabaseConnection> constructor =
                        DatabaseConnection.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            } catch (Exception e) {
                if (e.getCause() instanceof IllegalStateException) {
                    throw (IllegalStateException) e.getCause();
                }
                fail("Expected IllegalStateException but got: " + e.getClass().getName());
            }
        });

        // Verify the exception message
        assertEquals("Utility class", exception.getMessage());
    }
}