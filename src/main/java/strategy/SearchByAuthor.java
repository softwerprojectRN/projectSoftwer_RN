package strategy;

import dao.BookDAO;
import model.Book;
import java.util.List;

public class SearchByAuthor implements SearchStrategy {
    private final BookDAO bookDAO;

    public SearchByAuthor() {
        this.bookDAO = new BookDAO();
    }

    @Override
    public List<Book> search(String searchTerm) {
        return bookDAO.searchByAuthor(searchTerm);
    }
}