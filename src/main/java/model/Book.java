package model;

/**
 * Represents a book in the library system.
 * Extends Media with book-specific attributes.
 *
 * @author Library Management System
 * @version 1.0
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

    /**
     * Returns a string representation of the book.
     *
     * @return formatted string with book details
     */
    @Override
    public String toString() {
        return super.toString() + ", Author: '" + author + "', ISBN: " + isbn;
    }


}