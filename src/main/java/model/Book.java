package model;

public class Book extends Media {
    private String author;
    private String isbn;

    public Book(int id, String title, String author, String isbn, boolean isAvailable) {
        super(id, title, isAvailable, "book");
        this.author = author;
        this.isbn = isbn;
    }

    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }

    @Override
    public String toString() {
        return super.toString() + ", Author: '" + author + "', ISBN: " + isbn;
    }
}