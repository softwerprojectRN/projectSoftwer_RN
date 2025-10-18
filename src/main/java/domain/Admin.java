package domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;

public class Admin extends User {

    // دالة للاتصال بالداتابيز (SQLite) - نسخة خاصة بـ Admin
    public static Connection connect() {
        String url = "jdbc:sqlite:database.db";  // نفس ملف الداتابيز
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

    // دالة لتوليد salt عشوائي (للأمان) - نسخة خاصة بـ Admin
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // دالة لتشفير كلمة المرور باستخدام SHA-256 مع salt - نسخة خاصة بـ Admin
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

    // دالة static لتسجيل أدمن جديد (Sign Up للأدمن)
    // ترجع Admin object إذا نجح، أو null إذا كان الأدمن موجود
    public static Admin register(String username, String password) {
        // تحقق إذا الأدمن موجود بالفعل
        Connection conn = connect();
        String checkSql = "SELECT * FROM admins WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("الأدمن موجود بالفعل: " + username);
                return null;  // فشل التسجيل
            }
        } catch (SQLException e) {
            System.out.println("خطأ في التحقق: " + e.getMessage());
            return null;
        }

        // توليد salt و hashing
        String salt = generateSalt();
        String passwordHash = hashPassword(password, salt);

        // حفظ في الداتابيز (جدول admins)
        String sql = "INSERT INTO admins (username, password_hash, salt) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.setString(3, salt);
            pstmt.executeUpdate();
            System.out.println("تم تسجيل الأدمن بنجاح: " + username);
            // إرجاع كائن Admin جديد
            return new Admin(username, passwordHash, salt);
        } catch (SQLException e) {
            System.out.println("خطأ في التسجيل: " + e.getMessage());
            return null;
        }
    }

    // constructor خاص (protected) للاستخدام الداخلي بعد الـ login
    protected Admin(String username, String passwordHash, String salt) {
        super(username, passwordHash, salt);
        this.setLoggedIn(false);  // سيتم تحديثه في الـ login
    }

    // دالة static لتسجيل الدخول (Login للأدمن)
    // ترجع Admin object إذا نجح، أو null إذا فشل
    public static Admin login(String username, String password) {
        Connection conn = connect();
        String sql = "SELECT password_hash, salt FROM admins WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");

                // hashing كلمة المرور المدخلة ومقارنتها
                String inputHash = hashPassword(password, salt);
                if (storedHash.equals(inputHash)) {
                    System.out.println("Admin login successful, welcome " + username + "!");
                    Admin admin = new Admin(username, storedHash, salt);
                    admin.setLoggedIn(true);
                    return admin;
                } else {
                    System.out.println("Invalid username or password for admin.");
                    return null;
                }
            } else {
                System.out.println("الأدمن غير موجود.");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("خطأ في الدخول: " + e.getMessage());
            return null;
        }
    }

//    // Add a new book //
//    public void addBook(LibrarySystem library, String title, String author, String isbn) {
//        Book newBook = new Book(title, author, isbn);
//        library.addBook(newBook);
//        System.out.println("Admin " + getUsername() + " added book: " + title);
//    }

    public void showAdminInfo() {
        System.out.println("Admin username: " + getUsername());
    }
}