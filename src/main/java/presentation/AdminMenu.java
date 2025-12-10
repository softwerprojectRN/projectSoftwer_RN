package presentation;

import model.Admin;
import service.AdminService;
import service.BookService;
import service.BorrowingService;
import service.CDService;

import java.util.Scanner;

/**
 * Provides a console-based admin interface for managing the library system.
 * Allows admins to log in, register, add books/CDs, send overdue reminders, and manage users.
 *
 * Handles input validation and communicates with {@link AdminService} and {@link BorrowingService}.
 *
 * Usage: Run the {@link #main(String[])} method to start the admin menu.
 *
 * @author Library
 * @version 1.1
 */

public class AdminMenu {

    private static Scanner scanner = new Scanner(System.in);

    /** Service for admin-related operations */
    private final AdminService adminService;

    /** Service for managing borrowings and overdue books */
    private final BorrowingService borrowingService;

    /** Currently logged-in admin */
    private Admin currentAdmin;

    /**
     * Constructs a new {@code AdminMenu} and initializes required services.
     */
    public AdminMenu() {
        this.adminService = new AdminService();
        this.borrowingService = new BorrowingService();
    }

    /**
     * Starts the admin menu system with login/registration and main operations.
     */
    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== Admin System ===");
            System.out.println("1. Login as Admin");
            System.out.println("2. Register New Admin");
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
                    loginAdmin();
                    if (currentAdmin != null) {
                        showMenu();
                    }
                    break;
                case 2:
                    registerAdmin();
                    break;
                case 3:
                    System.out.println("Exiting Admin System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    /**
     * Handles new admin registration with validation.
     */
    private void registerAdmin() {
        System.out.println("\n=== Register New Admin ===");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty!");
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (password.isEmpty()) {
            System.out.println("Password cannot be empty!");
            return;
        }

        System.out.print("Confirm Password: ");
        String confirmPassword = scanner.nextLine().trim();

        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match!");
            return;
        }

        Admin newAdmin = adminService.register(username, password);
        if (newAdmin != null) {
            System.out.println("Admin registered successfully! You can now login.");
        } else {
            System.out.println("Failed to register admin. Username might already exist.");
        }
    }

    /**
     * Handles admin login and sets the {@link #currentAdmin}.
     */
    private void loginAdmin() {
        System.out.println("\n=== Admin Login ===");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        currentAdmin = adminService.login(username, password);
        if (currentAdmin == null) {
            System.out.println("Invalid credentials!");
        }
    }

    /**
     * Displays the main admin menu after successful login.
     */
    private void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. Send Overdue Reminders");
            System.out.println("2. Unregister User");
            System.out.println("3. Add New Book");
            System.out.println("4. Add New CD");
            System.out.println("5. View All Users with Overdue Books");
            System.out.println("6. Logout");
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
                    sendOverdueReminders();
                    break;
                case 2:
                    unregisterUser();
                    break;
                case 3:
                    addNewBook();
                    break;
                case 4:
                    addNewCD();
                    break;
                case 5:
                    viewUsersWithOverdueBooks();
                    break;
                case 6:
                    currentAdmin.logout();
                    currentAdmin = null;
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    /**
     * Sends overdue book reminders to users.
     */
    private void sendOverdueReminders() {
        System.out.println("\nSending overdue reminders...");
        adminService.sendOverdueReminders(borrowingService);
        System.out.println("Reminders sent successfully!");
    }

    /**
     * Unregisters a user after confirmation.
     */
    private void unregisterUser() {
        System.out.print("\nEnter username to unregister: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty!");
            return;
        }

        System.out.print("Are you sure you want to unregister user '" + username + "'? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("yes")) {
            System.out.println("Operation cancelled.");
            return;
        }

        boolean success = adminService.unregisterUser(username);
        if (success) {
            System.out.println("User unregistered successfully!");
        } else {
            System.out.println("Failed to unregister user! User might not exist.");
        }
    }

    /**
     * Adds a new book to the system using {@link service.BookService}.
     */
    private void addNewBook() {
        System.out.println("\n=== Add New Book ===");

        System.out.print("Title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Author: ");
        String author = scanner.nextLine().trim();

        System.out.print("ISBN: ");
        String isbn = scanner.nextLine().trim();

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            System.out.println("All fields are required!");
            return;
        }

        BookService bookService = new BookService();
        model.Book book = bookService.addBook(title, author, isbn);

        if (book != null) {
            System.out.println("Book added successfully!");
            System.out.println(book);
        } else {
            System.out.println("Failed to add book!");
        }
    }

    /**
     * Adds a new CD to the system using {@link service.CDService}.
     */
    private void addNewCD() {
        System.out.println("\n=== Add New CD ===");

        System.out.print("Title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Artist: ");
        String artist = scanner.nextLine().trim();

        System.out.print("Genre: ");
        String genre = scanner.nextLine().trim();

        System.out.print("Duration (in minutes): ");
        String durationStr = scanner.nextLine().trim();

        if (title.isEmpty() || artist.isEmpty()) {
            System.out.println("Title and Artist are required!");
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationStr);
            if (duration <= 0) {
                System.out.println("Duration must be a positive number!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid duration format!");
            return;
        }

        CDService cdService = new CDService();
        model.CD cd = cdService.addCD(title, artist, genre, duration);

        if (cd != null) {
            System.out.println("CD added successfully!");
            System.out.println(cd);
        } else {
            System.out.println("Failed to add CD!");
        }
    }

    /**
     * Displays all users who currently have overdue books.
     */
    private void viewUsersWithOverdueBooks() {
        System.out.println("\n=== Users with Overdue Books ===");
        var usersWithOverdue = borrowingService.getUsersWithOverdueBooks();

        if (usersWithOverdue.isEmpty()) {
            System.out.println("No users have overdue books.");
        } else {
            System.out.println("Total users with overdue books: " + usersWithOverdue.size());
            System.out.println("-----------------------------------");
            for (var userInfo : usersWithOverdue) {
                System.out.printf("User ID: %d | Username: %s | Overdue Books: %d%n",
                        userInfo.getUserId(),
                        userInfo.getUsername(),
                        userInfo.getOverdueCount());
            }
            System.out.println("-----------------------------------");
        }
    }

    /**
     * Main entry point for the AdminMenu console application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        AdminMenu menu = new AdminMenu();
        menu.start();
    }
}