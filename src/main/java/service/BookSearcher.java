package service;

import model.Book;
import java.util.ArrayList;
import java.util.List;


/**
 * Context class for searching books using different {@link SearchStrategy} implementations.
 *
 * <p>This class allows switching the search strategy at runtime, following
 * the Strategy design pattern. Clients can perform searches without
 * worrying about the underlying search logic.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     BookSearcher searcher = new BookSearcher(new SearchByTitle());
 *     List&lt;Book&gt; results = searcher.search("Java");
 *     searcher.setSearchStrategy(new SearchByAuthor());
 *     results = searcher.search("Joshua Bloch");
 * </pre>
 *
 * @author Library
 * @version 1.1
 */
public class BookSearcher {

    /** Current search strategy used for book searching */
    private SearchStrategy searchStrategy;

    /**
     * Constructs a {@code BookSearcher} with the specified search strategy.
     *
     * @param searchStrategy the initial search strategy to use
     */
    public BookSearcher(SearchStrategy searchStrategy) {
        this.searchStrategy = searchStrategy;
    }

    /**
     * Updates the search strategy at runtime.
     *
     * @param searchStrategy the new search strategy to use
     */
    public void setSearchStrategy(SearchStrategy searchStrategy) {
        this.searchStrategy = searchStrategy;
    }

    /**
     * Searches for books using the current search strategy.
     *
     * @param searchTerm the search query; must not be null or empty
     * @return a list of {@link Book} objects matching the search term,
     *         or an empty list if no matches are found or the search term is invalid
     */
    public List<Book> search(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            System.out.println("Search term cannot be empty");
            return new ArrayList<>();
        }
        return searchStrategy.search(searchTerm.trim());
    }
}