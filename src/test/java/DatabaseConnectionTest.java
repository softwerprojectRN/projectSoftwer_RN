
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseConnectionTest {

    @Test
    void testGetConnection_Success() {
        Connection mockConnection = mock(Connection.class);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                    .thenReturn(mockConnection);

            Connection result = DatabaseConnection.getConnection();

            assertNotNull(result);
            assertEquals(mockConnection, result);
            mockedDriverManager.verify(() -> DriverManager.getConnection("jdbc:sqlite:database.db"), times(1));
        }
    }

    @Test
    void testGetConnection_Failure() {
        SQLException sqlException = new SQLException("Database connection failed");

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString()))
                    .thenThrow(sqlException);

            Connection result = DatabaseConnection.getConnection();

            assertNull(result, "Connection should be null when an SQLException is thrown");
            mockedDriverManager.verify(() -> DriverManager.getConnection("jdbc:sqlite:database.db"), times(1));
        }
    }
}