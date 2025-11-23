package presentation;

import model.Admin;
import service.AdminService;
import service.BorrowingService;

import java.util.Scanner;

public class AdminMenu {
    private static Scanner scanner = new Scanner(System.in);
    private final AdminService adminService;
    private final BorrowingService borrowingService;
    private Admin currentAdmin;

    public AdminMenu() {
        this.adminService = new AdminService();
        this.borrowingService = new BorrowingService();
    }

    public void start() {
        // Admin login
        System.out.println("=== Admin Login ===");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        currentAdmin = adminService.login(username, password);
        if (currentAdmin == null) {
            System.out.println("Invalid credentials!");
            return;
        }

        showMenu();
    }

    private void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. Send Overdue Reminders");
            System.out.println("2. Unregister User");
            System.out.println("3. Logout");
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
                    currentAdmin.logout();
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private void sendOverdueReminders() {
        System.out.println("\nSending overdue reminders...");
        adminService.sendOverdueReminders(borrowingService);
        System.out.println("Reminders sent successfully!");
    }

    private void unregisterUser() {
        System.out.print("\nEnter username to unregister: ");
        String username = scanner.nextLine();

        boolean success = adminService.unregisterUser(username);
        if (success) {
            System.out.println("User unregistered successfully!");
        } else {
            System.out.println("Failed to unregister user!");
        }
    }

    public static void main(String[] args) {
        AdminMenu menu = new AdminMenu();
        menu.start();
    }
}