//
//package domain;
//import java.util.List;
//import java.util.Scanner;
//
//public class Main {
//    private static Scanner scanner = new Scanner(System.in);
//    private static User currentUser = null;
//    private static Admin currentAdmin = null;
//
//    public static void main(String[] args) {
//        // Initialize the system with some sample data
//        initializeSystem();
//
//        // Main menu loop
//        while (true) {
//            System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
//            if (currentAdmin != null) {
//                System.out.println("Logged in as Admin: " + currentAdmin.getUsername());
//                System.out.println("1. Add Book");
//                System.out.println("2. Search Book");
//                System.out.println("3. Send Overdue Reminders");
//                System.out.println("4. Unregister User");
//                System.out.println("5. Logout");
//            } else if (currentUser != null) {
//                System.out.println("Logged in as User: " + currentUser.getUsername());
//                System.out.println("1. Search Book");
//                System.out.println("2. View Available Books");
//                System.out.println("3. Borrow Book");
//                System.out.println("4. Return Book");
//                System.out.println("5. Pay Fine");
//                System.out.println("6. View My Borrowed Books");
//                System.out.println("7. Logout");
//            } else {
//                System.out.println("1. Admin Login");
//                System.out.println("2. User Login");
//                System.out.println("3. Register New User");
//                System.out.println("4. Exit");
//            }
//
//            System.out.print("Enter your choice: ");
//            int choice = scanner.nextInt();
//            scanner.nextLine(); // Consume newline
//
//            if (currentAdmin != null) {
//                handleAdminMenu(choice);
//            } else if (currentUser != null) {
//                handleUserMenu(choice);
//            } else {
//                handleMainMenu(choice);
//            }
//        }
//    }
//
//    private static void initializeSystem() {
//        // Create an admin if not exists
//        Admin admin = Admin.register("admin", "admin123");
//        if (admin == null) {
//            System.out.println("Admin already exists or registration failed");
//        }
//
//        // Add some sample books
//        Book.addBook("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565");
//        Book.addBook("To Kill a Mockingbird", "Harper Lee", "9780061120084");
//        Book.addBook("1984", "George Orwell", "9780451524935");
//
//        // Create a sample user
//        User user = User.register("user1", "password123");
//        if (user == null) {
//            System.out.println("User already exists or registration failed");
//        }
//    }
//
//    private static void handleMainMenu(int choice) {
//        switch (choice) {
//            case 1:
//                adminLogin();
//                break;
//            case 2:
//                userLogin();
//                break;
//            case 3:
//                registerUser();
//                break;
//            case 4:
//                System.out.println("Exiting the system. Goodbye!");
//                System.exit(0);
//                break;
//            default:
//                System.out.println("Invalid choice. Please try again.");
//        }
//    }
//
//    private static void handleAdminMenu(int choice) {
//        switch (choice) {
//            case 1:
//                addBook();
//                break;
//            case 2:
//                searchBook();
//                break;
//            case 3:
//                sendOverdueReminders();
//                break;
//            case 4:
//                unregisterUser();
//                break;
//            case 5:
//                adminLogout();
//                break;
//            default:
//                System.out.println("Invalid choice. Please try again.");
//        }
//    }
//
//    private static void handleUserMenu(int choice) {
//        switch (choice) {
//            case 1:
//                searchBook();
//                break;
//            case 2:
//                viewAvailableBooks();
//                break;
//            case 3:
//                borrowBook();
//                break;
//            case 4:
//                returnBook();
//                break;
//            case 5:
//                payFine();
//                break;
//            case 6:
//                viewBorrowedBooks();
//                break;
//            case 7:
//                userLogout();
//                break;
//            default:
//                System.out.println("Invalid choice. Please try again.");
//        }
//    }
//
//    // US1.1 Admin login
//    private static void adminLogin() {
//        System.out.print("Enter username: ");
//        String username = scanner.nextLine();
//        System.out.print("Enter password: ");
//        String password = scanner.nextLine();
//
//        Admin admin = Admin.login(username, password);
//        if (admin != null) {
//            currentAdmin = admin;
//            System.out.println("Admin login successful!");
//        } else {
//            System.out.println("Invalid credentials. Please try again.");
//        }
//    }
//
//    // US1.2 Admin logout
//    private static void adminLogout() {
//        if (currentAdmin != null) {
//            currentAdmin.logout();
//            currentAdmin = null;
//            System.out.println("Admin logged out successfully.");
//        }
//    }
//
//    // User login
//    // User login
//    private static void userLogin() {
//        System.out.print("Enter username: ");
//        String username = scanner.nextLine();
//        System.out.print("Enter password: ");
//        String password = scanner.nextLine();
//
//        User user = User.login(username, password);
//        if (user != null) {
//            currentUser = user;
//            // Ensure the logged-in status is set
//            currentUser.setLoggedIn(true);
//            System.out.println("User login successful!");
//        } else {
//            System.out.println("Invalid credentials. Please try again.");
//        }
//    }
//
//    // User logout
//    private static void userLogout() {
//        if (currentUser != null) {
//            currentUser.logout();
//            currentUser = null;
//            System.out.println("User logged out successfully.");
//        }
//    }
//
//    // Register new user
//    private static void registerUser() {
//        System.out.print("Enter username: ");
//        String username = scanner.nextLine();
//        System.out.print("Enter password: ");
//        String password = scanner.nextLine();
//
//        User user = User.register(username, password);
//        if (user != null) {
//            System.out.println("User registered successfully!");
//        } else {
//            System.out.println("Registration failed. Username might already exist.");
//        }
//    }
//
//    // US1.3 Add book
//    private static void addBook() {
//        System.out.print("Enter book title: ");
//        String title = scanner.nextLine();
//        System.out.print("Enter book author: ");
//        String author = scanner.nextLine();
//        System.out.print("Enter book ISBN: ");
//        String isbn = scanner.nextLine();
//
//        Book book = Book.addBook(title, author, isbn);
//        if (book != null) {
//            System.out.println("Book added successfully!");
//            System.out.println(book.toString());
//        } else {
//            System.out.println("Failed to add book. ISBN might already exist.");
//        }
//    }
//
//    // US1.4 Search book
//    private static void searchBook() {
//        System.out.print("Enter search term (title/author/ISBN): ");
//        String searchTerm = scanner.nextLine().toLowerCase();
//
//        List<Book> allBooks = Book.getAllBooks();
//        boolean found = false;
//
//        System.out.println("\nSearch Results:");
//        System.out.println("----------------------------------------");
//        for (Book book : allBooks) {
//            if (book.getTitle().toLowerCase().contains(searchTerm) ||
//                    book.getAuthor().toLowerCase().contains(searchTerm) ||
//                    book.getIsbn().toLowerCase().contains(searchTerm)) {
//                System.out.println(book.toString());
//                found = true;
//            }
//        }
//
//        if (!found) {
//            System.out.println("No books found matching your search.");
//        }
//    }
//
//    // View available books
//    private static void viewAvailableBooks() {
//        List<Book> allBooks = Book.getAllBooks();
//        boolean found = false;
//
//        System.out.println("\nAvailable Books:");
//        System.out.println("----------------------------------------");
//        for (Book book : allBooks) {
//            if (book.isAvailable()) {
//                System.out.println(book.toString());
//                found = true;
//            }
//        }
//
//        if (!found) {
//            System.out.println("No available books at the moment.");
//        }
//    }
//
//    // US2.1 Borrow book
//
//    private static void borrowBook() {
//        if (!(currentUser instanceof Borrower)) {
//            // Create a Borrower object from the current user
//            currentUser = new Borrower(currentUser.getUsername(), currentUser.getPasswordHash(), "");
//            // Set the logged in status
//            currentUser.setLoggedIn(true);
//        }
//
//        Borrower borrower = (Borrower) currentUser;
//
//        // Check if user has already borrowed the maximum number of books
//        if (borrower.getBorrowedBooks().size() >= BorrowingService.getMaxBorrowedBooks()) {
//            System.out.println("You have already borrowed the maximum number of books (" +
//                    BorrowingService.getMaxBorrowedBooks() + ").");
//            System.out.println("Please return a book before borrowing another one.");
//            return;
//        }
//
//        // Check if user has outstanding fines
//        if (borrower.getFineBalance() > 0) {
//            System.out.println("You have outstanding fines of $" + borrower.getFineBalance());
//            System.out.println("Please pay your fines before borrowing a book.");
//            return;
//        }
//
//        // Display available books
//        viewAvailableBooks();
//
//        System.out.print("\nEnter book ISBN to borrow: ");
//        String isbn = scanner.nextLine();
//
//        List<Book> allBooks = Book.getAllBooks();
//        Book bookToBorrow = null;
//
//        for (Book book : allBooks) {
//            if (book.getIsbn().equals(isbn)) {
//                bookToBorrow = book;
//                break;
//            }
//        }
//
//        if (bookToBorrow != null) {
//            if (!bookToBorrow.isAvailable()) {
//                System.out.println("This book is not available for borrowing.");
//                return;
//            }
//
//            // Display book details
//            System.out.println("\nBook Details:");
//            System.out.println("Title: " + bookToBorrow.getTitle());
//            System.out.println("Author: " + bookToBorrow.getAuthor());
//            System.out.println("ISBN: " + bookToBorrow.getIsbn());
//            System.out.println("Borrow Period: " + BorrowingService.getBorrowDays() + " days");
//            System.out.println("Fine per day: $" + BorrowingService.getFinePerDay());
//
//            System.out.print("\nConfirm borrowing this book? (Y/N): ");
//            String confirm = scanner.nextLine();
//
//            if (confirm.equalsIgnoreCase("Y")) {
//                boolean success = BorrowingService.borrowBook(borrower, bookToBorrow);
//                if (success) {
//                    System.out.println("Book borrowed successfully!");
//                    System.out.println("Due date: " + java.time.LocalDate.now().plusDays(BorrowingService.getBorrowDays()));
//                } else {
//                    System.out.println("Failed to borrow book.");
//                }
//            } else {
//                System.out.println("Borrowing cancelled.");
//            }
//        } else {
//            System.out.println("Book not found.");
//        }
//    }
//
//    // Return book
//
//    private static void returnBook() {
//        if (!(currentUser instanceof Borrower)) {
//            // Create a Borrower object from the current user
//            currentUser = new Borrower(currentUser.getUsername(), currentUser.getPasswordHash(), "");
//            // Set the logged in status
//            currentUser.setLoggedIn(true);
//        }
//
//        Borrower borrower = (Borrower) currentUser;
//
//        // Display borrowed books
//        List<Borrower.BookRecord> borrowedBooks = borrower.getBorrowedBooks();
//        if (borrowedBooks.isEmpty()) {
//            System.out.println("You have no borrowed books to return.");
//            return;
//        }
//
//        System.out.println("\nYour Borrowed Books:");
//        System.out.println("----------------------------------------");
//        for (int i = 0; i < borrowedBooks.size(); i++) {
//            Borrower.BookRecord record = borrowedBooks.get(i);
//            System.out.println((i + 1) + ". " + record.getBook().getTitle());
//            System.out.println("   ISBN: " + record.getBook().getIsbn());
//            System.out.println("   Due Date: " + record.getDueDate());
//            System.out.println("   Status: " + (record.isOverdue() ? "OVERDUE" : "On Time"));
//            if (record.isOverdue()) {
//                System.out.println("   Overdue Days: " + record.getOverdueDays());
//                System.out.println("   Fine: $" + (record.getOverdueDays() * BorrowingService.getFinePerDay()));
//            }
//            System.out.println("----------------------------------------");
//        }
//
//        System.out.print("\nEnter the ISBN of the book to return: ");
//        String isbn = scanner.nextLine();
//
//        List<Book> allBooks = Book.getAllBooks();
//        Book bookToReturn = null;
//
//        for (Book book : allBooks) {
//            if (book.getIsbn().equals(isbn)) {
//                bookToReturn = book;
//                break;
//            }
//        }
//
//        if (bookToReturn != null) {
//            boolean success = BorrowingService.returnBook(borrower, bookToReturn);
//            if (success) {
//                System.out.println("Book returned successfully!");
//            } else {
//                System.out.println("Failed to return book.");
//            }
//        } else {
//            System.out.println("Book not found or not borrowed by you.");
//        }
//    }
//
//    // US2.2 Overdue book detection
//    // US2.2 Overdue book detection
//    private static void viewBorrowedBooks() {
//        if (!(currentUser instanceof Borrower)) {
//            // Create a Borrower object from the current user
//            currentUser = new Borrower(currentUser.getUsername(), currentUser.getPasswordHash(), "");
//            // Set the logged in status
//            currentUser.setLoggedIn(true);
//        }
//
//        Borrower borrower = (Borrower) currentUser;
//
//        System.out.println("\nYour Borrowed Books:");
//        System.out.println("----------------------------------------");
//
//        List<Borrower.BookRecord> borrowedBooks = borrower.getBorrowedBooks();
//        if (borrowedBooks.isEmpty()) {
//            System.out.println("You have no borrowed books.");
//        } else {
//            for (Borrower.BookRecord record : borrowedBooks) {
//                System.out.println("Book: " + record.getBook().getTitle());
//                System.out.println("ISBN: " + record.getBook().getIsbn());
//                System.out.println("Due Date: " + record.getDueDate());
//                System.out.println("Status: " + (record.isOverdue() ? "OVERDUE" : "On Time"));
//                if (record.isOverdue()) {
//                    System.out.println("Overdue Days: " + record.getOverdueDays());
//                    System.out.println("Fine: $" + (record.getOverdueDays() * BorrowingService.getFinePerDay()));
//                }
//                System.out.println("----------------------------------------");
//            }
//        }
//
//        System.out.println("Your Fine Balance: $" + borrower.getFineBalance());
//    }
//    // US2.3 Pay fine
//    // US2.3 Pay fine
//    private static void payFine() {
//        if (!(currentUser instanceof Borrower)) {
//            // Create a Borrower object from the current user
//            currentUser = new Borrower(currentUser.getUsername(), currentUser.getPasswordHash(), "");
//            // Set the logged in status
//            currentUser.setLoggedIn(true);
//        }
//
//        Borrower borrower = (Borrower) currentUser;
//
//        System.out.println("Your current fine balance: $" + borrower.getFineBalance());
//
//        if (borrower.getFineBalance() > 0) {
//            System.out.print("Enter amount to pay: ");
//            double amount = scanner.nextDouble();
//            scanner.nextLine(); // Consume newline
//
//            boolean success = borrower.payFine(amount);
//            if (success) {
//                System.out.println("Payment successful!");
//                System.out.println("Your new fine balance: $" + borrower.getFineBalance());
//            } else {
//                System.out.println("Payment failed. Please check the amount and try again.");
//            }
//        } else {
//            System.out.println("You have no fines to pay.");
//        }
//    }
//
//    // US3.1 Send reminder
//    private static void sendOverdueReminders() {
//        Admin.sendOverdueReminders();
//        System.out.println("Overdue reminders sent successfully!");
//
//        // Display sent emails for verification
//        List<EmailServer.Email> sentEmails = Admin.getEmailServer().getSentEmails();
//        if (!sentEmails.isEmpty()) {
//            System.out.println("\nSent Emails:");
//            System.out.println("----------------------------------------");
//            for (EmailServer.Email email : sentEmails) {
//                System.out.println("To: " + email.getTo());
//                System.out.println("Subject: " + email.getSubject());
//                System.out.println("Body: " + email.getBody());
//                System.out.println("----------------------------------------");
//            }
//        } else {
//            System.out.println("No overdue books found. No emails sent.");
//        }
//    }
//
//    // US4.2 Unregister user
//    private static void unregisterUser() {
//        System.out.print("Enter username of user to unregister: ");
//        String username = scanner.nextLine();
//
//        boolean success = Admin.unregisterUser(username);
//        if (success) {
//            System.out.println("User unregistered successfully!");
//        } else {
//            System.out.println("Failed to unregister user. User might not exist.");
//        }
//    }
//}