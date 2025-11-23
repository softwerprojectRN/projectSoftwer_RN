import model.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MediaTest {

    @Test
    void testMediaCreation() {
        Media media = new Media(1, "Book A", true, "book");

        assertEquals(1, media.getId());
        assertEquals("Book A", media.getTitle());
        assertTrue(media.isAvailable());
        assertEquals("book", media.getMediaType());
    }

    @Test
    void testSetAvailable() {
        Media media = new Media(2, "CD Music", false, "cd");

        assertFalse(media.isAvailable());

        media.setAvailable(true);

        assertTrue(media.isAvailable());
    }

    @Test
    void testToStringAvailable() {
        Media media = new Media(3, "Book B", true, "book");
        String expected = "ID: 3, Title: 'Book B', Type: book, Available: Yes";

        assertEquals(expected, media.toString());
    }

    @Test
    void testToStringNotAvailable() {
        Media media = new Media(4, "CD Rock", false, "cd");
        String expected = "ID: 4, Title: 'CD Rock', Type: cd, Available: No";

        assertEquals(expected, media.toString());
    }
}
