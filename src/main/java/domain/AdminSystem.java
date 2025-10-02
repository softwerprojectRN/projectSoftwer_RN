package domain;



import java.util.ArrayList;
import java.util.Scanner;

public class AdminSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);


        ArrayList<Admin> admins = new ArrayList<>();
        admins.add(new Admin("nour", "1234"));
        admins.add(new Admin("rahaf", "5678"));

        Admin currentAdmin = null;

        while(true) {
            System.out.println("\n1. Login\n2. Logout\n3. Manage Books\n4. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch(choice) {
                case 1:
                    if(currentAdmin != null && currentAdmin.isLoggedIn()) {
                        System.out.println("Already logged in as " + currentAdmin.getUsername());
                        break;
                    }

                    System.out.print("Enter username: ");
                    String user = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String pass = scanner.nextLine();

                    boolean loggedIn = false;
                    for(Admin admin : admins) {
                        if(admin.login(user, pass)) {
                            currentAdmin = admin;
                            loggedIn = true;
                            break;
                        }
                    }
                    if(!loggedIn) System.out.println("Error: Invalid username or password.");
                    break;

                case 2:
                    if(currentAdmin != null) {
                        currentAdmin.logout();
                        currentAdmin = null;
                    } else {
                        System.out.println("No admin is logged in.");
                    }
                    break;

                case 3:
                    if(currentAdmin != null) {
                        currentAdmin.manageBooks();
                    } else {
                        System.out.println("Error: You must login first to perform this action.");
                    }
                    break;

                case 4:
                    System.out.println("Exiting...");
                    System.exit(0);

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
