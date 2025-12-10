
import dao.BookDAO;
import model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import service.SearchByTitle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SearchByTitleTest {

    @Mock
    private BookDAO mockBookDAO;

    @Mock
    private Book mockBook1;

    @Mock
    private Book mockBook2;

    private SearchByTitle searchByTitle;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        searchByTitle = new SearchByTitle();
        try {
            java.lang.reflect.Field daoField = SearchByTitle.class.getDeclaredField("bookDAO");
            daoField.setAccessible(true);
            daoField.set(searchByTitle, mockBookDAO);
        } catch (Exception e) {
            fail("Failed to inject mock DAO: " + e.getMessage());
        }
    }

    @Test
    public void testSearchWithValidTitle() {
        String title = "Effective Java";
        List<Book> expectedBooks = Arrays.asList(mockBook1, mockBook2);
        when(mockBookDAO.searchByTitle(title)).thenReturn(expectedBooks);

        List<Book> result = searchByTitle.search(title);

        assertEquals(expectedBooks, result);
        verify(mockBookDAO).searchByTitle(title);
    }

    @Test
    public void testSearchWithPartialTitle() {
        String partialTitle = "Effective";
        List<Book> expectedBooks = Collections.singletonList(mockBook1);
        when(mockBookDAO.searchByTitle(partialTitle)).thenReturn(expectedBooks);

        List<Book> result = searchByTitle.search(partialTitle);

        assertEquals(expectedBooks, result);
        verify(mockBookDAO).searchByTitle(partialTitle);
    }

    @Test
    public void testSearchWithNoMatchingBooks() {
        String title = "Nonexistent Book Title";
        List<Book> expectedBooks = Collections.emptyList();
        when(mockBookDAO.searchByTitle(title)).thenReturn(expectedBooks);

        List<Book> result = searchByTitle.search(title);

        assertEquals(expectedBooks, result);
        verify(mockBookDAO).searchByTitle(title);
    }

    @Test
    public void testConstructor() {
        assertNotNull(searchByTitle);
    }
}