package domain;


public class Admin extends User {
    public Admin(String username, String password) {
        super(username, password);
    }

    @Override
    public String getUsername() {
        return super.getUsername();
    }

    @Override
    public String getPassword() {
        return super.getPassword();
    }

    // Add a new book ///////////////
    public void addBook(LibrarySystem library, String title, String author, String isbn) {
        Book newBook = new Book(title, author, isbn);
        library.addBook(newBook);
        System.out.println("Admin " + getUsername() + " added book: " + title);
    }

    public void showAdminInfo() {
        System.out.println("Admin username: " + getUsername());
    }
}
