import dao.*;

import model.Admin;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDADTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    private AdminDAO adminDAO;
    private MockedStatic<DatabaseConnection> mockedDatabaseConnection;

    @BeforeEach
    void setUp() {
        mockedDatabaseConnection = mockStatic(DatabaseConnection.class);
        mockedDatabaseConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
        adminDAO = new AdminDAO();
    }

    @AfterEach
    void tearDown() {
        if (mockedDatabaseConnection != null) {
            mockedDatabaseConnection.close();
        }
    }

    @Test
    void testInitializeTable() throws SQLException {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        adminDAO.initializeTable();
        verify(mockStatement).execute(contains("CREATE TABLE IF NOT EXISTS admins"));
    }

    @Test
    void testInitializeTableWithSQLException() throws SQLException {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        doThrow(new SQLException("Table creation failed")).when(mockStatement).execute(anyString());
        adminDAO.initializeTable();
        verify(mockStatement).execute(contains("CREATE TABLE IF NOT EXISTS admins"));
    }

    @Test
    void testFindByUsernameSuccess() throws SQLException {
        String username = "admin";
        int expectedId = 1;
        String expectedPasswordHash = "hashedpassword";
        String expectedSalt = "salt123";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(expectedId);
        when(mockResultSet.getString("username")).thenReturn(username);
        when(mockResultSet.getString("password_hash")).thenReturn(expectedPasswordHash);
        when(mockResultSet.getString("salt")).thenReturn(expectedSalt);

        Admin result = adminDAO.findByUsername(username);

        assertNotNull(result);
        assertEquals(expectedId, result.getId());
        assertEquals(username, result.getUsername());
        assertEquals(expectedPasswordHash, result.getPasswordHash());
        assertEquals(expectedSalt, result.getSalt());

        verify(mockPreparedStatement).setObject(1, username);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testFindByUsernameNotFound() throws SQLException {
        String username = "nonexistent";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Admin result = adminDAO.findByUsername(username);

        assertNull(result);
        verify(mockPreparedStatement).setObject(1, username);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testFindByUsernameWithSQLException() throws SQLException {
        String username = "admin";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Query failed"));

        Admin result = adminDAO.findByUsername(username);

        assertNull(result);
        verify(mockPreparedStatement).setObject(1, username);
    }

    @Test
    void testInsertSuccess() throws SQLException {
        String username = "newadmin";
        String passwordHash = "hashedpassword";
        String salt = "salt123";
        int expectedId = 2;

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet); // Correctly stubbed
        when(mockResultSet.next()).thenReturn(true); // Correctly stubbed
        when(mockResultSet.getInt(1)).thenReturn(expectedId);

        boolean result = adminDAO.insert(username, passwordHash, salt);

        assertTrue(result);
        verify(mockPreparedStatement).setObject(1, username);
        verify(mockPreparedStatement).setObject(2, passwordHash);
        verify(mockPreparedStatement).setObject(3, salt);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testInsertFailure() throws SQLException {
        // Setup
        String username = "newadmin";
        String passwordHash = "hashedpassword";
        String salt = "salt123";

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // --- FIX IS HERE ---
        // We must stub getGeneratedKeys() to prevent NullPointerException.
        // We simulate that no keys were generated.
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Execute
        boolean result = adminDAO.insert(username, passwordHash, salt);

        // Verify
        assertFalse(result);
        verify(mockPreparedStatement).setObject(1, username);
        verify(mockPreparedStatement).setObject(2, passwordHash);
        verify(mockPreparedStatement).setObject(3, salt);
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).getGeneratedKeys(); // Good practice to verify it was called
        verify(mockResultSet).next(); // And this was called
    }

    @Test
    void testInsertWithSQLException() throws SQLException {
        String username = "newadmin";
        String passwordHash = "hashedpassword";
        String salt = "salt123";

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        // This test throws an exception before getGeneratedKeys() is called, so no stubbing is needed.
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Insert failed"));

        boolean result = adminDAO.insert(username, passwordHash, salt);

        assertFalse(result);
        verify(mockPreparedStatement).setObject(1, username);
        verify(mockPreparedStatement).setObject(2, passwordHash);
        verify(mockPreparedStatement).setObject(3, salt);
    }
}