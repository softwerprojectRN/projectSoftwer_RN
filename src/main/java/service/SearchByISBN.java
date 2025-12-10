package service;

import dao.BookDAO;
import model.Book;
import java.util.List;

/**
 * A concrete implementation of the {@link SearchStrategy} interface that performs
 * book searches based on ISBN matching.
 * <p>
 * This strategy delegates the search operation to the {@link BookDAO}, which
 * executes the actual database queries to retrieve books whose ISBN contains
 * or matches the given pattern.
 * </p>
 *
 * <p>
 * This implementation follows the Strategy Pattern, enabling flexible and
 * interchangeable search behaviors within the library system.
 * </p>
 *
 * @version 1.1
 */

public class SearchByISBN implements SearchStrategy {

    /**
     * Data Access Object responsible for interacting with the book repository.
     * Handles ISBN-based lookup operations.
     */
    private final BookDAO bookDAO;

    /**
     * Creates a new instance of the {@code SearchByISBN} strategy and initializes
     * its {@link BookDAO} dependency for performing ISBN-based book lookups.
     */

    public SearchByISBN() {
        this.bookDAO = new BookDAO();
    }

    /**
     * Searches for books whose ISBN matches or contains the specified search pattern.
     *
     * @param searchTerm the ISBN or partial ISBN used to filter book records;
     *                   must not be null or empty
     * @return a list of {@link Book} objects whose ISBN matches the pattern,
     *         or an empty list if no books are found
     */

    @Override
    public List<Book> search(String searchTerm) {
        return bookDAO.searchByISBNPattern(searchTerm);
    }
}