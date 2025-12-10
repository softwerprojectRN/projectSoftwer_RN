
import dao.DatabaseConnection;
import dao.MediaDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSet mockGeneratedKeys;

    private MediaDAO mediaDAO;
    private MockedStatic<DatabaseConnection> mockedDatabaseConnection;

    @BeforeEach
    void setUp() {
        // Mock the static DatabaseConnection.getConnection() method
        mockedDatabaseConnection = mockStatic(DatabaseConnection.class);
        mockedDatabaseConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
        mediaDAO = new MediaDAO();
    }

    @AfterEach
    void tearDown() {
        if (mockedDatabaseConnection != null) {
            mockedDatabaseConnection.close();
        }
    }

    @Test
    void testInitializeTable() throws SQLException {
        // Setup
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        // Execute
        mediaDAO.initializeTable();

        // Verify
        verify(mockStatement).execute(contains("CREATE TABLE IF NOT EXISTS media"));
    }

    @Test
    void testInitializeTableWithSQLException() throws SQLException {
        // Setup
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        doThrow(new SQLException("Table creation failed")).when(mockStatement).execute(anyString());

        // Execute - should not throw an exception
        mediaDAO.initializeTable();

        // Verify
        verify(mockStatement).execute(contains("CREATE TABLE IF NOT EXISTS media"));
    }

    @Test
    void testInsert() throws SQLException {
        // Setup
        String title = "Test Media";
        String mediaType = "book";
        int expectedId = 123;

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(expectedId);

        // Execute
        int result = mediaDAO.insert(title, mediaType);

        // Verify
        assertEquals(expectedId, result);
        verify(mockPreparedStatement).setObject(1, title);
        verify(mockPreparedStatement).setObject(2, mediaType);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testInsertFailure() throws SQLException {
        // Setup
        String title = "Test Media";
        String mediaType = "book";

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        // We must stub getGeneratedKeys() to prevent NullPointerException in BaseDAO
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(false);

        // Execute
        int result = mediaDAO.insert(title, mediaType);

        // Verify
        assertEquals(-1, result);
        verify(mockPreparedStatement).setObject(1, title);
        verify(mockPreparedStatement).setObject(2, mediaType);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testUpdateAvailabilitySuccessToTrue() throws SQLException {
        // Setup
        int mediaId = 1;
        boolean available = true;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Execute
        boolean result = mediaDAO.updateAvailability(mediaId, available);

        // Verify
        assertTrue(result);
        verify(mockPreparedStatement).setObject(1, 1); // true is converted to 1
        verify(mockPreparedStatement).setObject(2, mediaId);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testUpdateAvailabilitySuccessToFalse() throws SQLException {
        // Setup
        int mediaId = 1;
        boolean available = false;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Execute
        boolean result = mediaDAO.updateAvailability(mediaId, available);

        // Verify
        assertTrue(result);
        verify(mockPreparedStatement).setObject(1, 0); // false is converted to 0
        verify(mockPreparedStatement).setObject(2, mediaId);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testUpdateAvailabilityFailure() throws SQLException {
        // Setup
        int mediaId = 999;
        boolean available = true;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0); // No rows affected

        // Execute
        boolean result = mediaDAO.updateAvailability(mediaId, available);

        // Verify
        assertFalse(result);
        verify(mockPreparedStatement).setObject(1, 1);
        verify(mockPreparedStatement).setObject(2, mediaId);
        verify(mockPreparedStatement).executeUpdate();
    }
}