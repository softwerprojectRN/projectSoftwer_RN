package strategy;

import dao.BookDAO;
import model.Book;
import java.util.List;

/**
 * Concrete search strategy for searching books by ISBN pattern.
 * Implements the {@link SearchStrategy} interface.
 *
 * Allows searching for books in the database based on ISBN matching.
 *
 * @author Library Management System
 * @version 1.0
 */
public class SearchByISBN implements SearchStrategy {

    /** DAO for accessing book data */
    private final BookDAO bookDAO;

    /**
     * Constructs a new {@code SearchByISBN} strategy.
     * Initializes the {@link BookDAO}.
     */
    public SearchByISBN() {
        this.bookDAO = new BookDAO();
    }

    /**
     * Searches for books using a pattern of ISBN numbers.
     *
     * @param searchTerm the ISBN pattern to search for
     * @return a list of {@link Book} objects matching the ISBN pattern
     */
    @Override
    public List<Book> search(String searchTerm) {
        return bookDAO.searchByISBNPattern(searchTerm);
    }
}