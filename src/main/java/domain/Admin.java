package domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;

public class Admin extends User {

    // دالة للاتصال بالداتابيز (SQLite) - نسخة خاصة بـ Admin
    public static Connection connect() {
        String url = "jdbc:sqlite:database.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    // كتلة static لإنشاء جدول admins تلقائياً أول مرة
    static {
        Connection conn = connect();
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
    }

    // دالة لتوليد salt عشوائي
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // دالة لتشفير كلمة المرور باستخدام SHA-256 مع salt
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

    // تسجيل أدمن جديد
    public static Admin register(String username, String password) {
        Connection conn = connect();
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

    // constructor خاص
    public Admin(String username, String passwordHash, String salt) {
        super(username, passwordHash, salt);
        this.setLoggedIn(false);
    }

    // تسجيل الدخول للأدمن
    public static Admin login(String username, String password) {
        Connection conn = connect();
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

    // Add this method to the Admin class
    public static boolean unregisterUser(String username) {
        // First verify this is an admin operation
        // In a real application, you would check if the current user is an admin

        Connection conn = connect();
        if (conn == null) return false;

        try {
            // Check if user exists
            String checkUserSql = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkUserSql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("User not found: " + username);
                    return false;
                }

                int userId = rs.getInt("id");

                // Check for active loans
                String checkLoansSql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND returned = 0";
                try (PreparedStatement loansStmt = conn.prepareStatement(checkLoansSql)) {
                    loansStmt.setInt(1, userId);
                    ResultSet loansRs = loansStmt.executeQuery();

                    if (loansRs.next() && loansRs.getInt(1) > 0) {
                        System.out.println("Cannot unregister user with active loans: " + username);
                        return false;
                    }
                }

                // Check for unpaid fines
                String checkFinesSql = "SELECT total_fine FROM user_fines WHERE user_id = ?";
                try (PreparedStatement finesStmt = conn.prepareStatement(checkFinesSql)) {
                    finesStmt.setInt(1, userId);
                    ResultSet finesRs = finesStmt.executeQuery();

                    if (finesRs.next() && finesRs.getDouble("total_fine") > 0) {
                        System.out.println("Cannot unregister user with unpaid fines: " + username);
                        return false;
                    }
                }

                // If checks pass, delete the user
                String deleteUserSql = "DELETE FROM users WHERE id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteUserSql)) {
                    deleteStmt.setInt(1, userId);
                    int affectedRows = deleteStmt.executeUpdate();

                    if (affectedRows > 0) {
                        System.out.println("Successfully unregistered user: " + username);
                        return true;
                    } else {
                        System.out.println("Failed to unregister user: " + username);
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error unregistering user: " + e.getMessage());
            return false;
        }
    }
}