package domain; // أو أي اسم حزمة تفضله

import java.util.List;
import java.util.Scanner;

/**
 * This is the main entry point for the Library Management System's console application.
 * It provides a command-line interface for users to interact with the system.
 */

//public class Main {
//
//    private static LibrarySystem library = new LibrarySystem();
//    private static Scanner scanner = new Scanner(System.in);
//    // We need a way to access the currently logged-in borrower for borrower-specific actions
//    private static Borrower currentBorrower = null;
//
//    public static void main(String[] args) {
//        runApplication();
//    }
//
//    /**
//     * The main application loop. It runs until the user chooses to exit.
//     */
//    private static void runApplication() {
//        while (true) {
//            if (library.isLoggedIn()) {
//                // An admin is logged in
//                showAdminMenu();
//            } else if (currentBorrower != null && currentBorrower.isLoggedIn()) {
//                // A borrower is logged in
//                showBorrowerMenu();
//            } else {
//                // No one is logged in
//                showMainMenu();
//            }
//        }
//    }
//
//    // ==================== MENUS ====================
//
//    private static void showMainMenu() {
//        System.out.println("\n--- Main Menu ---");
//        System.out.println("1. Admin Login");
//        System.out.println("2. Borrower Login");
//        System.out.println("3. Exit");
//        System.out.print("Enter your choice: ");
//
//        int choice = scanner.nextInt();
//        scanner.nextLine(); // Consume the rest of the line
//
//        switch (choice) {
//            case 1:
//                handleAdminLogin();
//                break;
//            case 2:
//                handleBorrowerLogin();
//                break;
//            case 3:
//                System.out.println("Exiting the system. Goodbye!");
//                scanner.close();
//                System.exit(0);
//                break;
//            default:
//                System.out.println("Invalid choice. Please try again.");
//        }
//    }
//
//    private static void showAdminMenu() {
//        System.out.println("\n--- Admin Menu ---");
//        System.out.println("1. Add a new book");
//        System.out.println("2. Search for a book");
//        System.out.println("3. Logout");
//        System.out.print("Enter your choice: ");
//
//        int choice = scanner.nextInt();
//        scanner.nextLine(); // Consume the rest of the line
//
//        switch (choice) {
//            case 1:
//                handleAddBook();
//                break;
//            case 2:
//                handleSearchBook();
//                break;
//            case 3:
//                handleLogout();
//                break;
//            default:
//                System.out.println("Invalid choice. Please try again.");
//        }
//    }
//
//    private static void showBorrowerMenu() {
//        System.out.println("\n--- Borrower Menu ---");
//        System.out.println("1. Search for a book");
//        System.out.println("2. Borrow a book");
//        System.out.println("3. Return a book");
//        System.out.println("4. View my borrowed books");
//        System.out.println("5. Pay fine");
//        System.out.println("6. Logout");
//        System.out.print("Enter your choice: ");
//
//        int choice = scanner.nextInt();
//        scanner.nextLine(); // Consume the rest of the line
//
//        switch (choice) {
//            case 1:
//                handleSearchBook();
//                break;
//            case 2:
//                handleBorrowBook();
//                break;
//            case 3:
//                handleReturnBook();
//                break;
//            case 4:
//                handleViewBorrowedBooks();
//                break;
//            case 5:
//                handlePayFine();
//                break;
//            case 6:
//                handleLogout();
//                break;
//            default:
//                System.out.println("Invalid choice. Please try again.");
//        }
//    }
//
//    // ==================== ACTION HANDLERS ====================
//
//    private static void handleAdminLogin() {
//        System.out.print("Enter username: ");
//        String username = scanner.nextLine();
//        System.out.print("Enter password: ");
//        String password = scanner.nextLine();
//        library.login(username, password);
//    }
//
//    private static void handleBorrowerLogin() {
//        // For simplicity, we'll create borrowers on the fly if they don't exist
//        System.out.print("Enter username: ");
//        String username = scanner.nextLine();
//        System.out.print("Enter password: ");
//        String password = scanner.nextLine();
//
//        // In a real system, you'd fetch this from a database.
//        // Here, we'll create a temporary borrower object for the session.
//        currentBorrower = new Borrower(username, password);
//        currentBorrower.login(username, password); // Use the User's login method
//    }
//
//    private static void handleLogout() {
//        if (library.isLoggedIn()) {
//            library.logout();
//        } else if (currentBorrower != null) {
//            currentBorrower.logout();
//            currentBorrower = null; // Clear the session
//        }
//    }
//
//    private static void handleAddBook() {
//        System.out.print("Enter book title: ");
//        String title = scanner.nextLine();
//        System.out.print("Enter book author: ");
//        String author = scanner.nextLine();
//        System.out.print("Enter book ISBN: ");
//        String isbn = scanner.nextLine();
//
//        library.getCurrentAdmin().ifPresent(admin -> admin.addBook(library, title, author, isbn));
//    }
//
//    private static void handleSearchBook() {
//        System.out.print("Enter search query (title, author, or ISBN): ");
//        String query = scanner.nextLine();
//        List<Book> results = library.searchBooks(query);
//
//        if (results.isEmpty()) {
//            System.out.println("No books found matching your query.");
//        } else {
//            System.out.println("--- Search Results ---");
//            for (Book book : results) {
//                System.out.println(book);
//            }
//        }
//    }
//
//    private static void handleBorrowBook() {
//        System.out.print("Enter the ISBN of the book to borrow: ");
//        String isbn = scanner.nextLine();
//        Book bookToBorrow = findBookByIsbn(isbn);
//
//        if (bookToBorrow != null) {
//            currentBorrower.borrowBook(bookToBorrow);
//        } else {
//            System.out.println("Book with ISBN " + isbn + " not found.");
//        }
//    }
//
//    private static void handleReturnBook() {
//        System.out.print("Enter the ISBN of the book to return: ");
//        String isbn = scanner.nextLine();
//        Book bookToReturn = findBookByIsbn(isbn);
//
//        if (bookToReturn != null) {
//            currentBorrower.returnBook(bookToReturn);
//        } else {
//            System.out.println("Book with ISBN " + isbn + " not found in the system.");
//        }
//    }
//
//    private static void handleViewBorrowedBooks() {
//        currentBorrower.showBorrowedBooks();
//    }
//
//    private static void handlePayFine() {
//        System.out.println("Your current fine balance is: " + currentBorrower.getFineBalance());
//        System.out.print("Enter amount to pay: ");
//        double amount = scanner.nextDouble();
//        scanner.nextLine(); // Consume the rest of the line
//        currentBorrower.payFine(amount);
//    }
//
//    // ==================== HELPER METHODS ====================
//
//    /**
//     * A helper method to find a book in the library by its ISBN.
//     * @param isbn The ISBN of the book to find.
//     * @return The Book object if found, otherwise null.
//     */
//    private static Book findBookByIsbn(String isbn) {
//        // This is a simple way to find a book. A more efficient way would be a Map in LibrarySystem.
//        for (Book book : library.searchBooks(isbn)) { // Search by ISBN to narrow it down
//            if (book.getIsbn().equalsIgnoreCase(isbn)) {
//                return book;
//            }
//        }
//        return null;
//    }
//}

