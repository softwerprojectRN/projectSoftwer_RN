//
//
//import dao.AdminDAO;
//import dao.DatabaseConnection;
//import model.Admin;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.sql.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class AdminDAOTest {
//
//    @Mock
//    private Connection mockConnection;
//
//    @Mock
//    private PreparedStatement mockPreparedStatement;
//
//    @Mock
//    private Statement mockStatement;
//
//    @Mock
//    private ResultSet mockResultSet;
//
//    private AdminDAO adminDAO;
//    private MockedStatic<DatabaseConnection> mockedDatabaseConnection;
//
//    @BeforeEach
//    void setUp() {
//        // Mock the static DatabaseConnection.getConnection() method
//        mockedDatabaseConnection = mockStatic(DatabaseConnection.class);
//        mockedDatabaseConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
//        adminDAO = new AdminDAO();
//    }
//
//    @AfterEach
//    void tearDown() {
//        if (mockedDatabaseConnection != null) {
//            mockedDatabaseConnection.close();
//        }
//    }
//
//    @Test
//    void testInitializeTable() throws SQLException {
//        // Setup
//        when(mockConnection.createStatement()).thenReturn(mockStatement);
//
//        // Execute
//        adminDAO.initializeTable();
//
//        // Verify
//        verify(mockStatement).execute(contains("CREATE TABLE IF NOT EXISTS admins"));
//    }
//
//    @Test
//    void testInitializeTableWithSQLException() throws SQLException {
//        // Setup
//        when(mockConnection.createStatement()).thenReturn(mockStatement);
//        doThrow(new SQLException("Table creation failed")).when(mockStatement).execute(anyString());
//
//        // Execute - should not throw an exception
//        adminDAO.initializeTable();
//
//        // Verify
//        verify(mockStatement).execute(contains("CREATE TABLE IF NOT EXISTS admins"));
//    }
//
//    @Test
//    void testFindByUsernameSuccess() throws SQLException {
//        // Setup
//        String username = "admin";
//        int expectedId = 1;
//        String expectedPasswordHash = "hashedpassword";
//        String expectedSalt = "salt123";
//
//        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
//        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
//        when(mockResultSet.next()).thenReturn(true);
//        when(mockResultSet.getInt("id")).thenReturn(expectedId);
//        when(mockResultSet.getString("username")).thenReturn(username);
//        when(mockResultSet.getString("password_hash")).thenReturn(expectedPasswordHash);
//        when(mockResultSet.getString("salt")).thenReturn(expectedSalt);
//
//        // Execute
//        Admin result = adminDAO.findByUsername(username);
//
//        // Verify
//        assertNotNull(result);
//        assertEquals(expectedId, result.getId());
//        assertEquals(username, result.getUsername());
//        assertEquals(expectedPasswordHash, result.getPasswordHash());
//        assertEquals(expectedSalt, result.getSalt());
//
//        verify(mockPreparedStatement).setObject(1, username);
//        verify(mockPreparedStatement).executeQuery();
//        verify(mockResultSet).next();
//    }
//
//    @Test
//    void testFindByUsernameNotFound() throws SQLException {
//        // Setup
//        String username = "nonexistent";
//
//        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
//        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
//        when(mockResultSet.next()).thenReturn(false);
//
//        // Execute
//        Admin result = adminDAO.findByUsername(username);
//
//        // Verify
//        assertNull(result);
//        verify(mockPreparedStatement).setObject(1, username);
//        verify(mockPreparedStatement).executeQuery();
//        verify(mockResultSet).next();
//    }
//
//    @Test
//    void testFindByUsernameWithSQLException() throws SQLException {
//        // Setup
//        String username = "admin";
//
//        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
//        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Query failed"));
//
//        // Execute
//        Admin result = adminDAO.findByUsername(username);
//
//        // Verify
//        assertNull(result);
//        verify(mockPreparedStatement).setObject(1, username);
//        verify(mockPreparedStatement).executeQuery();
//    }
//
//    @Test
//    void testInsertSuccess() throws SQLException {
//        // Setup
//        String username = "newadmin";
//        String passwordHash = "hashedpassword";
//        String salt = "salt123";
//        int expectedId = 2;
//
//        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
//                .thenReturn(mockPreparedStatement);
//        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
//        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
//        when(mockResultSet.next()).thenReturn(true);
//        when(mockResultSet.getInt(1)).thenReturn(expectedId);
//
//        // Execute
//        boolean result = adminDAO.insert(username, passwordHash, salt);
//
//        // Verify
//        assertTrue(result);
//        verify(mockPreparedStatement).setObject(1, username);
//        verify(mockPreparedStatement).setObject(2, passwordHash);
//        verify(mockPreparedStatement).setObject(3, salt);
//        verify(mockPreparedStatement).executeUpdate();
//    }
//
//    @Test
//    void testInsertFailure() throws SQLException {
//        // Setup
//        String username = "newadmin";
//        String passwordHash = "hashedpassword";
//        String salt = "salt123";
//
//        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
//                .thenReturn(mockPreparedStatement);
//        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
//
//        // Execute
//        boolean result = adminDAO.insert(username, passwordHash, salt);
//
//        // Verify
//        assertFalse(result);
//        verify(mockPreparedStatement).setObject(1, username);
//        verify(mockPreparedStatement).setObject(2, passwordHash);
//        verify(mockPreparedStatement).setObject(3, salt);
//        verify(mockPreparedStatement).executeUpdate();
//    }
//
//    @Test
//    void testInsertWithSQLException() throws SQLException {
//        // Setup
//        String username = "newadmin";
//        String passwordHash = "hashedpassword";
//        String salt = "salt123";
//
//        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
//                .thenReturn(mockPreparedStatement);
//        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Insert failed"));
//
//        // Execute
//        boolean result = adminDAO.insert(username, passwordHash, salt);
//
//        // Verify
//        assertFalse(result);
//        verify(mockPreparedStatement).setObject(1, username);
//        verify(mockPreparedStatement).setObject(2, passwordHash);
//        verify(mockPreparedStatement).setObject(3, salt);
//        verify(mockPreparedStatement).executeUpdate();
//    }
//}