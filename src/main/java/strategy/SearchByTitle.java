package strategy;

import dao.BookDAO;
import model.Book;
import java.util.List;

/**
 * Concrete search strategy for searching books by title.
 * Implements the {@link SearchStrategy} interface.
 *
 * Enables searching for books in the database based on the title field.
 *
 * @author Library Management System
 * @version 1.0
 */
public class SearchByTitle implements SearchStrategy {

    /** DAO for accessing book data */
    private final BookDAO bookDAO;

    /**
     * Constructs a new {@code SearchByTitle} strategy.
     * Initializes the {@link BookDAO}.
     */
    public SearchByTitle() {
        this.bookDAO = new BookDAO();
    }

    /**
     * Searches for books using the title provided.
     *
     * @param searchTerm the title to search for
     * @return a list of {@link Book} objects matching the title
     */
    @Override
    public List<Book> search(String searchTerm) {
        return bookDAO.searchByTitle(searchTerm);
    }
}