import domain.Book;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class BookTest {
    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book("The Great Gatsby", "F. Scott Fitzgerald", "1234567890");
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("The Great Gatsby", book.getTitle());
        assertEquals("F. Scott Fitzgerald", book.getAuthor());
        assertEquals("1234567890", book.getIsbn());
        assertTrue(book.isAvailable());
    }

    @Test
    void testSetAvailable() {
        book.setAvailable(false);
        assertFalse(book.isAvailable());
        book.setAvailable(true);
        assertTrue(book.isAvailable());
    }

    @Test
    void testBorrow() {
        book.borrow();
        assertFalse(book.isAvailable(), "Book should not be available after borrowing");
    }

    @Test
    void testReturnBook() {
        book.borrow(); // Make it unavailable first
        book.returnBook();
        assertTrue(book.isAvailable(), "Book should be available after return");
    }

    @Test
    void testToStringAvailable() {
        String result = book.toString();
        assertTrue(result.contains("Available: Yes"));
        assertTrue(result.contains("Title: 'The Great Gatsby'"));
    }

    @Test
    void testToStringNotAvailable() {
        book.borrow();
        String result = book.toString();
        assertTrue(result.contains("Available: No"));
    }
}