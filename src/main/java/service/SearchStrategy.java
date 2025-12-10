package service;

import model.Book;
import java.util.List;

/**
 * Represents a strategy interface for performing book search operations.
 * <p>
 * This interface defines the contract for implementing different search
 * mechanisms within the library system. Each concrete strategy
 * (e.g., {@link SearchByTitle}, {@link SearchByAuthor}, {@link SearchByISBN})
 * provides its own logic for locating books based on a specific criterion.
 * </p>
 *
 * <p>
 * The Strategy Pattern is used here to allow flexible and interchangeable
 * search behaviors without modifying the core system logic.
 * </p>
 *
 * @author Library
 * @version 1.1
 */

public interface SearchStrategy {

    /**
     * Executes a search operation using the implemented search strategy.
     *
     * @param searchTerm the input query used to match books according to the strategy's criteria;
     *                   must not be null or empty
     * @return a list of matching {@link Book} objects; returns an empty list if no matches are found
     */

    List<Book> search(String searchTerm);
}