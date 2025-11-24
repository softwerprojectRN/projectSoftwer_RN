
import model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import strategy.BookSearcher;
import strategy.SearchStrategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookSearcherTest {

    @Mock
    private SearchStrategy mockStrategy;

    @Mock
    private Book mockBook1;

    @Mock
    private Book mockBook2;

    private BookSearcher bookSearcher;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        bookSearcher = new BookSearcher(mockStrategy);
    }

    @Test
    public void testSearchWithValidTerm() {
        // Arrange
        String searchTerm = "Java Programming";
        List<Book> expectedBooks = Arrays.asList(mockBook1, mockBook2);
        when(mockStrategy.search(searchTerm)).thenReturn(expectedBooks);

        // Act
        List<Book> result = bookSearcher.search(searchTerm);

        // Assert
        assertEquals(expectedBooks, result);
        verify(mockStrategy).search(searchTerm);
    }

    @Test
    public void testSearchWithNullTerm() {
        // Act
        List<Book> result = bookSearcher.search(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(mockStrategy, never()).search(anyString());
    }

    @Test
    public void testSearchWithEmptyTerm() {
        // Act
        List<Book> result = bookSearcher.search("");

        // Assert
        assertTrue(result.isEmpty());
        verify(mockStrategy, never()).search(anyString());
    }

    @Test
    public void testSearchWithWhitespaceOnlyTerm() {
        // Act
        List<Book> result = bookSearcher.search("   ");

        // Assert
        assertTrue(result.isEmpty());
        verify(mockStrategy, never()).search(anyString());
    }

    @Test
    public void testSetSearchStrategy() {
        // Arrange
        SearchStrategy newStrategy = mock(SearchStrategy.class);
        List<Book> expectedBooks = Collections.singletonList(mockBook1);
        String searchTerm = "Design Patterns";
        when(newStrategy.search(searchTerm)).thenReturn(expectedBooks);

        // Act
        bookSearcher.setSearchStrategy(newStrategy);
        List<Book> result = bookSearcher.search(searchTerm);

        // Assert
        assertEquals(expectedBooks, result);
        verify(newStrategy).search(searchTerm);
        verify(mockStrategy, never()).search(searchTerm);
    }

    @Test
    public void testConstructorWithStrategy() {
        // Arrange
        SearchStrategy initialStrategy = mock(SearchStrategy.class);
        String searchTerm = "Test Book";
        List<Book> expectedBooks = Collections.singletonList(mockBook2);
        when(initialStrategy.search(searchTerm)).thenReturn(expectedBooks);

        // Act
        BookSearcher newBookSearcher = new BookSearcher(initialStrategy);
        List<Book> result = newBookSearcher.search(searchTerm);

        // Assert
        assertEquals(expectedBooks, result);
        verify(initialStrategy).search(searchTerm);
    }
}