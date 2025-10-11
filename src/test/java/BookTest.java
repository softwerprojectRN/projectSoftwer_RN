import domain.Book;
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
    void testConstructorSetsFieldsCorrectly() {
        assertEquals("The Great Gatsby", book.getTitle());
        assertEquals("F. Scott Fitzgerald", book.getAuthor());
        assertEquals("1234567890", book.getIsbn());
    }


    @Test
    void testGetTitle() {
        assertEquals("The Great Gatsby", book.getTitle());
    }

    @Test
    void testGetAuthor() {
        assertEquals("F. Scott Fitzgerald", book.getAuthor());
    }

    @Test
    void testGetIsbn() {
        assertEquals("1234567890", book.getIsbn());
    }


    @Test
    void testBookIsAvailableByDefault() {
        assertTrue(book.isAvailable());
    }

    @Test
    void testBorrowChangesAvailabilityToFalse() {
        book.borrow();
        assertFalse(book.isAvailable());
    }

    @Test
    void testReturnBookChangesAvailabilityToTrue() {
        book.borrow();
        book.returnBook();
        assertTrue(book.isAvailable());
    }

    @Test
    void testSetAvailableTrue() {
        book.setAvailable(true);
        assertTrue(book.isAvailable());
    }

    @Test
    void testSetAvailableFalse() {
        book.setAvailable(false);
        assertFalse(book.isAvailable());
    }

    @Test
    void testToStringWhenAvailable() {
        String result = book.toString();
        assertTrue(result.contains("Title: 'The Great Gatsby'"));
        assertTrue(result.contains("Author: 'F. Scott Fitzgerald'"));
        assertTrue(result.contains("ISBN: 1234567890"));
        assertTrue(result.contains("Available: Yes"));
    }

    @Test
    void testToStringWhenNotAvailable() {
        book.borrow();
        String result = book.toString();
        assertTrue(result.contains("Title: 'The Great Gatsby'"));
        assertTrue(result.contains("Author: 'F. Scott Fitzgerald'"));
        assertTrue(result.contains("ISBN: 1234567890"));
        assertTrue(result.contains("Available: No"));
    }

    @Test
    void testBorrowTwiceKeepsUnavailable() {
        book.borrow();
        book.borrow();
        assertFalse(book.isAvailable());
    }

    @Test
    void testReturnBookWhenAlreadyAvailable() {
        // AlreadyAvailable
        book.returnBook();
        assertTrue(book.isAvailable());
    }
}
