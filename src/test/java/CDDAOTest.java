import dao.*;

import model.CD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CDDAOTest {

    private CDDAO cdDAO;

    @BeforeEach
    void setUp() {
        cdDAO = new CDDAO();
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

            cdDAO.initializeTable();
            verify(mockStmt).execute(anyString());
            assertTrue(outContent.toString().contains("CDs table created successfully."));
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

            cdDAO.initializeTable();
            assertTrue(errContent.toString().contains("Error creating CDs table: Create failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testInitializeTable_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            assertDoesNotThrow(() -> cdDAO.initializeTable());
        }
    }

    // ------------------ insert() ------------------
    @Test
    void testInsert_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            int id = cdDAO.insert(1, "Artist", "Pop", 60);
            assertEquals(1, id);
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

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            int id = cdDAO.insert(1, "Artist", "Pop", 60);
            assertEquals(-1, id);
            assertTrue(errContent.toString().contains("Error inserting CD: Insert failed"));
        } finally {
            System.setErr(System.err);
        }
    }

    @Test
    void testInsert_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            int id = cdDAO.insert(1, "Artist", "Pop", 60);
            assertEquals(-1, id);
        }
    }

    // ------------------ findById() ------------------
    @Test
    void testFindById_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("id")).thenReturn(1);
        when(mockRs.getString("title")).thenReturn("Album");
        when(mockRs.getString("artist")).thenReturn("Artist");
        when(mockRs.getString("genre")).thenReturn("Pop");
        when(mockRs.getInt("duration")).thenReturn(60);
        when(mockRs.getInt("available")).thenReturn(1);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            CD cd = cdDAO.findById(1);
            assertNotNull(cd);
            assertEquals("Artist", cd.getArtist());
            assertTrue(cd.isAvailable());
        }
    }

    @Test
    void testFindById_notFound() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            CD cd = cdDAO.findById(1);
            assertNull(cd);
        }
    }

    @Test
    void testFindById_connectionNull() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            CD cd = cdDAO.findById(1);
            assertNull(cd);
        }
    }

    // ------------------ findAll() ------------------
    @Test
    void testFindAll_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        Statement mockStmt = mock(Statement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.createStatement()).thenReturn(mockStmt);
        when(mockStmt.executeQuery(anyString())).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, true, false);
        when(mockRs.getInt("id")).thenReturn(1, 2);
        when(mockRs.getString("title")).thenReturn("Album1", "Album2");
        when(mockRs.getString("artist")).thenReturn("Artist1", "Artist2");
        when(mockRs.getString("genre")).thenReturn("Pop", "Rock");
        when(mockRs.getInt("duration")).thenReturn(60, 70);
        when(mockRs.getInt("available")).thenReturn(1, 0);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            List<CD> cds = cdDAO.findAll();
            assertEquals(2, cds.size());
            assertEquals("Album1", cds.get(0).getTitle());
            assertTrue(cds.get(0).isAvailable());
            assertFalse(cds.get(1).isAvailable());
            assertTrue(outContent.toString().contains("Retrieved 2 CDs from database."));
        } finally {
            System.setOut(System.out);
        }
    }

    @Test
    void testSearchByTitle_successMultiple() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, true, false);
        when(mockRs.getInt("id")).thenReturn(1, 2);
        when(mockRs.getString("title")).thenReturn("CD1", "CD2");
        when(mockRs.getString("artist")).thenReturn("A1", "A2");
        when(mockRs.getString("genre")).thenReturn("Pop", "Rock");
        when(mockRs.getInt("duration")).thenReturn(50, 60);
        when(mockRs.getInt("available")).thenReturn(1, 0);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            List<CD> cds = cdDAO.searchByTitle("CD");
            assertEquals(2, cds.size());
            assertTrue(cds.get(0).isAvailable());
            assertFalse(cds.get(1).isAvailable());
        }
    }

    @Test
    void testSearchByTitle_emptyResult() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            List<CD> cds = cdDAO.searchByTitle("NonExist");
            assertTrue(cds.isEmpty());
        }
    }

    @Test
    void testSearchByTitle_sqlException() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        doThrow(new SQLException("Query failed")).when(mockStmt).executeQuery();

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            List<CD> cds = cdDAO.searchByTitle("Any");
            assertTrue(cds.isEmpty());
        }
    }

    @Test
    void testSearchByTitle_nullConnection() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            List<CD> cds = cdDAO.searchByTitle("Any");
            assertTrue(cds.isEmpty());
        }
    }

    // ------------------ searchByArtist() ------------------
    @Test
    void testSearchByArtist_successSingle() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(1);
        when(mockRs.getString("title")).thenReturn("Album1");
        when(mockRs.getString("artist")).thenReturn("Artist1");
        when(mockRs.getString("genre")).thenReturn("Pop");
        when(mockRs.getInt("duration")).thenReturn(40);
        when(mockRs.getInt("available")).thenReturn(1);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);

            List<CD> cds = cdDAO.searchByArtist("Artist1");
            assertEquals(1, cds.size());
            assertEquals("Artist1", cds.get(0).getArtist());
        }
    }

    @Test
    void testSearchByArtist_empty() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);
            List<CD> cds = cdDAO.searchByArtist("NoOne");
            assertTrue(cds.isEmpty());
        }
    }

    // ------------------ searchByGenre() ------------------
    @Test
    void testSearchByGenre_success() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getInt("id")).thenReturn(1);
        when(mockRs.getString("title")).thenReturn("Album");
        when(mockRs.getString("artist")).thenReturn("Artist");
        when(mockRs.getString("genre")).thenReturn("Jazz");
        when(mockRs.getInt("duration")).thenReturn(45);
        when(mockRs.getInt("available")).thenReturn(1);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);
            List<CD> cds = cdDAO.searchByGenre("Jazz");
            assertEquals(1, cds.size());
            assertEquals("Jazz", cds.get(0).getGenre());
        }
    }

    @Test
    void testSearchByGenre_empty() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(mockConn);
            List<CD> cds = cdDAO.searchByGenre("NoGenre");
            assertTrue(cds.isEmpty());
        }
    }

    @Test
    void testSearchByGenre_nullConnection() {
        try (MockedStatic<util.DatabaseConnection> dbMock = mockStatic(util.DatabaseConnection.class)) {
            dbMock.when(util.DatabaseConnection::getConnection).thenReturn(null);
            List<CD> cds = cdDAO.searchByGenre("Any");
            assertTrue(cds.isEmpty());
        }
    }
}
