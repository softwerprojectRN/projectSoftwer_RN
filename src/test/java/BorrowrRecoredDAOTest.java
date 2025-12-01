import dao.*;
import model.MediaRecord;
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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            borrowRecordDAO.initializeTable();
            assertTrue(errContent.toString().contains("Error creating borrow_records table: Create table failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testInitializeTable_connectionNull() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);
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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            int id = borrowRecordDAO.insert(1, 2, "book", "Title", LocalDate.now(), LocalDate.now().plusDays(7));
            assertEquals(-1, id);
            assertTrue(errContent.toString().contains("Error inserting borrow record: Insert failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testInsert_connectionNull() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);
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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = borrowRecordDAO.markAsReturned(1, LocalDate.now(), 5.0);
            assertFalse(result);
            assertTrue(errContent.toString().contains("Error marking record as returned: Update failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testMarkAsReturned_connectionNull() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);
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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            int count = borrowRecordDAO.countActiveByUserId(1);
            assertEquals(3, count);
        }
    }

    @Test
    void testCountActiveByUserId_connectionNull() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);
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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            List<UserWithOverdueBooks> users = borrowRecordDAO.getUsersWithOverdueBooks();
            assertEquals(1, users.size());
            assertEquals("user1", users.get(0).getUsername());
            assertEquals(2, users.get(0).getOverdueCount());
        }
    }

    @Test
    void testGetUsersWithOverdueBooks_connectionNull() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);

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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            List<UserWithOverdueBooks> users = borrowRecordDAO.getUsersWithOverdueBooks();
            assertTrue(users.isEmpty());
            assertTrue(errContent.toString().contains("Error getting users with overdue books: Query failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testFindActiveByUserId_book() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        PreparedStatement mockBookStmt = mock(PreparedStatement.class);
        ResultSet mockBookRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(contains("FROM borrow_records br"))).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("media_id")).thenReturn(1);
        when(mockRs.getString("media_type")).thenReturn("book");
        when(mockRs.getString("media_title")).thenReturn("Book Title");
        when(mockRs.getInt("id")).thenReturn(101);
        when(mockRs.getString("due_date")).thenReturn(LocalDate.now().plusDays(5).toString());

        when(mockConn.prepareStatement(contains("FROM books b"))).thenReturn(mockBookStmt);
        when(mockBookStmt.executeQuery()).thenReturn(mockBookRs);
        when(mockBookRs.next()).thenReturn(true);
        when(mockBookRs.getString("author")).thenReturn("Author A");
        when(mockBookRs.getString("isbn")).thenReturn("12345");

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            List<MediaRecord> records = borrowRecordDAO.findActiveByUserId(1);

            assertEquals(1, records.size());
            assertTrue(records.get(0).getMedia() instanceof Book);
            assertEquals("Book Title", records.get(0).getMedia().getTitle());
        }
    }

    @Test
    void testFindActiveByUserId_cd() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        PreparedStatement mockCdStmt = mock(PreparedStatement.class);
        ResultSet mockCdRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(contains("FROM borrow_records br"))).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("media_id")).thenReturn(2);
        when(mockRs.getString("media_type")).thenReturn("cd");
        when(mockRs.getString("media_title")).thenReturn("CD Title");
        when(mockRs.getInt("id")).thenReturn(102);
        when(mockRs.getString("due_date")).thenReturn(LocalDate.now().plusDays(3).toString());

        when(mockConn.prepareStatement(contains("FROM cds c"))).thenReturn(mockCdStmt);
        when(mockCdStmt.executeQuery()).thenReturn(mockCdRs);
        when(mockCdRs.next()).thenReturn(true);
        when(mockCdRs.getString("artist")).thenReturn("Artist X");
        when(mockCdRs.getString("genre")).thenReturn("Pop");
        when(mockCdRs.getInt("duration")).thenReturn(60);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            List<MediaRecord> records = borrowRecordDAO.findActiveByUserId(1);

            assertEquals(1, records.size());
            assertTrue(records.get(0).getMedia() instanceof CD);
            assertEquals("CD Title", records.get(0).getMedia().getTitle());
        }
    }

    @Test
    void testFindActiveByUserId_noRecords() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            List<MediaRecord> records = borrowRecordDAO.findActiveByUserId(1);
            assertTrue(records.isEmpty());
        }
    }

    @Test
    void testFindActiveByUserId_connectionNull() {
        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);
            List<MediaRecord> records = borrowRecordDAO.findActiveByUserId(1);
            assertTrue(records.isEmpty());
        }
    }

    @Test
    void testFindActiveByUserId_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenThrow(new SQLException("Query failed"));

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            List<MediaRecord> records = borrowRecordDAO.findActiveByUserId(1);
            assertTrue(records.isEmpty());
            assertTrue(errContent.toString().contains("Error loading borrowed media"));
        } finally {
            System.setErr(System.err);
        }
    }
    @Test
    void testFindOverdueByUserId() {
        MediaRecord overdue = mock(MediaRecord.class);
        when(overdue.isOverdue()).thenReturn(true);
        MediaRecord notOverdue = mock(MediaRecord.class);
        when(notOverdue.isOverdue()).thenReturn(false);

        BorrowRecordDAO spyDAO = spy(borrowRecordDAO);
        doReturn(List.of(overdue, notOverdue)).when(spyDAO).findActiveByUserId(1);

        List<MediaRecord> overdueRecords = spyDAO.findOverdueByUserId(1);
        assertEquals(1, overdueRecords.size());
        assertTrue(overdueRecords.contains(overdue));
    }
    @Test
    void testMarkAsReturned_noRowsUpdated() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(0);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            assertFalse(borrowRecordDAO.markAsReturned(1, LocalDate.now(), 5.0));
        }
    }

}
