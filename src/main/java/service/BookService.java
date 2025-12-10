package service;

import dao.BookDAO;
import dao.MediaDAO;
import model.Book;

import java.util.List;

/**
 * Service class responsible for managing book-related operations, including:
 * <ul>
 *     <li>Adding new books</li>
 *     <li>Retrieving all books</li>
 *     <li>Searching for books by title, author, or ISBN</li>
 * </ul>
 *
 * <p>This service interacts with {@link BookDAO} and {@link MediaDAO} to perform
 * database operations and maintain media/book consistency.</p>
 *
 * @version 1.1
 */
public class BookService {

    /** DAO used for interacting with the books table. */
    private final BookDAO bookDAO;

    /** DAO used for interacting with the media table. */
    private final MediaDAO mediaDAO;

    /**
     * Constructs a {@code BookService} instance and initializes required database tables.
     */
    public BookService() {
        this.bookDAO = new BookDAO();
        this.mediaDAO = new MediaDAO();
        this.bookDAO.initializeTable();
        this.mediaDAO.initializeTable();
    }

    /**
     * Adds a new book to the system after validating input fields.
     *
     * @param title  The book's title; must be non-null and non-empty
     * @param author The book's author; must be non-null and non-empty
     * @param isbn   The book's ISBN; must be non-null, non-empty, and unique
     * @return A {@link Book} object representing the newly added book,
     *         or {@code null} if the operation fails or the book already exists
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
     * @return A list containing all {@link Book} objects.
     */

    public List<Book> getAllBooks() {
        return bookDAO.findAll();
    }

    /**
     * Searches for books based on a given search term and type.
     *
     * @param searchTerm The keyword to search for; must not be null or empty
     * @param searchType The type of search: "title", "author", or "isbn"
     * @return A list of {@link Book} objects matching the search criteria,
     *         or an empty list if no matches are found
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