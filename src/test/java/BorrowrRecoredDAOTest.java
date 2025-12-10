import dao.BorrowRecordDAO;
import dao.DatabaseConnection;
import model.Book;
import model.CD;
import model.MediaRecord;
import model.UserWithOverdueBooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
        when(mockStmt.execute(anyString())).thenReturn(true);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            borrowRecordDAO.initializeTable();

            verify(mockStmt, times(1)).execute(anyString());
        }
    }

    @Test
    void testInitializeTable_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        when(mockConn.createStatement()).thenReturn(mockStmt);
        when(mockStmt.execute(anyString())).thenThrow(new SQLException("Create table failed"));

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            assertDoesNotThrow(() -> borrowRecordDAO.initializeTable());
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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            int id = borrowRecordDAO.insert(1, 2, "book", "Title", LocalDate.now(), LocalDate.now().plusDays(7));
            assertEquals(-1, id);
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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            boolean result = borrowRecordDAO.markAsReturned(1, LocalDate.now(), 5.0);
            assertFalse(result);
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

    // ------------------ countActiveByUserId() ------------------
    @Test
    void testCountActiveByUserId_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt(1)).thenReturn(3);

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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            int count = borrowRecordDAO.countActiveByUserId(1);
            assertEquals(0, count);
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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            List<UserWithOverdueBooks> users = borrowRecordDAO.getUsersWithOverdueBooks();
            assertTrue(users.isEmpty());
        }
    }

    // ------------------ findActiveByUserId() ------------------
    @Test
    void testFindActiveByUserId_book() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement borrowStmt = mock(PreparedStatement.class);
        ResultSet borrowRs = mock(ResultSet.class);
        PreparedStatement bookStmt = mock(PreparedStatement.class);
        ResultSet bookRs = mock(ResultSet.class);

        // Set up executeQuery mocks first
        when(borrowStmt.executeQuery()).thenReturn(borrowRs);
        when(bookStmt.executeQuery()).thenReturn(bookRs);

        // Set up ResultSet mocks
        when(borrowRs.next()).thenReturn(true, false);
        when(borrowRs.getInt("media_id")).thenReturn(1);
        when(borrowRs.getString("media_type")).thenReturn("book");
        when(borrowRs.getString("media_title")).thenReturn("Book Title");
        when(borrowRs.getInt("id")).thenReturn(101);
        when(borrowRs.getString("due_date")).thenReturn(LocalDate.now().plusDays(5).toString());

        when(bookRs.next()).thenReturn(true);
        when(bookRs.getString("title")).thenReturn("Book Title");
        when(bookRs.getString("author")).thenReturn("Author A");
        when(bookRs.getString("isbn")).thenReturn("12345");

        // Use thenAnswer to return different statements based on SQL content
        when(mockConn.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.contains("borrow_records")) {
                return borrowStmt;
            } else if (sql.contains("books") && sql.contains("JOIN")) {
                return bookStmt;
            }
            return mock(PreparedStatement.class);
        });

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
        PreparedStatement borrowStmt = mock(PreparedStatement.class);
        ResultSet borrowRs = mock(ResultSet.class);
        PreparedStatement cdStmt = mock(PreparedStatement.class);
        ResultSet cdRs = mock(ResultSet.class);

        // Set up executeQuery mocks first
        when(borrowStmt.executeQuery()).thenReturn(borrowRs);
        when(cdStmt.executeQuery()).thenReturn(cdRs);

        // Set up ResultSet mocks
        when(borrowRs.next()).thenReturn(true, false);
        when(borrowRs.getInt("media_id")).thenReturn(2);
        when(borrowRs.getString("media_type")).thenReturn("cd");
        when(borrowRs.getString("media_title")).thenReturn("CD Title");
        when(borrowRs.getInt("id")).thenReturn(102);
        when(borrowRs.getString("due_date")).thenReturn(LocalDate.now().plusDays(3).toString());

        when(cdRs.next()).thenReturn(true);
        when(cdRs.getString("title")).thenReturn("CD Title");
        when(cdRs.getString("artist")).thenReturn("Artist X");
        when(cdRs.getString("genre")).thenReturn("Pop");
        when(cdRs.getInt("duration")).thenReturn(60);

        // Use thenAnswer to return different statements based on SQL content
        when(mockConn.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.contains("borrow_records")) {
                return borrowStmt;
            } else if (sql.contains("cds") && sql.contains("JOIN")) {
                return cdStmt;
            }
            return mock(PreparedStatement.class);
        });

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

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
            List<MediaRecord> records = borrowRecordDAO.findActiveByUserId(1);
            assertTrue(records.isEmpty());
        }
    }

    // ------------------ findOverdueByUserId() ------------------
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






    // ------------------ fetchBookDetails() ------------------
    @Test
    void testFetchBookDetails_notFound() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        BorrowRecordDAO spyDAO = spy(borrowRecordDAO);
        Media result = spyDAO.fetchBookDetails(mockConn, 1);
        assertNull(result);
    }


    // ------------------ fetchCDDetails() ------------------
    @Test
    void testFetchCDDetails_notFound() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        BorrowRecordDAO spyDAO = spy(borrowRecordDAO);
        Media result = spyDAO.fetchCDDetails(mockConn, 2);
        assertNull(result);
    }



    // ------------------ findActiveByUserId() with unknown mediaType ------------------
    @Test
    void testFindActiveByUserId_unknownMediaType() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement borrowStmt = mock(PreparedStatement.class);
        ResultSet borrowRs = mock(ResultSet.class);

        when(borrowStmt.executeQuery()).thenReturn(borrowRs);
        when(borrowRs.next()).thenReturn(true, false);
        when(borrowRs.getInt("media_id")).thenReturn(99);
        when(borrowRs.getString("media_type")).thenReturn("unknown");
        when(borrowRs.getInt("id")).thenReturn(123);
        when(borrowRs.getString("due_date")).thenReturn(LocalDate.now().toString());

        when(mockConn.prepareStatement(anyString())).thenReturn(borrowStmt);

        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);

            List<MediaRecord> records = borrowRecordDAO.findActiveByUserId(1);
            assertTrue(records.isEmpty());
        }
    }

}
