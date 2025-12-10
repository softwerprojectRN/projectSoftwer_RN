//import dao.*;
//
//import model.Admin;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.MockedStatic;
//
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//import java.sql.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class AdminDAOTest {
//
//    private AdminDAO adminDAO;
//
//    @BeforeEach
//    void setUp() {
//        adminDAO = new AdminDAO();
//    }
//
//    // ------------------ initializeTable() ------------------
//
//    @Test
//    void testInitializeTable_success() throws SQLException {
//        Connection mockConn = mock(Connection.class);
//        Statement mockStmt = mock(Statement.class);
//
//        when(mockConn.createStatement()).thenReturn(mockStmt);
//
//        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
//            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
//
//            adminDAO.initializeTable();
//
//            verify(mockStmt, times(1)).execute(anyString());
//        }
//    }
//
//    @Test
//    void testInitializeTable_connectionNull() {
//        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
//            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);
//            assertDoesNotThrow(() -> adminDAO.initializeTable());
//        }
//    }
//
//    @Test
//    void testInitializeTable_sqlException() throws SQLException {
//        Connection mockConn = mock(Connection.class);
//        Statement mockStmt = mock(Statement.class);
//
//        when(mockConn.createStatement()).thenReturn(mockStmt);
//        doThrow(new SQLException("Create table failed")).when(mockStmt).execute(anyString());
//
//        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
//            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
//            assertDoesNotThrow(() -> adminDAO.initializeTable());
//        }
//    }
//
//    // ------------------ findByUsername() ------------------
//
//    @Test
//    void testFindByUsername_found() throws SQLException {
//        Connection mockConn = mock(Connection.class);
//        PreparedStatement mockStmt = mock(PreparedStatement.class);
//        ResultSet mockRs = mock(ResultSet.class);
//
//        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
//        when(mockStmt.executeQuery()).thenReturn(mockRs);
//        when(mockRs.next()).thenReturn(true);
//        when(mockRs.getInt("id")).thenReturn(1);
//        when(mockRs.getString("username")).thenReturn("admin");
//        when(mockRs.getString("password_hash")).thenReturn("hash");
//        when(mockRs.getString("salt")).thenReturn("salt");
//
//        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
//            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
//
//            Admin admin = adminDAO.findByUsername("admin");
//
//            assertNotNull(admin);
//            assertEquals(1, admin.getId());
//            assertEquals("admin", admin.getUsername());
//            assertEquals("hash", admin.getPasswordHash());
//            assertEquals("salt", admin.getSalt());
//        }
//    }
//
//    @Test
//    void testFindByUsername_notFound() throws SQLException {
//        Connection mockConn = mock(Connection.class);
//        PreparedStatement mockStmt = mock(PreparedStatement.class);
//        ResultSet mockRs = mock(ResultSet.class);
//
//        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
//        when(mockStmt.executeQuery()).thenReturn(mockRs);
//        when(mockRs.next()).thenReturn(false);
//
//        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
//            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
//
//            Admin admin = adminDAO.findByUsername("nonexistent");
//            assertNull(admin);
//        }
//    }
//
//    @Test
//    void testFindByUsername_connectionNull() {
//        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
//            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);
//
//            Admin admin = adminDAO.findByUsername("admin");
//            assertNull(admin);
//        }
//    }
//
//    @Test
//    void testFindByUsername_sqlException() throws SQLException {
//        Connection mockConn = mock(Connection.class);
//        PreparedStatement mockStmt = mock(PreparedStatement.class);
//
//        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
//        when(mockStmt.executeQuery()).thenThrow(new SQLException("Query failed"));
//
//        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
//            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
//
//            Admin admin = adminDAO.findByUsername("admin");
//            assertNull(admin);
//        }
//    }
//
//    // ------------------ insert() ------------------
//
//    @Test
//    void testInsert_success() throws SQLException {
//        Connection mockConn = mock(Connection.class);
//        PreparedStatement mockStmt = mock(PreparedStatement.class);
//
//        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
//        when(mockStmt.executeUpdate()).thenReturn(1);
//
//        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
//            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
//
//            boolean result = adminDAO.insert("admin", "hash", "salt");
//
//            assertTrue(result);
//            verify(mockStmt, times(1)).setString(1, "admin");
//            verify(mockStmt, times(1)).setString(2, "hash");
//            verify(mockStmt, times(1)).setString(3, "salt");
//            verify(mockStmt, times(1)).executeUpdate();
//        }
//    }
//
//    @Test
//    void testInsert_failure() throws SQLException {
//        Connection mockConn = mock(Connection.class);
//        PreparedStatement mockStmt = mock(PreparedStatement.class);
//
//        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
//        doThrow(new SQLException("Insert failed")).when(mockStmt).executeUpdate();
//
//        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
//            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
//
//            boolean result = adminDAO.insert("admin", "hash", "salt");
//            assertFalse(result);
//        }
//    }
//
//    @Test
//    void testInsert_connectionNull() {
//        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
//            dbMock.when(DatabaseConnection::getConnection).thenReturn(null);
//
//            boolean result = adminDAO.insert("admin", "hash", "salt");
//            assertFalse(result);
//        }
//    }
//    @Test
//    void testInitializeTable_printSuccess() throws SQLException {
//        Connection mockConn = mock(Connection.class);
//        Statement mockStmt = mock(Statement.class);
//        when(mockConn.createStatement()).thenReturn(mockStmt);
//
//        // Capture system output
//        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(outContent));
//
//        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
//            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
//
//            adminDAO.initializeTable();
//
//            String output = outContent.toString();
//            assertTrue(output.contains("Admins table created successfully."));
//        } finally {
//            System.setOut(System.out); // Reset
//        }
//    }
//
//    @Test
//    void testInitializeTable_printError() throws SQLException {
//        Connection mockConn = mock(Connection.class);
//        Statement mockStmt = mock(Statement.class);
//
//        when(mockConn.createStatement()).thenReturn(mockStmt);
//        doThrow(new SQLException("Create table failed")).when(mockStmt).execute(anyString());
//
//        // Capture system error
//        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
//        System.setErr(new PrintStream(errContent));
//
//        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
//            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
//
//            adminDAO.initializeTable();
//
//            String output = errContent.toString();
//            assertTrue(output.contains("Error creating admins table: Create table failed"));
//        } finally {
//            System.setErr(System.err); // Reset
//        }
//    }
//
//    // ------------------ findByUsername() ------------------
//
//    @Test
//    void testFindByUsername_printError() throws SQLException {
//        Connection mockConn = mock(Connection.class);
//        PreparedStatement mockStmt = mock(PreparedStatement.class);
//
//        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
//        when(mockStmt.executeQuery()).thenThrow(new SQLException("Query failed"));
//
//        // Capture system error
//        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
//        System.setErr(new PrintStream(errContent));
//
//        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
//            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
//
//            Admin admin = adminDAO.findByUsername("admin");
//            assertNull(admin);
//
//            String output = errContent.toString();
//            assertTrue(output.contains("Error finding admin: Query failed"));
//        } finally {
//            System.setErr(System.err);
//        }
//    }
//
//    @Test
//    void testInsert_printError() throws SQLException {
//        Connection mockConn = mock(Connection.class);
//        PreparedStatement mockStmt = mock(PreparedStatement.class);
//
//        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
//        doThrow(new SQLException("Insert failed")).when(mockStmt).executeUpdate();
//
//        // Capture system error
//        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
//        System.setErr(new PrintStream(errContent));
//
//        try (MockedStatic<DatabaseConnection> dbMock = mockStatic(DatabaseConnection.class)) {
//            dbMock.when(DatabaseConnection::getConnection).thenReturn(mockConn);
//
//            boolean result = adminDAO.insert("admin", "hash", "salt");
//            assertFalse(result);
//
//            String output = errContent.toString();
//            assertTrue(output.contains("Error inserting admin: Insert failed"));
//        } finally {
//            System.setErr(System.err);
//        }
//    }
//
//}
