import dao.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import dao.DatabaseConnection;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MediaDAOTest {

    private MediaDAO mediaDAO;

    @BeforeEach
    void setUp() {
        mediaDAO = new MediaDAO();
    }

    // ------------------ initializeTable() ------------------
    @Test
    void testInitializeTable_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        when(mockConn.createStatement()).thenReturn(mockStmt);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            mediaDAO.initializeTable();
            verify(mockStmt).execute(anyString());
            assertTrue(outContent.toString().contains("Media table created successfully."));
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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            mediaDAO.initializeTable();
            assertTrue(errContent.toString().contains("Error creating media table: Create failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testInitializeTable_nullConnection() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);
            assertDoesNotThrow(() -> mediaDAO.initializeTable());
        }
    }

    // ------------------ insert() ------------------
    @Test
    void testInsert_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
        when(mockStmt.getGeneratedKeys()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt(1)).thenReturn(42);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            int id = mediaDAO.insert("Some Media", "book");
            assertEquals(42, id);
            verify(mockStmt).executeUpdate();
        }
    }

    @Test
    void testInsert_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
        doThrow(new SQLException("Insert failed")).when(mockStmt).executeUpdate();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            int id = mediaDAO.insert("Some Media", "book");
            assertEquals(-1, id);
            assertTrue(errContent.toString().contains("Error inserting media: Insert failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testInsert_nullConnection() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);
            int id = mediaDAO.insert("Some Media", "book");
            assertEquals(-1, id);
        }
    }

    // ------------------ updateAvailability() ------------------
    @Test
    void testUpdateAvailability_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1); // 1 row updated

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = mediaDAO.updateAvailability(1, false);
            assertTrue(result);
        }
    }

    @Test
    void testUpdateAvailability_noRows() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(0); // no rows updated

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = mediaDAO.updateAvailability(1, true);
            assertFalse(result);
        }
    }

    @Test
    void testUpdateAvailability_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        doThrow(new SQLException("Update failed")).when(mockStmt).executeUpdate();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = mediaDAO.updateAvailability(1, true);
            assertFalse(result);
            assertTrue(errContent.toString().contains("Error updating media availability: Update failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testUpdateAvailability_nullConnection() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);
            assertFalse(mediaDAO.updateAvailability(1, true));
        }
    }
}

