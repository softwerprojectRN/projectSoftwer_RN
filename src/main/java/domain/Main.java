package domain;//package domain;
//
//import java.util.List;
//import java.util.Scanner;
//
//public class Main {
//    private static Scanner scanner = new Scanner(System.in);
//    private static User currentUser = null;
//    private static Admin currentAdmin = null;
//
//    public static void main(String[] args) {
//        // Initialize database tables
//        initializeDatabase();
//
//        // Create a default admin if not exists
//        createDefaultAdmin();
//
//        boolean running = true;
//        while (running) {
//            if (currentUser != null) {
//                userMenu();
//            } else if (currentAdmin != null) {
//                adminMenu();
//            } else {
//                mainMenu();
//            }
//        }
//    }
//
//    // --- Helper Methods for Safe Input ---
//
//    private static int readInt(Scanner scanner, String prompt) {
//        while (true) {
//            System.out.print(prompt);
//            try {
//                return Integer.parseInt(scanner.nextLine());
//            } catch (NumberFormatException e) {
//                System.out.println("Invalid input. Please enter a whole number.");
//            }
//        }
//    }
//
//    private static double readDouble(Scanner scanner, String prompt) {
//        while (true) {
//            System.out.print(prompt);
//            try {
//                return Double.parseDouble(scanner.nextLine());
//            } catch (NumberFormatException e) {
//                System.out.println("Invalid input. Please enter a valid number.");
//            }
//        }
//    }
//
//    // --- End Helper Methods ---
//
//    private static void initializeDatabase() {
//        // Tables are created automatically in static blocks of each class
//        System.out.println("Database initialized successfully.");
//    }
//
//    private static void createDefaultAdmin() {
//        Admin admin = Admin.login("admin", "admin123");
//        if (admin == null) {
//            Admin.register("admin", "admin123");
//            System.out.println("Default admin created (username: admin, password: admin123)");
//        }
//    }
//
//    private static void mainMenu() {
//        System.out.println("\n=== Library Management System ===");
//        System.out.println("1. User Login");
//        System.out.println("2. User Registration");
//        System.out.println("3. Admin Login");
//        System.out.println("4. Exit");
//        int choice = readInt(scanner, "Select an option: ");
//
//        switch (choice) {
//            case 1:
//                userLogin();
//                break;
//            case 2:
//                userRegistration();
//                break;
//            case 3:
//                adminLogin();
//                break;
//            case 4:
//                System.out.println("Exiting system...");
//                System.exit(0);
//                break;
//            default:
//                System.out.println("Invalid option!");
//        }
//    }
//
//    private static void userLogin() {
//        System.out.println("\n=== User Login ===");
//        System.out.print("Username: ");
//        String username = scanner.nextLine();
//        System.out.print("Password: ");
//        String password = scanner.nextLine();
//
//        User user = User.login(username, password);
//        if (user != null) {
//            currentUser = new Borrower(username, user.getPasswordHash(), user.getPasswordHash());
//            currentUser.setLoggedIn(true);
//            System.out.println("Login successful! Welcome, " + username);
//        } else {
//            System.out.println("Invalid username or password!");
//        }
//    }
//
//    private static void userRegistration() {
//        System.out.println("\n=== User Registration ===");
//        System.out.print("Username: ");
//        String username = scanner.nextLine();
//        System.out.print("Password: ");
//        String password = scanner.nextLine();
//
//        User user = User.register(username, password);
//        if (user != null) {
//            System.out.println("Registration successful! You can now login.");
//        } else {
//            System.out.println("Registration failed! Username might already exist.");
//        }
//    }
//
//    private static void adminLogin() {
//        System.out.println("\n=== Admin Login ===");
//        System.out.print("Username: ");
//        String username = scanner.nextLine();
//        System.out.print("Password: ");
//        String password = scanner.nextLine();
//
//        Admin admin = Admin.login(username, password);
//        if (admin != null) {
//            currentAdmin = admin;
//            currentAdmin.setLoggedIn(true);
//            System.out.println("Admin login successful! Welcome, " + username);
//        } else {
//            System.out.println("Invalid admin credentials!");
//        }
//    }
//
//    private static void userMenu() {
//        System.out.println("\n=== User Menu ===");
//        System.out.println("1. View Available Books");
//        System.out.println("2. View Available CDs");
//        System.out.println("3. Borrow Media");
//        System.out.println("4. Return Media");
//        System.out.println("5. View Borrowed Media");
//        System.out.println("6. View Overdue Report");
//        System.out.println("7. Pay Fine");
//        System.out.println("8. Logout");
//        int choice = readInt(scanner, "Select an option: ");
//
//        switch (choice) {
//            case 1:
//                viewAvailableBooks();
//                break;
//            case 2:
//                viewAvailableCDs();
//                break;
//            case 3:
//                borrowMedia();
//                break;
//            case 4:
//                returnMedia();
//                break;
//            case 5:
//                viewBorrowedMedia();
//                break;
//            case 6:
//                viewOverdueReport();
//                break;
//            case 7:
//                payFine();
//                break;
//            case 8:
//                currentUser.logout();
//                currentUser = null;
//                break;
//            default:
//                System.out.println("Invalid option!");
//        }
//    }
//
//    private static void viewAvailableBooks() {
//        System.out.println("\n=== Available Books ===");
//        List<Book> books = Book.getAllBooks();
//        for (Book book : books) {
//            if (book.isAvailable()) {
//                System.out.println(book);
//            }
//        }
//    }
//
//    private static void viewAvailableCDs() {
//        System.out.println("\n=== Available CDs ===");
//        List<CD> cds = CD.getAllCDs();
//        for (CD cd : cds) {
//            if (cd.isAvailable()) {
//                System.out.println(cd);
//            }
//        }
//    }
//
//    private static void borrowMedia() {
//        System.out.println("\n=== Borrow Media ===");
//        int mediaType = readInt(scanner, "1. Borrow Book\n2. Borrow CD\nSelect media type: ");
//        int mediaId = readInt(scanner, "Enter media ID: ");
//
//        Media media = null;
//        if (mediaType == 1) {
//            List<Book> books = Book.getAllBooks();
//            for (Book book : books) {
//                if (book.getId() == mediaId) {
//                    media = book;
//                    break;
//                }
//            }
//        } else if (mediaType == 2) {
//            List<CD> cds = CD.getAllCDs();
//            for (CD cd : cds) {
//                if (cd.getId() == mediaId) {
//                    media = cd;
//                    break;
//                }
//            }
//        }
//
//        if (media != null) {
//            Borrower borrower = (Borrower) currentUser;
//            BorrowingService.borrowMedia(borrower, media);
//        } else {
//            System.out.println("Media not found!");
//        }
//    }
//
//    private static void returnMedia() {
//        System.out.println("\n=== Return Media ===");
//        int mediaId = readInt(scanner, "Enter media ID: ");
//
//        Media media = null;
//        List<Book> books = Book.getAllBooks();
//        for (Book book : books) {
//            if (book.getId() == mediaId) {
//                media = book;
//                break;
//            }
//        }
//
//        if (media == null) {
//            List<CD> cds = CD.getAllCDs();
//            for (CD cd : cds) {
//                if (cd.getId() == mediaId) {
//                    media = cd;
//                    break;
//                }
//            }
//        }
//
//        if (media != null) {
//            Borrower borrower = (Borrower) currentUser;
//            BorrowingService.returnMedia(borrower, media);
//        } else {
//            System.out.println("Media not found!");
//        }
//    }
//
//    private static void viewBorrowedMedia() {
//        System.out.println("\n=== Borrowed Media ===");
//        Borrower borrower = (Borrower) currentUser;
//        List<Borrower.MediaRecord> borrowedMedia = borrower.getBorrowedMedia();
//
//        if (borrowedMedia.isEmpty()) {
//            System.out.println("You haven't borrowed any media.");
//        } else {
//            for (Borrower.MediaRecord record : borrowedMedia) {
//                Media media = record.getMedia();
//                System.out.println(media + ", Due Date: " + record.getDueDate());
//            }
//        }
//    }
//
//    private static void viewOverdueReport() {
//        System.out.println("\n=== Overdue Report ===");
//        Borrower borrower = (Borrower) currentUser;
//        borrower.generateOverdueReport();
//    }
//
//    private static void payFine() {
//        System.out.println("\n=== Pay Fine ===");
//        Borrower borrower = (Borrower) currentUser;
//        System.out.println("Current fine balance: " + borrower.getFineBalance());
//        double amount = readDouble(scanner, "Enter amount to pay: ");
//        borrower.payFine(amount);
//    }
//
//    private static void adminMenu() {
//        System.out.println("\n=== Admin Menu ===");
//        System.out.println("1. Add Book");
//        System.out.println("2. Add CD");
//        System.out.println("3. View All Books");
//        System.out.println("4. View All CDs");
//        System.out.println("5. Send Overdue Reminders");
//        System.out.println("6. Unregister User");
//        System.out.println("7. View Users with Overdue Books");
//        System.out.println("8. Logout");
//        int choice = readInt(scanner, "Select an option: ");
//
//        switch (choice) {
//            case 1:
//                addBook();
//                break;
//            case 2:
//                addCD();
//                break;
//            case 3:
//                viewAllBooks();
//                break;
//            case 4:
//                viewAllCDs();
//                break;
//            case 5:
//                sendOverdueReminders();
//                break;
//            case 6:
//                unregisterUser();
//                break;
//            case 7:
//                viewUsersWithOverdueBooks();
//                break;
//            case 8:
//                currentAdmin.logout();
//                currentAdmin = null;
//                break;
//            default:
//                System.out.println("Invalid option!");
//        }
//    }
//
//    private static void addBook() {
//        System.out.println("\n=== Add Book ===");
//        System.out.print("Title: ");
//        String title = scanner.nextLine();
//        System.out.print("Author: ");
//        String author = scanner.nextLine();
//        System.out.print("ISBN: ");
//        String isbn = scanner.nextLine();
//
//        Book book = Book.addBook(title, author, isbn);
//        if (book != null) {
//            System.out.println("Book added successfully!");
//        } else {
//            System.out.println("Failed to add book!");
//        }
//    }
//
//    private static void addCD() {
//        System.out.println("\n=== Add CD ===");
//        System.out.print("Title: ");
//        String title = scanner.nextLine();
//        System.out.print("Artist: ");
//        String artist = scanner.nextLine();
//        System.out.print("Genre: ");
//        String genre = scanner.nextLine();
//        int duration = readInt(scanner, "Duration (minutes): ");
//
//        CD cd = CD.addCD(title, artist, genre, duration);
//        if (cd != null) {
//            System.out.println("CD added successfully!");
//        } else {
//            System.out.println("Failed to add CD!");
//        }
//    }
//
//    private static void viewAllBooks() {
//        System.out.println("\n=== All Books ===");
//        List<Book> books = Book.getAllBooks();
//        for (Book book : books) {
//            System.out.println(book);
//        }
//    }
//
//    private static void viewAllCDs() {
//        System.out.println("\n=== All CDs ===");
//        List<CD> cds = CD.getAllCDs();
//        for (CD cd : cds) {
//            System.out.println(cd);
//        }
//    }
//
//    private static void sendOverdueReminders() {
//        System.out.println("\n=== Send Overdue Reminders ===");
//        Admin.sendOverdueReminders();
//        System.out.println("Reminders sent successfully!");
//
//        List<EmailServer.Email> sentEmails = Admin.getEmailServer().getSentEmails();
//        System.out.println("Sent " + sentEmails.size() + " email(s):");
//        for (EmailServer.Email email : sentEmails) {
//            System.out.println("To: " + email.getTo());
//            System.out.println("Subject: " + email.getSubject());
//            System.out.println("Body: " + email.getBody());
//            System.out.println("---");
//        }
//    }
//
//    private static void unregisterUser() {
//        System.out.println("\n=== Unregister User ===");
//        System.out.print("Enter username to unregister: ");
//        String username = scanner.nextLine();
//
//        boolean success = Admin.unregisterUser(username);
//        if (success) {
//            System.out.println("User unregistered successfully!");
//        } else {
//            System.out.println("Failed to unregister user!");
//        }
//    }
//
//    private static void viewUsersWithOverdueBooks() {
//        System.out.println("\n=== Users with Overdue Books ===");
//        List<Borrower.UserWithOverdueBooks> usersWithOverdueBooks = Borrower.getUsersWithOverdueBooks();
//
//        if (usersWithOverdueBooks.isEmpty()) {
//            System.out.println("No users with overdue books.");
//        } else {
//            for (Borrower.UserWithOverdueBooks user : usersWithOverdueBooks) {
//                System.out.println("Username: " + user.getUsername() +
//                        ", Overdue Books: " + user.getOverdueCount());
//            }
//        }
//    }
//}


