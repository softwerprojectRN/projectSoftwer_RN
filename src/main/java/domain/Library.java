package domain;

import java.util.ArrayList;
import java.util.List;

public class Library {
    private List<Book> books; // Collection of books in the library

    public Library() {
        books = new ArrayList<>();
    }

    // Add a new book
    public void addBook(String title, String author, String isbn) {
        Book newBook = new Book(title, author, isbn);
        books.add(newBook);
        System.out.println("Book added: " + title);
    }

    public List<Book> searchBooks(String query) {
        List<Book> results = new ArrayList<>();
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(query) ||
                    book.getAuthor().equalsIgnoreCase(query) ||
                    book.getIsbn().equalsIgnoreCase(query)) {
                results.add(book);
            }
        }
        return results;
    }

    // List all available books
    public void listAvailableBooks() {
        for (Book book : books) {
            if (book.isAvailable()) {
                System.out.println(book.getTitle() + " by " + book.getAuthor());
            }
        }
    }

}
