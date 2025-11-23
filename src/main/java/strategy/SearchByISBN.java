package strategy;

import dao.BookDAO;
import model.Book;
import java.util.List;

public class SearchByISBN implements SearchStrategy {
    private final BookDAO bookDAO;

    public SearchByISBN() {
        this.bookDAO = new BookDAO();
    }

    @Override
    public List<Book> search(String searchTerm) {
        return bookDAO.searchByISBNPattern(searchTerm);
    }
}