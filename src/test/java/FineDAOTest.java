import dao.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FineDAOTest {

    private FineDAO fineDAO;

    @BeforeEach
    void setUp() {
        fineDAO = new FineDAO();
    }

    // ------------------ initializeTable() ------------------
    @Test
    void testInitializeTable_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        when(mockConn.createStatement()).thenReturn(mockStmt);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            fineDAO.initializeTable();
            verify(mockStmt).execute(anyString());
            assertTrue(outContent.toString().contains("User fines table created successfully."));
        } finally {
            System.setOut(System.out);
        }
    }

    @Test
    void testInitializeTable_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        when(mockConn.createStatement()).thenReturn(mockStmt);
        doThrow(new SQLException("Create failed")).when(mockStmt).execute(anyString());

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            fineDAO.initializeTable();
            assertTrue(errContent.toString().contains("Error creating user_fines table: Create failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testInitializeTable_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            assertDoesNotThrow(() -> fineDAO.initializeTable());
        }
    }

    // ------------------ initializeFine() ------------------
    @Test
    void testInitializeFine_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            assertTrue(fineDAO.initializeFine(1));
            verify(mockStmt).executeUpdate();
        }
    }

    @Test
    void testInitializeFine_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        doThrow(new SQLException("Insert failed")).when(mockStmt).executeUpdate();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            assertFalse(fineDAO.initializeFine(1));
            assertTrue(errContent.toString().contains("Error initializing fine: Insert failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    // ------------------ getFineBalance() ------------------
    @Test
    void testGetFineBalance_existingRecord() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getDouble("total_fine")).thenReturn(10.0);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            double fine = fineDAO.getFineBalance(1);
            assertEquals(10.0, fine);
        }
    }

    @Test
    void testGetFineBalance_noRecord_callsInitialize() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false); // No record

        FineDAO spyDAO = spy(fineDAO);
        doReturn(true).when(spyDAO).initializeFine(1);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            double fine = spyDAO.getFineBalance(1);
            assertEquals(0.0, fine);
            verify(spyDAO).initializeFine(1);
        }
    }

    // ------------------ updateFine() ------------------
    @Test
    void testUpdateFine_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1); // updated one row

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            assertTrue(fineDAO.updateFine(1, 20.0));
        }
    }

    @Test
    void testUpdateFine_noRows_callsInitialize() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(0); // no rows affected

        FineDAO spyDAO = spy(fineDAO);
        doReturn(true).when(spyDAO).initializeFine(1);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            assertTrue(spyDAO.updateFine(1, 20.0));
            verify(spyDAO).initializeFine(1);
        }
    }

    // ------------------ addFine() ------------------
    @Test
    void testAddFine_success() throws SQLException {
        FineDAO spyDAO = spy(fineDAO);
        doReturn(10.0).when(spyDAO).getFineBalance(1);
        doReturn(true).when(spyDAO).updateFine(1, 15.0);

        assertTrue(spyDAO.addFine(1, 5.0));
        verify(spyDAO).updateFine(1, 15.0);
    }

    // ------------------ payFine() ------------------
    @Test
    void testPayFine_success() {
        FineDAO spyDAO = spy(fineDAO);
        doReturn(10.0).when(spyDAO).getFineBalance(1);
        doReturn(true).when(spyDAO).updateFine(1, 7.0);

        assertTrue(spyDAO.payFine(1, 3.0));
        verify(spyDAO).updateFine(1, 7.0);
    }

    @Test
    void testPayFine_invalidAmount() {
        FineDAO spyDAO = spy(fineDAO);
        doReturn(10.0).when(spyDAO).getFineBalance(1);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        assertFalse(spyDAO.payFine(1, -5.0));
        assertTrue(errContent.toString().contains("Invalid payment amount."));

        assertFalse(spyDAO.payFine(1, 15.0));
        assertTrue(errContent.toString().contains("Invalid payment amount."));
    }

    // ------------------ clearFine() ------------------
    @Test
    void testClearFine_callsUpdate() {
        FineDAO spyDAO = spy(fineDAO);
        doReturn(true).when(spyDAO).updateFine(1, 0.0);

        assertTrue(spyDAO.clearFine(1));
        verify(spyDAO).updateFine(1, 0.0);
    }
    // ------------------ getFineBalance null connection ------------------
    @Test
    void testGetFineBalance_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            assertEquals(0.0, fineDAO.getFineBalance(1));
        }
    }

    // ------------------ initializeFine null connection ------------------
    @Test
    void testInitializeFine_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            assertFalse(fineDAO.initializeFine(1));
        }
    }

    // ------------------ updateFine SQLException ------------------
    @Test
    void testUpdateFine_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        doThrow(new SQLException("Update failed")).when(mockStmt).executeUpdate();

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            assertFalse(fineDAO.updateFine(1, 20.0));
        }
    }

    // ------------------ addFine fails when updateFine returns false ------------------
    @Test
    void testAddFine_updateFails() {
        FineDAO spyDAO = spy(fineDAO);
        doReturn(10.0).when(spyDAO).getFineBalance(1);
        doReturn(false).when(spyDAO).updateFine(1, 15.0);

        assertFalse(spyDAO.addFine(1, 5.0));
    }

    // ------------------ payFine fails when updateFine returns false ------------------
    @Test
    void testPayFine_updateFails() {
        FineDAO spyDAO = spy(fineDAO);
        doReturn(10.0).when(spyDAO).getFineBalance(1);
        doReturn(false).when(spyDAO).updateFine(1, 7.0);

        assertFalse(spyDAO.payFine(1, 3.0));
    }

}
