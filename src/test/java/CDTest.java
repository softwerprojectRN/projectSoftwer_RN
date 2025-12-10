//import model.*;
//
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
//class CDTest {
//
//    @Test
//    void testCDConstructorAndGetters() {
//        CD cd = new CD(101, "Greatest Hits", "Queen", "Rock", 75, true);
//
//        // Verify superclass fields
//        assertEquals(101, cd.getId());
//        assertEquals("Greatest Hits", cd.getTitle());
//        assertTrue(cd.isAvailable());
//        assertEquals("cd", cd.getMediaType());
//
//        // Verify CD-specific fields
//        assertEquals("Queen", cd.getArtist());
//        assertEquals("Rock", cd.getGenre());
//        assertEquals(75, cd.getDuration());
//    }
//
//    @Test
//    void testCDToString() {
//        CD cd = new CD(101, "Greatest Hits", "Queen", "Rock", 75, true);
//        String str = cd.toString();
//
//        // Check that toString contains all relevant info
//        assertTrue(str.contains("101"));
//        assertTrue(str.contains("Greatest Hits"));
//        assertTrue(str.contains("cd"));
//        assertTrue(str.contains("Queen"));
//        assertTrue(str.contains("Rock"));
//        assertTrue(str.contains("75"));
//    }
//
//    @Test
//    void testCDAvailabilityFalse() {
//        CD cd = new CD(102, "Album X", "Artist Y", "Pop", 60, false);
//
//        assertFalse(cd.isAvailable());
//        assertEquals("Album X", cd.getTitle());
//        assertEquals("Artist Y", cd.getArtist());
//        assertEquals("Pop", cd.getGenre());
//        assertEquals(60, cd.getDuration());
//    }
//}
//
