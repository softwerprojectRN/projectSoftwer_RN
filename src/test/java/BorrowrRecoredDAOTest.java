import dao.*;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BorrowRecordDAOTest {

    private BorrowRecordDAO borrowRecordDAO;

    @BeforeEach
    void setUp() {
        borrowRecordDAO = new BorrowRecordDAO();
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

            borrowRecordDAO.initializeTable();

            verify(mockStmt, times(1)).execute(anyString());
            assertTrue(outContent.toString().contains("Borrow records table created successfully."));
        } finally {
            System.setOut(System.out);
        }
    }

    @Test
    void testInitializeTable_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        when(mockConn.createStatement()).thenReturn(mockStmt);
        doThrow(new SQLException("Create table failed")).when(mockStmt).execute(anyString());

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            borrowRecordDAO.initializeTable();
            assertTrue(errContent.toString().contains("Error creating borrow_records table: Create table failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testInitializeTable_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            assertDoesNotThrow(() -> borrowRecordDAO.initializeTable());
        }
    }

    // ------------------ insert() ------------------
    @Test
    void testInsert_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);
        when(mockStmt.getGeneratedKeys()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt(1)).thenReturn(42);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            int id = borrowRecordDAO.insert(1, 2, "book", "Title", LocalDate.now(), LocalDate.now().plusDays(7));
            assertEquals(42, id);
        }
    }

    @Test
    void testInsert_failure() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
        doThrow(new SQLException("Insert failed")).when(mockStmt).executeUpdate();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            int id = borrowRecordDAO.insert(1, 2, "book", "Title", LocalDate.now(), LocalDate.now().plusDays(7));
            assertEquals(-1, id);
            assertTrue(errContent.toString().contains("Error inserting borrow record: Insert failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testInsert_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            int id = borrowRecordDAO.insert(1, 2, "book", "Title", LocalDate.now(), LocalDate.now().plusDays(7));
            assertEquals(-1, id);
        }
    }

    // ------------------ markAsReturned() ------------------
    @Test
    void testMarkAsReturned_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = borrowRecordDAO.markAsReturned(1, LocalDate.now(), 5.0);
            assertTrue(result);
        }
    }

    @Test
    void testMarkAsReturned_failure() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        doThrow(new SQLException("Update failed")).when(mockStmt).executeUpdate();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = borrowRecordDAO.markAsReturned(1, LocalDate.now(), 5.0);
            assertFalse(result);
            assertTrue(errContent.toString().contains("Error marking record as returned: Update failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testMarkAsReturned_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            boolean result = borrowRecordDAO.markAsReturned(1, LocalDate.now(), 5.0);
            assertFalse(result);
        }
    }

    // ------------------ countActiveByUserId() ------------------
    @Test
    void testCountActiveByUserId_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("count")).thenReturn(3);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);
            int count = borrowRecordDAO.countActiveByUserId(1);
            assertEquals(3, count);
        }
    }

    @Test
    void testCountActiveByUserId_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            int count = borrowRecordDAO.countActiveByUserId(1);
            assertEquals(0, count);
        }
    }

    @Test
    void testCountActiveByUserId_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenThrow(new SQLException("Query failed"));

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);
            int count = borrowRecordDAO.countActiveByUserId(1);
            assertEquals(0, count);
            assertTrue(errContent.toString().contains("Error counting active records: Query failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    // ------------------ getUsersWithOverdueBooks() ------------------
    @Test
    void testGetUsersWithOverdueBooks_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(1);
        when(mockRs.getString("username")).thenReturn("user1");
        when(mockRs.getInt("overdue_count")).thenReturn(2);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            List<UserWithOverdueBooks> users = borrowRecordDAO.getUsersWithOverdueBooks();
            assertEquals(1, users.size());
            assertEquals("user1", users.get(0).getUsername());
            assertEquals(2, users.get(0).getOverdueCount());
        }
    }

    @Test
    void testGetUsersWithOverdueBooks_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);

            List<UserWithOverdueBooks> users = borrowRecordDAO.getUsersWithOverdueBooks();
            assertTrue(users.isEmpty());
        }
    }

    @Test
    void testGetUsersWithOverdueBooks_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenThrow(new SQLException("Query failed"));

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            List<UserWithOverdueBooks> users = borrowRecordDAO.getUsersWithOverdueBooks();
            assertTrue(users.isEmpty());
            assertTrue(errContent.toString().contains("Error getting users with overdue books: Query failed"));
        } finally {
            System.setErr(System.err);
        }
    }
}
