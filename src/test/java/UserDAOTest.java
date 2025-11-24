import dao.*;

import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import util.DatabaseConnection;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDAOTest {

    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
    }

    // ---------------- initializeTable() ----------------
    @Test
    void testInitializeTable_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        when(mockConn.createStatement()).thenReturn(mockStmt);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            userDAO.initializeTable();
            verify(mockStmt).execute(anyString());
            assertTrue(outContent.toString().contains("Users table created successfully."));
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

            userDAO.initializeTable();
            assertTrue(errContent.toString().contains("Error creating users table: Create failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testInitializeTable_nullConnection() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);
            assertDoesNotThrow(() -> userDAO.initializeTable());
        }
    }

    // ---------------- findByUsername() ----------------
    @Test
    void testFindByUsername_found() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("id")).thenReturn(1);
        when(mockRs.getString("username")).thenReturn("alice");
        when(mockRs.getString("password_hash")).thenReturn("hash");
        when(mockRs.getString("salt")).thenReturn("salt");

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            User user = userDAO.findByUsername("alice");
            assertNotNull(user);
            assertEquals("alice", user.getUsername());
        }
    }

    @Test
    void testFindByUsername_notFound() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            User user = userDAO.findByUsername("bob");
            assertNull(user);
        }
    }

    @Test
    void testFindByUsername_nullConnection() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);
            assertNull(userDAO.findByUsername("bob"));
        }
    }

    // ---------------- insert() ----------------
    @Test
    void testInsert_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            assertTrue(userDAO.insert("alice", "hash", "salt"));
            verify(mockStmt).executeUpdate();
        }
    }

    @Test
    void testInsert_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        doThrow(new SQLException("Insert failed")).when(mockStmt).executeUpdate();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = userDAO.insert("alice", "hash", "salt");
            assertFalse(result);
            assertTrue(errContent.toString().contains("Error inserting user: Insert failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testInsert_nullConnection() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);
            assertFalse(userDAO.insert("alice", "hash", "salt"));
        }
    }

    // ---------------- delete() ----------------
    @Test
    void testDelete_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement findStmt = mock(PreparedStatement.class);
        PreparedStatement deleteBorrowStmt = mock(PreparedStatement.class);
        PreparedStatement deleteFinesStmt = mock(PreparedStatement.class);
        PreparedStatement deleteUserStmt = mock(PreparedStatement.class);
        ResultSet findRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString()))
                .thenReturn(findStmt)
                .thenReturn(deleteBorrowStmt)
                .thenReturn(deleteFinesStmt)
                .thenReturn(deleteUserStmt);

        when(findStmt.executeQuery()).thenReturn(findRs);
        when(findRs.next()).thenReturn(true);
        when(findRs.getInt("id")).thenReturn(1);

        when(deleteUserStmt.executeUpdate()).thenReturn(1);

        doNothing().when(mockConn).setAutoCommit(false);
        doNothing().when(mockConn).commit();
        doNothing().when(mockConn).setAutoCommit(true);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = userDAO.delete("alice");
            assertTrue(result);
        }
    }
    // ---------------- findByUsername() ----------------
    @Test
    void testFindByUsername_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        doThrow(new SQLException("Query failed")).when(mockStmt).executeQuery();

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            User user = userDAO.findByUsername("alice");
            assertNull(user);
        }
    }

    // ---------------- insert() ----------------
    @Test
    void testInsert_duplicateUsername() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        doThrow(new SQLException("UNIQUE constraint failed")).when(mockStmt).executeUpdate();

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = userDAO.insert("alice", "hash", "salt");
            assertFalse(result);
        }
    }

    // ---------------- delete() ----------------
    @Test
    void testDelete_userNotFound() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement findStmt = mock(PreparedStatement.class);
        ResultSet findRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(findStmt);
        when(findStmt.executeQuery()).thenReturn(findRs);
        when(findRs.next()).thenReturn(false); // user not found

        doNothing().when(mockConn).rollback();
        doNothing().when(mockConn).setAutoCommit(false);
        doNothing().when(mockConn).setAutoCommit(true);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = userDAO.delete("unknown");
            assertFalse(result);
        }
    }

    @Test
    void testDelete_sqlExceptionDuringDelete() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement findStmt = mock(PreparedStatement.class);
        PreparedStatement deleteBorrowStmt = mock(PreparedStatement.class);
        PreparedStatement deleteFinesStmt = mock(PreparedStatement.class);
        PreparedStatement deleteUserStmt = mock(PreparedStatement.class);
        ResultSet findRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString()))
                .thenReturn(findStmt)
                .thenReturn(deleteBorrowStmt)
                .thenReturn(deleteFinesStmt)
                .thenReturn(deleteUserStmt);

        when(findStmt.executeQuery()).thenReturn(findRs);
        when(findRs.next()).thenReturn(true);
        when(findRs.getInt("id")).thenReturn(1);

        // SQLException during delete user
        doThrow(new SQLException("Delete failed")).when(deleteUserStmt).executeUpdate();

        doNothing().when(mockConn).setAutoCommit(false);
        doNothing().when(mockConn).rollback();
        doNothing().when(mockConn).setAutoCommit(true);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = userDAO.delete("alice");
            assertFalse(result);
        }
    }

    @Test
    void testDelete_sqlExceptionDuringSetAutoCommit() throws SQLException {
        Connection mockConn = mock(Connection.class);

        doThrow(new SQLException("AutoCommit failed")).when(mockConn).setAutoCommit(false);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = userDAO.delete("alice");
            assertFalse(result);
        }
    }

    @Test
    void testDelete_sqlExceptionDuringCommit() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement findStmt = mock(PreparedStatement.class);
        PreparedStatement deleteBorrowStmt = mock(PreparedStatement.class);
        PreparedStatement deleteFinesStmt = mock(PreparedStatement.class);
        PreparedStatement deleteUserStmt = mock(PreparedStatement.class);
        ResultSet findRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString()))
                .thenReturn(findStmt)
                .thenReturn(deleteBorrowStmt)
                .thenReturn(deleteFinesStmt)
                .thenReturn(deleteUserStmt);

        when(findStmt.executeQuery()).thenReturn(findRs);
        when(findRs.next()).thenReturn(true);
        when(findRs.getInt("id")).thenReturn(1);

        doNothing().when(mockConn).setAutoCommit(false);
        doThrow(new SQLException("Commit failed")).when(mockConn).commit();
        doNothing().when(mockConn).rollback();
        doNothing().when(mockConn).setAutoCommit(true);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = userDAO.delete("alice");
            assertFalse(result);
        }
    }
}

