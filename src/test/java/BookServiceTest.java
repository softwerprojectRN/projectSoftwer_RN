import service.*;

import dao.BookDAO;
import dao.MediaDAO;
import model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookServiceTest {

    private BookService bookService;
    private BookDAO bookDAOMock;
    private MediaDAO mediaDAOMock;

    @BeforeEach
    public void setup() throws Exception {
        bookService = new BookService();

        bookDAOMock = Mockito.mock(BookDAO.class);
        mediaDAOMock = Mockito.mock(MediaDAO.class);

        inject(bookService, "bookDAO", bookDAOMock);
        inject(bookService, "mediaDAO", mediaDAOMock);
    }

    private void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = BookService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // -------------------------------------------------------------------------
    // addBook() Tests
    // -------------------------------------------------------------------------

    @Test
    public void testAddBook_Success() {
        String title = "Clean Code";
        String author = "Robert C. Martin";
        String isbn = "9780132350884";
        when(bookDAOMock.findByISBN(isbn)).thenReturn(null);
        when(mediaDAOMock.insert(title, "book")).thenReturn(10);
        when(bookDAOMock.insert(10, author, isbn)).thenReturn(1);

        Book result = bookService.addBook(title, author, isbn);

        assertNotNull(result);

        assertEquals(title, result.getTitle());
        assertEquals(author, result.getAuthor());
        assertEquals(isbn, result.getIsbn());
        assertTrue(result.isAvailable());

        verify(mediaDAOMock).insert(title, "book");
        verify(bookDAOMock).insert(10, author, isbn);
    }

    @Test
    public void testAddBook_Fails_EmptyFields() {
        Book result = bookService.addBook("", "Author", "ISBN");
        assertNull(result);

        result = bookService.addBook("Title", "", "ISBN");
        assertNull(result);

        result = bookService.addBook("Title", "Author", "");
        assertNull(result);

        verify(bookDAOMock, never()).insert(anyInt(), anyString(), anyString());
    }

    @Test
    public void testAddBook_Fails_BookAlreadyExists() {
        String title = "Clean Code";
        String author = "Robert C. Martin";
        String isbn = "123";

        when(bookDAOMock.findByISBN("123"))
                .thenReturn(new Book(1, title, author, isbn, true));

        Book result = bookService.addBook("ABC", "Author", "123");

        assertNull(result);
        verify(mediaDAOMock, never()).insert(anyString(), anyString());
    }


    @Test
    public void testAddBook_Fails_MediaInsertError() {
        when(bookDAOMock.findByISBN("555")).thenReturn(null);
        when(mediaDAOMock.insert("Title", "book")).thenReturn(-1);

        Book result = bookService.addBook("Title", "Author", "555");

        assertNull(result);
        verify(bookDAOMock, never()).insert(anyInt(), anyString(), anyString());
    }

    @Test
    public void testAddBook_Fails_BookInsertError() {
        when(bookDAOMock.findByISBN("999")).thenReturn(null);
        when(mediaDAOMock.insert("Title", "book")).thenReturn(20);
        when(bookDAOMock.insert(20, "Author", "999")).thenReturn(-1);

        Book result = bookService.addBook("Title", "Author", "999");

        assertNull(result);
    }

    // -------------------------------------------------------------------------
    // getAllBooks() Tests
    // -------------------------------------------------------------------------

    @Test
    public void testGetAllBooks_ReturnsList() {
        List<Book> books = List.of(
                new Book(1, "Book A", "Author A", "111", true),
                new Book(2, "Book B", "Author B", "222", true)
        );

        when(bookDAOMock.findAll()).thenReturn(books);

        List<Book> result = bookService.getAllBooks();

        assertEquals(2, result.size());
        verify(bookDAOMock).findAll();
    }

    // -------------------------------------------------------------------------
    // searchBooks() Tests
    // -------------------------------------------------------------------------

    @Test
    public void testSearchBooks_ByTitle() {
        when(bookDAOMock.searchByTitle("Design")).thenReturn(List.of());

        bookService.searchBooks("Design", "title");
        verify(bookDAOMock).searchByTitle("Design");
    }

    @Test
    public void testSearchBooks_ByAuthor() {
        when(bookDAOMock.searchByAuthor("Martin")).thenReturn(List.of());

        bookService.searchBooks("Martin", "author");
        verify(bookDAOMock).searchByAuthor("Martin");
    }

    @Test
    public void testSearchBooks_ByISBNPattern() {
        when(bookDAOMock.searchByISBNPattern("978")).thenReturn(List.of());

        bookService.searchBooks("978", "isbn");
        verify(bookDAOMock).searchByISBNPattern("978");
    }

    @Test
    public void testSearchBooks_InvalidType_UsesTitle() {
        when(bookDAOMock.searchByTitle("Test")).thenReturn(List.of());

        bookService.searchBooks("Test", "unknown");
        verify(bookDAOMock).searchByTitle("Test");
    }

    @Test
    public void testSearchBooks_EmptySearchTerm() {
        List<Book> result = bookService.searchBooks("", "title");

        assertTrue(result.isEmpty());
        verify(bookDAOMock, never()).searchByTitle(anyString());
    }
}
