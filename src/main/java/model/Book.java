package model;

/**
 * Represents a book in the library system.
 *
 * <p>Extends {@link Media} to include book-specific attributes such as
 * author and ISBN.</p>
 *
 * <p>Provides methods to access book information and a string representation
 * suitable for display.</p>
 *
 * @author Library
 * @version 1.1
 */
public class Book extends Media {
    /** The author of the book */
    private String author;

    /** The ISBN of the book */
    private String isbn;

    /**
     * Constructs a Book object.
     *
     * @param id the book's unique identifier
     * @param title the book's title
     * @param author the book's author
     * @param isbn the book's ISBN
     * @param isAvailable the availability status
     */
    public Book(int id, String title, String author, String isbn, boolean isAvailable) {
        super(id, title, isAvailable, "book");
        this.author = author;
        this.isbn = isbn;
    }

    /**
     * Gets the author of the book.
     *
     * @return the author's name
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Gets the ISBN of the book.
     *
     * @return the ISBN
     */
    public String getIsbn() {
        return isbn;
    }

    /** Returns a formatted string representation of the book. */

    @Override
    public String toString() {
        return super.toString() + ", Author: '" + author + "', ISBN: " + isbn;
    }


}