
package domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.List;

public class Admin extends User {
    //بدونهم بعمل ايرور
    private static EmailServer emailServer = new EmailServer();
    private static EmailNotifier emailNotifier = new EmailNotifier(emailServer);
//
    public static Connection connect() {
        String url = "jdbc:sqlite:database.db";
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    static {
        Connection conn = connect();
        if (conn != null) {
            String sql = "CREATE TABLE IF NOT EXISTS admins (\n"
                    + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                    + " username text NOT NULL UNIQUE,\n"
                    + " password_hash text NOT NULL,\n"
                    + " salt text NOT NULL\n"
                    + ");";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                System.out.println("تم إنشاء جدول admins بنجاح.");
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("لم يتم إنشاء جدول admins لأن الاتصال فشل.");
        }
    }

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("خطأ في الـ hashing: " + e.getMessage());
        }
    }

    public static Admin register(String username, String password) {
        Connection conn = connect();
        if (conn == null) return null; // تغطية فرع conn == null

        String checkSql = "SELECT * FROM admins WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("الأدمن موجود بالفعل: " + username);
                return null;
            }
        } catch (SQLException e) {
            System.out.println("خطأ في التحقق: " + e.getMessage());
            return null;
        }

        String salt = generateSalt();
        String passwordHash = hashPassword(password, salt);

        String sql = "INSERT INTO admins (username, password_hash, salt) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.setString(3, salt);
            pstmt.executeUpdate();
            System.out.println("تم تسجيل الأدمن بنجاح: " + username);
            return new Admin(username, passwordHash, salt);
        } catch (SQLException e) {
            System.out.println("خطأ في التسجيل: " + e.getMessage());
            return null;
        }
    }

    public Admin(String username, String passwordHash, String salt) {
        super(username, passwordHash, salt);
        this.setLoggedIn(false);
    }

    public static Admin login(String username, String password) {
        Connection conn = connect();
        if (conn == null) return null; // تغطية فرع conn == null

        String sql = "SELECT password_hash, salt FROM admins WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                String inputHash = hashPassword(password, salt);
                if (storedHash.equals(inputHash)) {
                    Admin admin = new Admin(username, storedHash, salt);
                    admin.setLoggedIn(true);
                    return admin;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    public void showAdminInfo() {
        System.out.println("Admin username: " + getUsername());
    }










    // Method to send reminder emails to users with overdue books


    // Method to unregister a user
    public static boolean unregisterUser(String username) {
        Connection conn = connect();
        if (conn == null) return false;

        try {
            // Start transaction
            conn.setAutoCommit(false);

            // First, check if user exists
            String checkSql = "SELECT id FROM users WHERE username = ?";
            int userId = -1;

            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setString(1, username);
                ResultSet rs = checkPstmt.executeQuery();

                if (rs.next()) {
                    userId = rs.getInt("id");
                } else {
                    System.out.println("User not found: " + username);
                    conn.rollback();
                    return false;
                }
            }

            // Delete borrow records for this user
            String deleteBorrowSql = "DELETE FROM borrow_records WHERE user_id = ?";
            try (PreparedStatement deleteBorrowPstmt = conn.prepareStatement(deleteBorrowSql)) {
                deleteBorrowPstmt.setInt(1, userId);
                deleteBorrowPstmt.executeUpdate();
            }

            // Delete user fines
            String deleteFinesSql = "DELETE FROM user_fines WHERE user_id = ?";
            try (PreparedStatement deleteFinesPstmt = conn.prepareStatement(deleteFinesSql)) {
                deleteFinesPstmt.setInt(1, userId);
                deleteFinesPstmt.executeUpdate();
            }

            // Delete the user
            String deleteUserSql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement deleteUserPstmt = conn.prepareStatement(deleteUserSql)) {
                deleteUserPstmt.setInt(1, userId);
                int affectedRows = deleteUserPstmt.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit();
                    System.out.println("User unregistered successfully: " + username);
                    return true;
                } else {
                    conn.rollback();
                    System.out.println("Failed to unregister user: " + username);
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.out.println("Error unregistering user: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    // Method to get the email server for testing purposes
    public static EmailServer getEmailServer() {
        return emailServer;
    }

    // Method to set a custom email server for testing
    public static void setEmailServer(EmailServer server) {
        emailServer = server;
        emailNotifier = new EmailNotifier(emailServer);
    }


    // Updated method to send reminder emails to users with overdue books
    public static void sendOverdueReminders() {
        List<Borrower.UserWithOverdueBooks> usersWithOverdueBooks = Borrower.getUsersWithOverdueBooks();

        for (Borrower.UserWithOverdueBooks userWithOverdueBooks : usersWithOverdueBooks) {
            String username = userWithOverdueBooks.getUsername();
            int overdueCount = userWithOverdueBooks.getOverdueCount();

            String message = "You have " + overdueCount + " overdue book(s).";

            // Create a user object for notification
            User user = new User(username, "", "");
            emailNotifier.notify(user, message);
        }
    }

    // ... (rest of the existing code)
}





