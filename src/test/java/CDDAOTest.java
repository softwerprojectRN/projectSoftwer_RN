
import dao.CDDAO;
import dao.DatabaseConnection;
import model.CD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CDDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSet mockGeneratedKeys;

    private CDDAO cdDAO;
    private MockedStatic<DatabaseConnection> mockedDatabaseConnection;

    @BeforeEach
    void setUp() {
        // Mock the static DatabaseConnection.getConnection() method
        mockedDatabaseConnection = mockStatic(DatabaseConnection.class);
        mockedDatabaseConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
        cdDAO = new CDDAO();
    }

    @AfterEach
    void tearDown() {
        if (mockedDatabaseConnection != null) {
            mockedDatabaseConnection.close();
        }
    }

    @Test
    void testInitializeTable() throws SQLException {
        // Setup
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        // Execute
        cdDAO.initializeTable();

        // Verify
        verify(mockStatement).execute(contains("CREATE TABLE IF NOT EXISTS cds"));
    }

    @Test
    void testInitializeTableWithSQLException() throws SQLException {
        // Setup
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        doThrow(new SQLException("Table creation failed")).when(mockStatement).execute(anyString());

        // Execute - should not throw an exception
        cdDAO.initializeTable();

        // Verify
        verify(mockStatement).execute(contains("CREATE TABLE IF NOT EXISTS cds"));
    }

    @Test
    void testFindById() throws SQLException {
        // Setup
        int id = 1;
        String expectedTitle = "Test CD";
        String expectedArtist = "Test Artist";
        String expectedGenre = "Test Genre";
        int expectedDuration = 60;
        boolean expectedAvailable = true;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(id);
        when(mockResultSet.getString("title")).thenReturn(expectedTitle);
        when(mockResultSet.getString("artist")).thenReturn(expectedArtist);
        when(mockResultSet.getString("genre")).thenReturn(expectedGenre);
        when(mockResultSet.getInt("duration")).thenReturn(expectedDuration);
        when(mockResultSet.getInt("available")).thenReturn(1);

        // Execute
        CD result = cdDAO.findById(id);

        // Verify
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(expectedTitle, result.getTitle());
        assertEquals(expectedArtist, result.getArtist());
        assertEquals(expectedGenre, result.getGenre());
        assertEquals(expectedDuration, result.getDuration());
        assertEquals(expectedAvailable, result.isAvailable());

        verify(mockPreparedStatement).setObject(1, id);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testFindByIdNotFound() throws SQLException {
        // Setup
        int id = 999;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Execute
        CD result = cdDAO.findById(id);

        // Verify
        assertNull(result);
        verify(mockPreparedStatement).setObject(1, id);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testInsert() throws SQLException {
        // Setup
        int mediaId = 1;
        String artist = "Test Artist";
        String genre = "Test Genre";
        int duration = 60;
        int expectedId = 2;

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(expectedId);

        // Execute
        int result = cdDAO.insert(mediaId, artist, genre, duration);

        // Verify
        assertEquals(expectedId, result);
        verify(mockPreparedStatement).setObject(1, mediaId);
        verify(mockPreparedStatement).setObject(2, artist);
        verify(mockPreparedStatement).setObject(3, genre);
        verify(mockPreparedStatement).setObject(4, duration);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testInsertFailure() throws SQLException {
        // Setup
        int mediaId = 1;
        String artist = "Test Artist";
        String genre = "Test Genre";
        int duration = 60;

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(false);

        // Execute
        int result = cdDAO.insert(mediaId, artist, genre, duration);

        // Verify
        assertEquals(-1, result);
        verify(mockPreparedStatement).setObject(1, mediaId);
        verify(mockPreparedStatement).setObject(2, artist);
        verify(mockPreparedStatement).setObject(3, genre);
        verify(mockPreparedStatement).setObject(4, duration);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testFindAll() throws SQLException {
        // Setup
        CD[] expectedCDs = {
                new CD(1, "CD 1", "Artist 1", "Genre 1", 60, true),
                new CD(2, "CD 2", "Artist 2", "Genre 2", 45, false),
                new CD(3, "CD 3", "Artist 3", "Genre 3", 75, true)
        };

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2, 3);
        when(mockResultSet.getString("title")).thenReturn("CD 1", "CD 2", "CD 3");
        when(mockResultSet.getString("artist")).thenReturn("Artist 1", "Artist 2", "Artist 3");
        when(mockResultSet.getString("genre")).thenReturn("Genre 1", "Genre 2", "Genre 3");
        when(mockResultSet.getInt("duration")).thenReturn(60, 45, 75);
        when(mockResultSet.getInt("available")).thenReturn(1, 0, 1);

        // Execute
        List<CD> result = cdDAO.findAll();

        // Verify
        assertNotNull(result);
        assertEquals(3, result.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(expectedCDs[i].getId(), result.get(i).getId());
            assertEquals(expectedCDs[i].getTitle(), result.get(i).getTitle());
            assertEquals(expectedCDs[i].getArtist(), result.get(i).getArtist());
            assertEquals(expectedCDs[i].getGenre(), result.get(i).getGenre());
            assertEquals(expectedCDs[i].getDuration(), result.get(i).getDuration());
            assertEquals(expectedCDs[i].isAvailable(), result.get(i).isAvailable());
        }

        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testSearchByTitle() throws SQLException {
        // Setup
        String title = "Test";
        CD[] expectedCDs = {
                new CD(1, "Test CD 1", "Artist 1", "Genre 1", 60, true),
                new CD(2, "Test CD 2", "Artist 2", "Genre 2", 45, false)
        };

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("title")).thenReturn("Test CD 1", "Test CD 2");
        when(mockResultSet.getString("artist")).thenReturn("Artist 1", "Artist 2");
        when(mockResultSet.getString("genre")).thenReturn("Genre 1", "Genre 2");
        when(mockResultSet.getInt("duration")).thenReturn(60, 45);
        when(mockResultSet.getInt("available")).thenReturn(1, 0);

        // Execute
        List<CD> result = cdDAO.searchByTitle(title);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        for (int i = 0; i < 2; i++) {
            assertEquals(expectedCDs[i].getId(), result.get(i).getId());
            assertEquals(expectedCDs[i].getTitle(), result.get(i).getTitle());
            assertEquals(expectedCDs[i].getArtist(), result.get(i).getArtist());
            assertEquals(expectedCDs[i].getGenre(), result.get(i).getGenre());
            assertEquals(expectedCDs[i].getDuration(), result.get(i).getDuration());
            assertEquals(expectedCDs[i].isAvailable(), result.get(i).isAvailable());
        }

        verify(mockPreparedStatement).setObject(1, "%" + title + "%");
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testSearchByArtist() throws SQLException {
        // Setup
        String artist = "Test";
        CD expectedCD = new CD(1, "Test CD", "Test Artist", "Genre 1", 60, true);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("title")).thenReturn("Test CD");
        when(mockResultSet.getString("artist")).thenReturn("Test Artist");
        when(mockResultSet.getString("genre")).thenReturn("Genre 1");
        when(mockResultSet.getInt("duration")).thenReturn(60);
        when(mockResultSet.getInt("available")).thenReturn(1);

        // Execute
        List<CD> result = cdDAO.searchByArtist(artist);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedCD.getId(), result.get(0).getId());
        assertEquals(expectedCD.getTitle(), result.get(0).getTitle());
        assertEquals(expectedCD.getArtist(), result.get(0).getArtist());
        assertEquals(expectedCD.getGenre(), result.get(0).getGenre());
        assertEquals(expectedCD.getDuration(), result.get(0).getDuration());
        assertEquals(expectedCD.isAvailable(), result.get(0).isAvailable());

        verify(mockPreparedStatement).setObject(1, "%" + artist + "%");
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testSearchByGenre() throws SQLException {
        // Setup
        String genre = "Test";
        CD[] expectedCDs = {
                new CD(1, "CD 1", "Artist 1", "Test Genre 1", 60, true),
                new CD(2, "CD 2", "Artist 2", "Test Genre 2", 45, false)
        };

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("title")).thenReturn("CD 1", "CD 2");
        when(mockResultSet.getString("artist")).thenReturn("Artist 1", "Artist 2");
        when(mockResultSet.getString("genre")).thenReturn("Test Genre 1", "Test Genre 2");
        when(mockResultSet.getInt("duration")).thenReturn(60, 45);
        when(mockResultSet.getInt("available")).thenReturn(1, 0);

        // Execute
        List<CD> result = cdDAO.searchByGenre(genre);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        for (int i = 0; i < 2; i++) {
            assertEquals(expectedCDs[i].getId(), result.get(i).getId());
            assertEquals(expectedCDs[i].getTitle(), result.get(i).getTitle());
            assertEquals(expectedCDs[i].getArtist(), result.get(i).getArtist());
            assertEquals(expectedCDs[i].getGenre(), result.get(i).getGenre());
            assertEquals(expectedCDs[i].getDuration(), result.get(i).getDuration());
            assertEquals(expectedCDs[i].isAvailable(), result.get(i).isAvailable());
        }

        verify(mockPreparedStatement).setObject(1, "%" + genre + "%");
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testSearchMethodsReturnEmptyList() throws SQLException {
        // Setup
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Execute
        List<CD> result1 = cdDAO.searchByTitle("Nonexistent");
        List<CD> result2 = cdDAO.searchByArtist("Nonexistent");
        List<CD> result3 = cdDAO.searchByGenre("Nonexistent");

        // Verify
        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
        assertTrue(result3.isEmpty());
    }
}