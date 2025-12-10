import dao.*;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            userDAO.initializeTable();
            verify(mockStmt).execute(anyString());
        }
    }

    @Test
    void testInitializeTable_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        when(mockConn.createStatement()).thenReturn(mockStmt);
        doThrow(new SQLException("Create failed")).when(mockStmt).execute(anyString());

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            assertDoesNotThrow(() -> userDAO.initializeTable());
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

            assertNull(userDAO.findByUsername("bob"));
        }
    }

    // ---------------- insert() ----------------
    @Test
    void testInsert_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockKeys = mock(ResultSet.class);

        // prepareStatement مع RETURN_GENERATED_KEYS
        when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockStmt);

        // executeUpdate يرجع 1
        when(mockStmt.executeUpdate()).thenReturn(1);

        // mock getGeneratedKeys
        when(mockStmt.getGeneratedKeys()).thenReturn(mockKeys);
        when(mockKeys.next()).thenReturn(true);   // يوجد مفتاح
        when(mockKeys.getInt(1)).thenReturn(1);    // المفتاح الناتج

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = userDAO.insert("alice", "hash", "salt");

            assertTrue(result);

            // تحقق من setObject
            verify(mockStmt).setObject(1, "alice");
            verify(mockStmt).setObject(2, "hash");
            verify(mockStmt).setObject(3, "salt");
            verify(mockStmt).executeUpdate();
            verify(mockStmt).getGeneratedKeys();
            verify(mockKeys).next();
        }
    }

    // ---------------- delete() ----------------
    @Test
    void testDelete_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement findStmt = mock(PreparedStatement.class);
        PreparedStatement deleteStmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(mockConn.prepareStatement("SELECT id FROM users WHERE username = ?")).thenReturn(findStmt);
        when(mockConn.prepareStatement("DELETE FROM borrow_records WHERE user_id = ?")).thenReturn(deleteStmt);
        when(mockConn.prepareStatement("DELETE FROM user_fines WHERE user_id = ?")).thenReturn(deleteStmt);
        when(mockConn.prepareStatement("DELETE FROM users WHERE id = ?")).thenReturn(deleteStmt);

        when(findStmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);

        when(deleteStmt.executeUpdate()).thenReturn(1);

        doNothing().when(mockConn).setAutoCommit(false);
        doNothing().when(mockConn).commit();
        doNothing().when(mockConn).setAutoCommit(true);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            assertTrue(userDAO.delete("alice"));
        }
    }




    // ---------------- delete() additional test cases ----------------
    @Test
    void testDelete_userNotFound() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement findStmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(mockConn.prepareStatement("SELECT id FROM users WHERE username = ?")).thenReturn(findStmt);
        when(findStmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false); // user not found

        doNothing().when(mockConn).rollback();
        doNothing().when(mockConn).setAutoCommit(true);
        doNothing().when(mockConn).setAutoCommit(false);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            assertFalse(userDAO.delete("nonexistentUser"));
            verify(mockConn).rollback();
        }
    }


    @Test
    void testDelete_nullConnection() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);

            assertFalse(userDAO.delete("alice"));
        }
    }

    // ---------------- insert() additional test cases ----------------
    @Test
    void testInsert_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString(), anyInt())).thenReturn(mockStmt);
        doThrow(new SQLException("Insert failed")).when(mockStmt).executeUpdate();

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            assertFalse(userDAO.insert("alice", "hash", "salt"));
        }
    }

    @Test
    void testInsert_nullConnection() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);

            assertFalse(userDAO.insert("alice", "hash", "salt"));
        }
    }

    // ---------------- findByUsername() additional test cases ----------------
    @Test
    void testFindByUsername_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        doThrow(new SQLException("Query failed")).when(mockStmt).executeQuery();

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            assertNull(userDAO.findByUsername("alice"));
        }
    }

    @Test
    void testFindByUsername_nullConnection() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);

            assertNull(userDAO.findByUsername("alice"));
        }
    }

}
