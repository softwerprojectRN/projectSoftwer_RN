
import dao.BookDAO;
import model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import strategy.SearchByISBN;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SearchByISBNTest {

    @Mock
    private BookDAO mockBookDAO;

    @Mock
    private Book mockBook1;

    @Mock
    private Book mockBook2;

    private SearchByISBN searchByISBN;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        searchByISBN = new SearchByISBN();
        try {
            java.lang.reflect.Field daoField = SearchByISBN.class.getDeclaredField("bookDAO");
            daoField.setAccessible(true);
            daoField.set(searchByISBN, mockBookDAO);
        } catch (Exception e) {
            fail("Failed to inject mock DAO: " + e.getMessage());
        }
    }

    @Test
    public void testSearchWithValidISBN() {
        String isbnPattern = "978-0134685991";
        List<Book> expectedBooks = Arrays.asList(mockBook1, mockBook2);
        when(mockBookDAO.searchByISBNPattern(isbnPattern)).thenReturn(expectedBooks);

        List<Book> result = searchByISBN.search(isbnPattern);

        assertEquals(expectedBooks, result);
        verify(mockBookDAO).searchByISBNPattern(isbnPattern);
    }

    @Test
    public void testSearchWithPartialISBN() {
        String partialISBN = "978-01346";
        List<Book> expectedBooks = Collections.singletonList(mockBook1);
        when(mockBookDAO.searchByISBNPattern(partialISBN)).thenReturn(expectedBooks);

        List<Book> result = searchByISBN.search(partialISBN);

        assertEquals(expectedBooks, result);
        verify(mockBookDAO).searchByISBNPattern(partialISBN);
    }

    @Test
    public void testSearchWithNoMatchingBooks() {
        String isbnPattern = "999-9999999999";
        List<Book> expectedBooks = Collections.emptyList();
        when(mockBookDAO.searchByISBNPattern(isbnPattern)).thenReturn(expectedBooks);

        List<Book> result = searchByISBN.search(isbnPattern);

        assertEquals(expectedBooks, result);
        verify(mockBookDAO).searchByISBNPattern(isbnPattern);
    }

    @Test
    public void testConstructor() {
        assertNotNull(searchByISBN);
    }
}