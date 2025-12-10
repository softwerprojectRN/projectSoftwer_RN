//import service.*;
//
//import dao.CDDAO;
//import dao.MediaDAO;
//import model.CD;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.lang.reflect.Field;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//public class CDServiceTest {
//
//    private CDService cdService;
//    private CDDAO cdDAO;
//    private MediaDAO mediaDAO;
//
//    @BeforeEach
//    void setup() throws Exception {
//        cdService = new CDService();
//
//        cdDAO = mock(CDDAO.class);
//        mediaDAO = mock(MediaDAO.class);
//
//        // Inject mocks using reflection
//        Field cdField = CDService.class.getDeclaredField("cdDAO");
//        cdField.setAccessible(true);
//        cdField.set(cdService, cdDAO);
//
//        Field mediaField = CDService.class.getDeclaredField("mediaDAO");
//        mediaField.setAccessible(true);
//        mediaField.set(cdService, mediaDAO);
//    }
//
//    // -------------------------------------------------------------------------
//    // addCD TESTS
//    // -------------------------------------------------------------------------
//
//    @Test
//    void testAddCD_Success() {
//        when(mediaDAO.insert("Moonlight", "cd")).thenReturn(10);
//        when(cdDAO.insert(10, "Beethoven", "Classical", 300)).thenReturn(20);
//
//        CD cd = cdService.addCD("Moonlight", "Beethoven", "Classical", 300);
//
//        assertNotNull(cd);
//        assertEquals("Moonlight", cd.getTitle());
//        assertEquals("Beethoven", cd.getArtist());
//    }
//
//    @Test
//    void testAddCD_InvalidTitle() {
//        CD cd = cdService.addCD("", "Artist", "Rock", 200);
//        assertNull(cd);
//    }
//
//    @Test
//    void testAddCD_InvalidArtist() {
//        CD cd = cdService.addCD("Album", " ", "Pop", 200);
//        assertNull(cd);
//    }
//
//    @Test
//    void testAddCD_InvalidDuration() {
//        CD cd = cdService.addCD("Album", "Artist", "Pop", -10);
//        assertNull(cd);
//    }
//
//    @Test
//    void testAddCD_MediaInsertFails() {
//        when(mediaDAO.insert("Album", "cd")).thenReturn(-1);
//
//        CD cd = cdService.addCD("Album", "Artist", "Pop", 200);
//        assertNull(cd);
//    }
//
//    @Test
//    void testAddCD_CDInsertFails() {
//        when(mediaDAO.insert("Album", "cd")).thenReturn(5);
//        when(cdDAO.insert(5, "Artist", "Pop", 200)).thenReturn(-1);
//
//        CD cd = cdService.addCD("Album", "Artist", "Pop", 200);
//
//        assertNull(cd);
//    }
//
//    // -------------------------------------------------------------------------
//    // getAllCDs TESTS
//    // -------------------------------------------------------------------------
//
//    @Test
//    void testGetAllCDs() {
//        CD cd1 = new CD(1, "A", "Artist1", "Pop", 120, true);
//        CD cd2 = new CD(2, "B", "Artist2", "Rock", 150, true);
//
//        when(cdDAO.findAll()).thenReturn(Arrays.asList(cd1, cd2));
//
//        List<CD> result = cdService.getAllCDs();
//
//        assertEquals(2, result.size());
//    }
//
//    // -------------------------------------------------------------------------
//    // searchCDs TESTS
//    // -------------------------------------------------------------------------
//
//    @Test
//    void testSearchCDs_Title() {
//        when(cdDAO.searchByTitle("Love"))
//                .thenReturn(Collections.singletonList(new CD(1, "Love", "A", "Pop", 200, true)));
//
//        List<CD> result = cdService.searchCDs("Love", "title");
//
//        assertEquals(1, result.size());
//    }
//
//    @Test
//    void testSearchCDs_Artist() {
//        when(cdDAO.searchByArtist("Adele"))
//                .thenReturn(Collections.singletonList(new CD(1, "Hello", "Adele", "Soul", 300, true)));
//
//        List<CD> result = cdService.searchCDs("Adele", "artist");
//
//        assertEquals(1, result.size());
//    }
//
//    @Test
//    void testSearchCDs_Genre() {
//        when(cdDAO.searchByGenre("Jazz"))
//                .thenReturn(Collections.singletonList(new CD(1, "Smooth", "Artist", "Jazz", 220, true)));
//
//        List<CD> result = cdService.searchCDs("Jazz", "genre");
//
//        assertEquals(1, result.size());
//    }
//
//    @Test
//    void testSearchCDs_EmptyInput() {
//        List<CD> result = cdService.searchCDs("", "title");
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void testSearchCDs_InvalidType_DefaultsToTitle() {
//        when(cdDAO.searchByTitle("Test"))
//                .thenReturn(Collections.singletonList(new CD(1, "Test", "X", "Y", 100, true)));
//
//        List<CD> result = cdService.searchCDs("Test", "wrongType");
//
//        assertEquals(1, result.size());
//    }
//
//    // -------------------------------------------------------------------------
//    // findById TESTS
//    // -------------------------------------------------------------------------
//
//    @Test
//    void testFindById() {
//        CD cd = new CD(5, "My CD", "Artist", "Genre", 123, true);
//
//        when(cdDAO.findById(5)).thenReturn(cd);
//
//        CD result = cdService.findById(5);
//
//        assertNotNull(result);
//        assertEquals("My CD", result.getTitle());
//    }
//}
