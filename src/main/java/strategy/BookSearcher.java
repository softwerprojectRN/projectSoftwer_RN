package strategy;

import model.Book;
import strategy.*;
import java.util.ArrayList;
import java.util.List;


public class BookSearcher {
    private SearchStrategy searchStrategy;

    public BookSearcher(SearchStrategy searchStrategy) {
        this.searchStrategy = searchStrategy;
    }

    public void setSearchStrategy(SearchStrategy searchStrategy) {
        this.searchStrategy = searchStrategy;
    }

    public List<Book> search(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            System.out.println("لا يمكن البحث عن نص فارغ");
            return new ArrayList<>();
        }
        return searchStrategy.search(searchTerm.trim());
    }
}