import domain.CD;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CDTest {

    @Test
    public void testCDConstructor() {
        // Test the constructor
        CD cd = new CD(1, "Test Album", "Test Artist", "Rock", 60, true);

        assertEquals(1, cd.getId());
        assertEquals("Test Album", cd.getTitle());
        assertEquals("Test Artist", cd.getArtist());
        assertEquals("Rock", cd.getGenre());
        assertEquals(60, cd.getDuration());
        assertTrue(cd.isAvailable());
        assertEquals("cd", cd.getMediaType());
    }

    @Test
    public void testToString() {
        // Test the toString method
        CD cd = new CD(1, "Test Album", "Test Artist", "Rock", 60, true);
        String expected = "ID: 1, Title: 'Test Album', Type: cd, Available: Yes, Artist: 'Test Artist', Genre: Rock, Duration: 60 min";
        assertEquals(expected, cd.toString());
    }

    @Test
    public void testAddCDSuccess() throws SQLException {
        // Mock connection and prepared statements
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockMediaStmt = mock(PreparedStatement.class);
        PreparedStatement mockCDStmt = mock(PreparedStatement.class);
        ResultSet mockGeneratedKeys = mock(ResultSet.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                    .thenReturn(mockMediaStmt);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockCDStmt);
            when(mockMediaStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
            when(mockGeneratedKeys.next()).thenReturn(true);
            when(mockGeneratedKeys.getInt(1)).thenReturn(1);

            // Call the method
            CD result = CD.addCD("Test Album", "Test Artist", "Rock", 60);

            // Verify
            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Test Album", result.getTitle());
            assertEquals("Test Artist", result.getArtist());
            assertEquals("Rock", result.getGenre());
            assertEquals(60, result.getDuration());
            assertTrue(result.isAvailable());

            // Verify the interactions
            verify(mockMediaStmt).setString(1, "Test Album");
            verify(mockMediaStmt).executeUpdate();
            verify(mockMediaStmt).getGeneratedKeys();
            verify(mockCDStmt).setInt(1, 1);
            verify(mockCDStmt).setString(2, "Test Artist");
            verify(mockCDStmt).setString(3, "Rock");
            verify(mockCDStmt).setInt(4, 60);
            verify(mockCDStmt).executeUpdate();
        }
    }

    @Test
    public void testAddCDWithNoGeneratedKeys() throws SQLException {
        // Mock connection and prepared statements
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockMediaStmt = mock(PreparedStatement.class);
        ResultSet mockGeneratedKeys = mock(ResultSet.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                    .thenReturn(mockMediaStmt);
            when(mockMediaStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
            when(mockGeneratedKeys.next()).thenReturn(false);

            // Call the method
            CD result = CD.addCD("Test Album", "Test Artist", "Rock", 60);

            // Verify
            assertNull(result);
        }
    }

    @Test
    public void testAddCDWithSQLException() {
        // Mock connection and prepared statements
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockMediaStmt = mock(PreparedStatement.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);

            try {
                when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                        .thenReturn(mockMediaStmt);

                // Create the exception outside the when() call
                SQLException exception = new SQLException("Test exception");
                when(mockMediaStmt.executeUpdate()).thenThrow(exception);

                // Call the method
                CD result = CD.addCD("Test Album", "Test Artist", "Rock", 60);

                // Verify
                assertNull(result);
            } catch (SQLException e) {
                fail("Unexpected exception: " + e.getMessage());
            }
        }
    }

    @Test
    public void testGetAllCDsSuccess() throws SQLException {
        // Mock connection, statement, and result set
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);

            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

            // Mock the result set to return two rows
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getInt("id")).thenReturn(1, 2);
            when(mockResultSet.getString("title")).thenReturn("Album 1", "Album 2");
            when(mockResultSet.getString("artist")).thenReturn("Artist 1", "Artist 2");
            when(mockResultSet.getString("genre")).thenReturn("Rock", "Pop");
            when(mockResultSet.getInt("duration")).thenReturn(60, 45);
            when(mockResultSet.getInt("available")).thenReturn(1, 0);

            // Call the method
            List<CD> result = CD.getAllCDs();

            // Verify
            assertEquals(2, result.size());

            CD cd1 = result.get(0);
            assertEquals(1, cd1.getId());
            assertEquals("Album 1", cd1.getTitle());
            assertEquals("Artist 1", cd1.getArtist());
            assertEquals("Rock", cd1.getGenre());
            assertEquals(60, cd1.getDuration());
            assertTrue(cd1.isAvailable());

            CD cd2 = result.get(1);
            assertEquals(2, cd2.getId());
            assertEquals("Album 2", cd2.getTitle());
            assertEquals("Artist 2", cd2.getArtist());
            assertEquals("Pop", cd2.getGenre());
            assertEquals(45, cd2.getDuration());
            assertFalse(cd2.isAvailable());
        }
    }

    @Test
    public void testGetAllCDsWithSQLException() {
        // Mock connection and statement
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);

            try {
                when(mockConnection.createStatement()).thenReturn(mockStatement);

                // Create the exception outside the when() call
                SQLException exception = new SQLException("Test exception");
                when(mockStatement.executeQuery(anyString())).thenThrow(exception);

                // Call the method
                List<CD> result = CD.getAllCDs();

                // Verify
                assertEquals(0, result.size());
            } catch (SQLException e) {
                fail("Unexpected exception: " + e.getMessage());
            }
        }
    }

    @Test
    public void testInheritedMethods() throws SQLException {
        // Test inherited methods from Media class
        CD cd = new CD(1, "Test Album", "Test Artist", "Rock", 60, true);

        // Mock connection and prepared statement
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);

        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString())).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            // Test borrow method
            cd.borrow();
            assertFalse(cd.isAvailable());

            // Reset the mock for the next test
            reset(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            // Test returnMedia method
            cd.returnMedia();
            assertTrue(cd.isAvailable());

            // Reset the mock for the next test
            reset(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            // Test updateAvailability method
            cd.updateAvailability(false);
            assertFalse(cd.isAvailable());
        }
    }
}