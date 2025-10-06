package domain;

public class Book {
    String title;
    String author;
    String isbn;
    boolean isAvailable;

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.isAvailable = true;
    }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }


    /**
     * Provides a user-friendly string representation of the book.
     * @return A string with book details.
     */
    @Override
    public String toString() {
        return "Title: '" + title + "', Author: '" + author + "', ISBN: " + isbn + ", Available: " + (isAvailable ? "Yes" : "No");
    }


}
