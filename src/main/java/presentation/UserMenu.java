package presentation;

import model.Book;
import model.Borrower;
import model.CD;
import model.Media;
import service.*;

import java.util.List;
import java.util.Scanner;

public class UserMenu {
    private static Scanner scanner = new Scanner(System.in);
    private final UserService userService;
    private final BookService bookService;
    private final CDService cdService;
    private final BorrowingService borrowingService;
    private final BorrowerService borrowerService;
    private Borrower currentUser;

    public UserMenu() {
        this.userService = new UserService();
        this.bookService = new BookService();
        this.cdService = new CDService();
        this.borrowingService = new BorrowingService();
        this.borrowerService = new BorrowerService();
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== Library System ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid option! Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    registerUser();
                    break;
                case 2:
                    loginUser();
                    if (currentUser != null) {
                        showMainMenu();
                    }
                    break;
                case 3:
                    System.out.println("Thank you for using the Library System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private void registerUser() {
        System.out.println("\n=== User Registration ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (userService.register(username, password) != null) {
            System.out.println("Registration successful! You can now login.");
        } else {
            System.out.println("Registration failed!");
        }
    }

    private void loginUser() {
        System.out.println("\n=== User Login ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        var user = userService.login(username, password);
        if (user != null) {
            // Convert User to Borrower
            currentUser = new Borrower(user.getId(), user.getUsername(),
                    user.getPasswordHash(), user.getSalt());
            currentUser.setLoggedIn(true);

            // Load borrower data
            borrowerService.loadBorrowerData(currentUser);

            System.out.println("Login successful! Welcome " + username + "!");
        } else {
            System.out.println("Login failed!");
        }
    }

    private void showMainMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. Browse Books");
            System.out.println("2. Browse CDs");
            System.out.println("3. Search Books");
            System.out.println("4. Search CDs");
            System.out.println("5. Borrow Media");
            System.out.println("6. Return Media");
            System.out.println("7. View My Borrowed Items");
            System.out.println("8. View Overdue Report");
            System.out.println("9. Pay Fine");
            System.out.println("10. View Fine Balance");
            System.out.println("11. Logout");
            System.out.print("Select an option: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid option! Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    browseBooks();
                    break;
                case 2:
                    browseCDs();
                    break;
                case 3:
                    searchBooks();
                    break;
                case 4:
                    searchCDs();
                    break;
                case 5:
                    borrowMedia();
                    break;
                case 6:
                    returnMedia();
                    break;
                case 7:
                    viewBorrowedItems();
                    break;
                case 8:
                    viewOverdueReport();
                    break;
                case 9:
                    payFine();
                    break;
                case 10:
                    viewFineBalance();
                    break;
                case 11:
                    currentUser.logout();
                    currentUser = null;
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private void browseBooks() {
        System.out.println("\n=== All Books ===");
        List<Book> books = bookService.getAllBooks();

        if (books.isEmpty()) {
            System.out.println("No books available in the library.");
        } else {
            for (Book book : books) {
                System.out.println(book);
                System.out.println("---");
            }
        }
    }

    private void browseCDs() {
        System.out.println("\n=== All CDs ===");
        List<CD> cds = cdService.getAllCDs();

        if (cds.isEmpty()) {
            System.out.println("No CDs available in the library.");
        } else {
            for (CD cd : cds) {
                System.out.println(cd);
                System.out.println("---");
            }
        }
    }

    private void searchBooks() {
        System.out.println("\n=== Search Books ===");
        System.out.println("Search by:");
        System.out.println("1. Title");
        System.out.println("2. Author");
        System.out.println("3. ISBN");
        System.out.print("Select search type: ");

        String typeInput = scanner.nextLine().trim();
        String searchType = "title";

        switch (typeInput) {
            case "1":
                searchType = "title";
                break;
            case "2":
                searchType = "author";
                break;
            case "3":
                searchType = "isbn";
                break;
            default:
                System.out.println("Invalid type, using title search.");
        }

        System.out.print("Enter search term: ");
        String searchTerm = scanner.nextLine();

        List<Book> results = bookService.searchBooks(searchTerm, searchType);

        if (results.isEmpty()) {
            System.out.println("No books found matching your search.");
        } else {
            System.out.println("\n=== Search Results ===");
            for (Book book : results) {
                System.out.println(book);
                System.out.println("---");
            }
        }
    }

    private void searchCDs() {
        System.out.println("\n=== Search CDs ===");
        System.out.println("Search by:");
        System.out.println("1. Title");
        System.out.println("2. Artist");
        System.out.println("3. Genre");
        System.out.print("Select search type: ");

        String typeInput = scanner.nextLine().trim();
        String searchType = "title";

        switch (typeInput) {
            case "1":
                searchType = "title";
                break;
            case "2":
                searchType = "artist";
                break;
            case "3":
                searchType = "genre";
                break;
            default:
                System.out.println("Invalid type, using title search.");
        }

        System.out.print("Enter search term: ");
        String searchTerm = scanner.nextLine();

        List<CD> results = cdService.searchCDs(searchTerm, searchType);

        if (results.isEmpty()) {
            System.out.println("No CDs found matching your search.");
        } else {
            System.out.println("\n=== Search Results ===");
            for (CD cd : results) {
                System.out.println(cd);
                System.out.println("---");
            }
        }
    }

    private void borrowMedia() {
        System.out.println("\n=== Borrow Media ===");
        System.out.println("1. Borrow Book");
        System.out.println("2. Borrow CD");
        System.out.print("Select media type: ");

        String typeInput = scanner.nextLine().trim();

        System.out.print("Enter media ID: ");
        String idInput = scanner.nextLine().trim();

        try {
            int mediaId = Integer.parseInt(idInput);
            Media media = null;

            if (typeInput.equals("1")) {
                // Find book by searching all books
                List<Book> allBooks = bookService.getAllBooks();
                for (Book book : allBooks) {
                    if (book.getId() == mediaId) {
                        media = book;
                        break;
                    }
                }
            } else if (typeInput.equals("2")) {
                media = cdService.findById(mediaId);
            } else {
                System.out.println("Invalid media type!");
                return;
            }

            if (media == null) {
                System.out.println("Media not found!");
                return;
            }

            if (borrowingService.borrowMedia(currentUser, media)) {
                // Reload borrower data
                borrowerService.loadBorrowerData(currentUser);
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format!");
        }
    }

    private void returnMedia() {
        System.out.println("\n=== Return Media ===");

        List<model.MediaRecord> borrowed = currentUser.getBorrowedMedia();
        if (borrowed.isEmpty()) {
            System.out.println("You have no borrowed items to return.");
            return;
        }

        System.out.println("Your borrowed items:");
        for (int i = 0; i < borrowed.size(); i++) {
            System.out.println((i + 1) + ". " + borrowed.get(i).getMedia());
        }

        System.out.print("Select item number to return: ");
        String input = scanner.nextLine().trim();

        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < borrowed.size()) {
                Media media = borrowed.get(index).getMedia();
                if (borrowingService.returnMedia(currentUser, media)) {
                    // Reload borrower data
                    borrowerService.loadBorrowerData(currentUser);
                }
            } else {
                System.out.println("Invalid selection!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input!");
        }
    }

    private void viewBorrowedItems() {
        borrowerService.displayBorrowedMedia(currentUser);
    }

    private void viewOverdueReport() {
        borrowerService.generateOverdueReport(currentUser);
    }

    private void payFine() {
        System.out.println("\n=== Pay Fine ===");
        System.out.printf("Current fine balance: %.2f\n", currentUser.getFineBalance());

        if (currentUser.getFineBalance() <= 0) {
            System.out.println("You have no fines to pay.");
            return;
        }

        System.out.print("Enter amount to pay: ");
        String input = scanner.nextLine().trim();

        try {
            double amount = Double.parseDouble(input);
            borrowerService.payFine(currentUser, amount);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount!");
        }
    }

    private void viewFineBalance() {
        System.out.println("\n=== Fine Balance ===");
        System.out.printf("Your current fine balance: %.2f\n", currentUser.getFineBalance());
    }

    public static void main(String[] args) {
        UserMenu menu = new UserMenu();
        menu.start();
    }
}