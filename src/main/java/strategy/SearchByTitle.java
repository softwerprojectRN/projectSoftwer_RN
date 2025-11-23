package strategy;

import dao.BookDAO;
import model.Book;
import java.util.List;

public class SearchByTitle implements SearchStrategy {
    private final BookDAO bookDAO;

    public SearchByTitle() {
        this.bookDAO = new BookDAO();
    }

    @Override
    public List<Book> search(String searchTerm) {
        return bookDAO.searchByTitle(searchTerm);
    }
}