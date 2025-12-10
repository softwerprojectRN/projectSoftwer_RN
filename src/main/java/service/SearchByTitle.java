package service;

import dao.BookDAO;
import model.Book;
import java.util.List;

/**
 * A concrete implementation of the {@link SearchStrategy} interface that performs
 * book searches based on title matching.
 * <p>
 * This strategy delegates the search operation to the {@link BookDAO}, which
 * handles the actual database interaction and filtering logic.
 * </p>
 *
 * <p>
 * Using this strategy allows the system to support title-based searching without
 * altering the core search flow, following the Strategy Pattern.
 * </p>
 *
 * @version 1.1
 */

public class SearchByTitle implements SearchStrategy {

    /**
     * Data Access Object responsible for retrieving and querying book records
     * from the underlying data source.
     */
    private final BookDAO bookDAO;

    /**
     * Creates a new instance of the {@code SearchByTitle} strategy and initializes
     * its associated {@link BookDAO} for performing title-based lookups.
     */
    public SearchByTitle() {
        this.bookDAO = new BookDAO();
    }

    /**
     * Searches for books whose titles match the specified search term.
     *
     * @param searchTerm the title or partial title used to filter books;
     *                   must not be null or empty
     * @return a list of {@link Book} objects that match the given title,
     *         or an empty list if no results are found
     */

    @Override
    public List<Book> search(String searchTerm) {
        return bookDAO.searchByTitle(searchTerm);
    }
}