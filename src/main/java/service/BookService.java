package service;

import dao.BookDAO;
import dao.MediaDAO;
import model.Book;

import java.util.List;

/**
 * Service class responsible for book-related operations such as adding books,
 * retrieving all books, and searching books by title, author, or ISBN.
 *
 * @author Library Management System
 * @version 1.0
 */
public class BookService {

    /** DAO used for interacting with the books table. */
    private final BookDAO bookDAO;

    /** DAO used for interacting with the media table. */
    private final MediaDAO mediaDAO;

    /**
     * Constructs a BookService instance and initializes required database tables.
     */
    public BookService() {
        this.bookDAO = new BookDAO();
        this.mediaDAO = new MediaDAO();
        this.bookDAO.initializeTable();
        this.mediaDAO.initializeTable();
    }

    /**
     * Adds a new book to the system.
     *
     * @param title  The title of the book.
     * @param author The author of the book.
     * @param isbn   The ISBN of the book.
     * @return A Book object representing the newly added book,
     *         or null if the operation fails.
     */
    public Book addBook(String title, String author, String isbn) {
        if (title == null || title.trim().isEmpty() ||
                author == null || author.trim().isEmpty() ||
                isbn == null || isbn.trim().isEmpty()) {
            System.err.println("Error: All fields must be non-empty");
            return null;
        }

        Book existing = bookDAO.findByISBN(isbn);
        if (existing != null) {
            System.out.println("Book already exists (ISBN: " + isbn + ")");
            return null;
        }

        int mediaId = mediaDAO.insert(title.trim(), "book");
        if (mediaId == -1) {
            System.err.println("Error: Failed to create media record");
            return null;
        }

        int bookId = bookDAO.insert(mediaId, author.trim(), isbn.trim());
        if (bookId != -1) {
            System.out.println("Book added successfully: " + title);
            return new Book(bookId, title, author, isbn, true);
        }

        return null;
    }

    /**
     * Retrieves all books stored in the system.
     *
     * @return A list of all Book objects.
     */
    public List<Book> getAllBooks() {
        return bookDAO.findAll();
    }

    /**
     * Searches for books based on a provided search term and search type.
     *
     * @param searchTerm The keyword to search for.
     * @param searchType The type of search: "title", "author", or "isbn".
     * @return A list of books matching the search criteria.
     */
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
