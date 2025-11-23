package strategy;

import model.Book;

import java.util.List;

public interface SearchStrategy {
    List<Book> search(String searchTerm);
}