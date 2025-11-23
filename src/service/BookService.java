package service;

import dao.BookDAO;
import dao.MediaDAO;
import model.Book;

import java.util.List;

public class BookService {
    private final BookDAO bookDAO;
    private final MediaDAO mediaDAO;

    public BookService() {
        this.bookDAO = new BookDAO();
        this.mediaDAO = new MediaDAO();
        this.bookDAO.initializeTable();
        this.mediaDAO.initializeTable();
    }

    public Book addBook(String title, String author, String isbn) {
        // Validate input
        if (title == null || title.trim().isEmpty() ||
                author == null || author.trim().isEmpty() ||
                isbn == null || isbn.trim().isEmpty()) {
            System.err.println("Error: All fields must be non-empty");
            return null;
        }

        // Check if book with ISBN already exists
        Book existing = bookDAO.findByISBN(isbn);
        if (existing != null) {
            System.out.println("Book already exists (ISBN: " + isbn + ")");
            return null;
        }

        // Insert into media table first
        int mediaId = mediaDAO.insert(title.trim(), "book");
        if (mediaId == -1) {
            System.err.println("Error: Failed to create media record");
            return null;
        }

        // Insert into books table
        int bookId = bookDAO.insert(mediaId, author.trim(), isbn.trim());
        if (bookId != -1) {
            System.out.println("Book added successfully: " + title);
            return new Book(bookId, title, author, isbn, true);
        }

        return null;
    }

    public List<Book> getAllBooks() {
        return bookDAO.findAll();
    }

    public List<Book> searchBooks(String searchTerm, String searchType) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            System.out.println("Search term cannot be empty");
            return List.of();
        }

        switch (searchType.toLowerCase()) {
            case "title":
                return bookDAO.searchByTitle(searchTerm);
            case "author":
                return bookDAO.searchByAuthor(searchTerm);
            case "isbn":
                return bookDAO.searchByISBNPattern(searchTerm);
            default:
                System.out.println("Invalid search type. Using title search.");
                return bookDAO.searchByTitle(searchTerm);
        }
    }
}