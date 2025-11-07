package domain;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
   private static Scanner scanner = new Scanner(System.in);
   private static Admin currentAdmin;

   public static void main(String[] args) {
       // Admin login
       System.out.println("=== Admin Login ===");
       System.out.print("Username: ");
       String username = scanner.nextLine();
       System.out.print("Password: ");
       String password = scanner.nextLine();

       currentAdmin = Admin.login(username, password);
       if (currentAdmin == null) {
           System.out.println("Invalid credentials!");
           return;
       }

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
//عندما يستدعي الأدمن الدالة Admin.sendOverdueReminders().
//نقوم بتنفيذ استعلام (query) على قاعدة البيانات لجلب كل المستخدمين الذين لديهم سجلات استعارة (borrow_records) حيث:
//الكتاب لم يُرجع بعد (returned = 0).
//تاريخ الإرجاع (due_date) قد انتهى (أصبح في الماضي).

   private static void sendOverdueReminders() {
       System.out.println("\nSending overdue reminders...");
       
       // Check if email server is available
       EmailServer emailServer = Admin.getEmailServer();
       if (emailServer == null) {
           System.out.println("Error: Email server is not available. Cannot send reminders.");
           return;
       }
       
       // Get count of emails before sending
       int emailsBefore = emailServer.getSentEmails().size();
       
       // Send reminders
       try {
           Admin.sendOverdueReminders();
           System.out.println("Reminders sent successfully!");
           
           // Display sent emails for verification
           List<EmailServer.Email> sentEmails = emailServer.getSentEmails();
           int newEmails = sentEmails.size() - emailsBefore;
           
           if (newEmails > 0) {
               System.out.println("\nSent " + newEmails + " email(s):");
               // Display only the newly sent emails
               for (int i = emailsBefore; i < sentEmails.size(); i++) {
                   EmailServer.Email email = sentEmails.get(i);
                   System.out.println("To: " + email.getTo());
                   System.out.println("Subject: " + email.getSubject());
                   System.out.println("Body: " + email.getBody());
                   System.out.println("---");
               }
           } else {
               System.out.println("No overdue reminders to send.");
           }
       } catch (Exception e) {
           System.out.println("Error sending reminders: " + e.getMessage());
           e.printStackTrace();
       }
   }

   private static void unregisterUser() {
       System.out.print("\nEnter username to unregister: ");
       String username = scanner.nextLine();

       boolean success = Admin.unregisterUser(username);
       if (success) {
           System.out.println("User unregistered successfully!");
       } else {
           System.out.println("Failed to unregister user!");
       }
   }
}