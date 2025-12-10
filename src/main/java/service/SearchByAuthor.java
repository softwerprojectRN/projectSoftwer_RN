package service;

import dao.BookDAO;
import model.Book;
import java.util.List;

/**
 * A concrete implementation of the {@link SearchStrategy} interface that performs
 * book searches based on author name matching.
 * <p>
 * This strategy utilizes the {@link BookDAO} to execute database queries and
 * retrieve books written by authors whose names match or contain the provided
 * search term.
 * </p>
 *
 * <p>
 * Following the Strategy Pattern, this implementation allows the library system
 * to support flexible and interchangeable searching behaviors without modifying
 * the core search logic.
 * </p>
 *
 * @version 1.1
 */

public class SearchByAuthor implements SearchStrategy {

    /**
     * Data Access Object responsible for interacting with stored book records.
     * Handles author-based lookup operations.
     */
    private final BookDAO bookDAO;

    /**
     * Creates a new instance of the {@code SearchByAuthor} strategy and initializes
     * its {@link BookDAO} dependency for performing author-based queries.
     */
    public SearchByAuthor() {
        this.bookDAO = new BookDAO();
    }

    /**
     * Searches for books whose author name matches or contains the specified term.
     *
     * @param searchTerm the full or partial author name used to filter book records;
     *                   must not be null or empty
     * @return a list of {@link Book} objects written by authors matching the term,
     *         or an empty list if no matching books are found
     */

    @Override
    public List<Book> search(String searchTerm) {
        return bookDAO.searchByAuthor(searchTerm);
    }
}