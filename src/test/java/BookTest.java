//import model.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//class BookTest {
//
//    private Book book;
//
//    @BeforeEach
//    void setUp() {
//        book = new Book(1, "Data Structures", "Rahaf Ishtayeh", "ISBN12345", true);
//    }
//
//    @Test
//    void testConstructorAndInheritance() {
//        // Check Media parent fields
//        assertEquals(1, book.getId());
//        assertEquals("Data Structures", book.getTitle());
//        assertTrue(book.isAvailable());
//
//        // Check Book fields
//        assertEquals("Rahaf Ishtayeh", book.getAuthor());
//        assertEquals("ISBN12345", book.getIsbn());
//    }
//
//    @Test
//    void testGetAuthor() {
//        assertEquals("Rahaf Ishtayeh", book.getAuthor());
//    }
//
//    @Test
//    void testGetIsbn() {
//        assertEquals("ISBN12345", book.getIsbn());
//    }
//
//    @Test
//    void testToStringOverride() {
//        String text = book.toString();
//
//        // Check that super.toString() content exists
//        assertTrue(text.contains("ID: 1"));
//        assertTrue(text.contains("Title: 'Data Structures'"));
//        assertTrue(text.contains("Type: book"));
//        assertTrue(text.contains("Available: Yes"));
//
//        // Check Book-specific additions
//        assertTrue(text.contains("Author: 'Rahaf Ishtayeh'"));
//        assertTrue(text.contains("ISBN: ISBN12345"));
//    }
//}
