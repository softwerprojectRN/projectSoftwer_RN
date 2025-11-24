package strategy;

import dao.BookDAO;
import model.Book;
import java.util.List;

/**
 * Concrete search strategy for searching books by author name.
 * Implements the {@link SearchStrategy} interface.
 *
 * Provides a way to search the book database based on the author field.
 *
 * @author Library Management System
 * @version 1.0
 */
public class SearchByAuthor implements SearchStrategy {

    /** DAO for accessing book data */
    private final BookDAO bookDAO;

    /**
     * Constructs a new {@code SearchByAuthor} strategy.
     * Initializes the {@link BookDAO}.
     */
    public SearchByAuthor() {
        this.bookDAO = new BookDAO();
    }

    /**
     * Searches for books by author using the provided search term.
     *
     * @param searchTerm the author name to search for
     * @return a list of {@link Book} objects matching the author
     */
    @Override
    public List<Book> search(String searchTerm) {
        return bookDAO.searchByAuthor(searchTerm);
    }
}
