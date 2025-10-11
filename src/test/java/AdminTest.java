
import domain.Admin;
import domain.Book;
import domain.LibrarySystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class AdminTest {

    private Admin admin;
    private LibrarySystem library;

    @BeforeEach
    void setUp() {
        admin = new Admin("adminUser", "adminPass");
        library = new LibrarySystem();
    }

    @Test
    void testGetUsernameReturnsCorrectValue() {
        assertEquals("adminUser", admin.getUsername());
    }

    @Test
    void testGetPasswordReturnsCorrectValue() {
        assertEquals("adminPass", admin.getPassword());
    }

    @Test
    void testAddBookAddsBookToLibrary() {
        // تأكد أنه قبل الإضافة الكتاب مش موجود
        List<Book> searchBefore = library.searchBooks("1984");
        assertEquals(0, searchBefore.size());

        // إضافة الكتاب
        admin.addBook(library, "1984", "George Orwell", "11111");

        // تحقق أن الكتاب أضيف
        List<Book> searchAfter = library.searchBooks("1984");
        assertEquals(1, searchAfter.size());

        Book addedBook = searchAfter.get(0);
        assertEquals("1984", addedBook.getTitle());
        assertEquals("George Orwell", addedBook.getAuthor());
        assertEquals("11111", addedBook.getIsbn());
        assertTrue(addedBook.isAvailable());
    }

    @Test
    void testAddMultipleBooks() {
        admin.addBook(library, "Book1", "Author1", "123");
        admin.addBook(library, "Book2", "Author2", "456");

        List<Book> search1 = library.searchBooks("Book1");
        List<Book> search2 = library.searchBooks("Book2");

        assertEquals(1, search1.size());
        assertEquals(1, search2.size());
    }

    @Test
    void testShowAdminInfoDoesNotThrowException() {
        assertDoesNotThrow(() -> admin.showAdminInfo());
    }
}
