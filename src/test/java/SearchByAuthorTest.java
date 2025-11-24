
import dao.BookDAO;
import model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import strategy.SearchByAuthor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SearchByAuthorTest {

    @Mock
    private BookDAO mockBookDAO;

    @Mock
    private Book mockBook1;

    @Mock
    private Book mockBook2;

    private SearchByAuthor searchByAuthor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        searchByAuthor = new SearchByAuthor();
        try {
            java.lang.reflect.Field daoField = SearchByAuthor.class.getDeclaredField("bookDAO");
            daoField.setAccessible(true);
            daoField.set(searchByAuthor, mockBookDAO);
        } catch (Exception e) {
            fail("Failed to inject mock DAO: " + e.getMessage());
        }
    }

    @Test
    public void testSearchWithValidAuthor() {
        String authorName = "Joshua Bloch";
        List<Book> expectedBooks = Arrays.asList(mockBook1, mockBook2);
        when(mockBookDAO.searchByAuthor(authorName)).thenReturn(expectedBooks);

        List<Book> result = searchByAuthor.search(authorName);

        assertEquals(expectedBooks, result);
        verify(mockBookDAO).searchByAuthor(authorName);
    }

    @Test
    public void testSearchWithNoMatchingBooks() {
        String authorName = "Unknown Author";
        List<Book> expectedBooks = Collections.emptyList();
        when(mockBookDAO.searchByAuthor(authorName)).thenReturn(expectedBooks);

        List<Book> result = searchByAuthor.search(authorName);

        assertEquals(expectedBooks, result);
        verify(mockBookDAO).searchByAuthor(authorName);
    }

    @Test
    public void testSearchWithPartialAuthorName() {
        String partialAuthorName = "Bloch";
        List<Book> expectedBooks = Collections.singletonList(mockBook1);
        when(mockBookDAO.searchByAuthor(partialAuthorName)).thenReturn(expectedBooks);

        List<Book> result = searchByAuthor.search(partialAuthorName);

        assertEquals(expectedBooks, result);
        verify(mockBookDAO).searchByAuthor(partialAuthorName);
    }

    @Test
    public void testConstructor() {
        assertNotNull(searchByAuthor);
    }
}