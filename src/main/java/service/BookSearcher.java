package service;

import model.Book;
import java.util.ArrayList;
import java.util.List;

/**
 * Context class for searching books using different search strategies.
 * Supports changing the strategy at runtime.
 *
 * @author Library Management System
 * @version 1.0
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
     * @param searchTerm the search query (cannot be empty)
     * @return a list of {@link Book} objects that match the search term
     */
    public List<Book> search(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            System.out.println("Search term cannot be empty");
            return new ArrayList<>();
        }
        return searchStrategy.search(searchTerm.trim());
    }
}