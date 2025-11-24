package strategy;

import model.Book;
import java.util.List;

/**
 * Interface for book search strategies.
 * Defines the contract for implementing various search methods.
 *
 * Different concrete strategies (e.g., {@link SearchByTitle}, {@link SearchByAuthor}, {@link SearchByISBN})
 * implement this interface to provide customized search behavior.
 *
 * @author Library Management System
 * @version 1.0
 */
public interface SearchStrategy {

    /**
     * Searches for books based on a search term.
     *
     * @param searchTerm the query term used for searching
     * @return a list of {@link Book} objects that match the search term
     */
    List<Book> search(String searchTerm);
}