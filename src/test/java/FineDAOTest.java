
import dao.DatabaseConnection;
import dao.FineDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FineDAOTest {

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

    // Use a spy to test internal method calls
    @Spy
    private FineDAO fineDAO;

    private MockedStatic<DatabaseConnection> mockedDatabaseConnection;

    @BeforeEach
    void setUp() {
        mockedDatabaseConnection = mockStatic(DatabaseConnection.class);
        mockedDatabaseConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
        fineDAO = new FineDAO();
        fineDAO = spy(fineDAO);
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
        fineDAO.initializeTable();
        verify(mockStatement).execute(contains("CREATE TABLE IF NOT EXISTS user_fines"));
    }

    @Test
    void testGetFineBalance_Exists() throws SQLException {
        double expectedBalance = 15.50;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDouble("total_fine")).thenReturn(expectedBalance);

        double result = fineDAO.getFineBalance(1);

        assertEquals(expectedBalance, result);
        verify(mockPreparedStatement).setObject(1, 1);
        verify(fineDAO, never()).initializeFine(anyInt());
    }

    @Test
    void testGetFineBalance_NotExists() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        doReturn(true).when(fineDAO).initializeFine(1);

        double result = fineDAO.getFineBalance(1);

        assertEquals(0.0, result);
        verify(mockPreparedStatement).setObject(1, 1);
        verify(fineDAO).initializeFine(1);
    }

    @Test
    void testInitializeFine_Success() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(1);

        boolean result = fineDAO.initializeFine(1);

        assertTrue(result);
        // --- FIX IS HERE ---
        // Only verify the parameter that is actually set. The second parameter (0.0) is hardcoded in the SQL.
        verify(mockPreparedStatement).setObject(1, 1);
        // Removed: verify(mockPreparedStatement).setObject(2, 0.0);
    }

    @Test
    void testInitializeFine_Failure() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(false);

        boolean result = fineDAO.initializeFine(1);

        assertFalse(result);
        // Only verify the parameter that is actually set.
        verify(mockPreparedStatement).setObject(1, 1);
    }

    @Test
    void testUpdateFine_Success() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = fineDAO.updateFine(1, 25.0);

        assertTrue(result);
        verify(mockPreparedStatement).setObject(1, 25.0);
        verify(mockPreparedStatement).setObject(2, 1);
        verify(fineDAO, never()).initializeFine(anyInt());
    }

    @Test
    void testUpdateFine_FailsButInitializeSucceeds() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        doReturn(true).when(fineDAO).initializeFine(1);

        boolean result = fineDAO.updateFine(1, 25.0);

        assertTrue(result);
        verify(fineDAO).initializeFine(1);
    }

    @Test
    void testUpdateFine_FailsAndInitializeFails() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        doReturn(false).when(fineDAO).initializeFine(1);

        boolean result = fineDAO.updateFine(1, 25.0);

        assertFalse(result);
        verify(fineDAO).initializeFine(1);
    }

    @Test
    void testAddFine() {
        double currentBalance = 10.0;
        double fineToAdd = 5.0;
        double newBalance = currentBalance + fineToAdd;
        doReturn(currentBalance).when(fineDAO).getFineBalance(1);
        doReturn(true).when(fineDAO).updateFine(1, newBalance);

        boolean result = fineDAO.addFine(1, fineToAdd);

        assertTrue(result);
        verify(fineDAO).getFineBalance(1);
        verify(fineDAO).updateFine(1, newBalance);
    }

    @Test
    void testPayFine_Success() {
        double currentBalance = 20.0;
        double paymentAmount = 15.0;
        double newBalance = currentBalance - paymentAmount;
        doReturn(currentBalance).when(fineDAO).getFineBalance(1);
        doReturn(true).when(fineDAO).updateFine(1, newBalance);

        boolean result = fineDAO.payFine(1, paymentAmount);

        assertTrue(result);
        verify(fineDAO).getFineBalance(1);
        verify(fineDAO).updateFine(1, newBalance);
    }

    @Test
    void testPayFine_InvalidAmountZero() {
        double currentBalance = 20.0;
        doReturn(currentBalance).when(fineDAO).getFineBalance(1);

        boolean result = fineDAO.payFine(1, 0.0);

        assertFalse(result);
        verify(fineDAO).getFineBalance(1);
        verify(fineDAO, never()).updateFine(anyInt(), anyDouble());
    }

    @Test
    void testPayFine_InvalidAmountTooHigh() {
        double currentBalance = 20.0;
        doReturn(currentBalance).when(fineDAO).getFineBalance(1);

        boolean result = fineDAO.payFine(1, 25.0);

        assertFalse(result);
        verify(fineDAO).getFineBalance(1);
        verify(fineDAO, never()).updateFine(anyInt(), anyDouble());
    }

    @Test
    void testClearFine() {
        doReturn(true).when(fineDAO).updateFine(1, 0.0);

        boolean result = fineDAO.clearFine(1);

        assertTrue(result);
        verify(fineDAO).updateFine(1, 0.0);
    }
}