package domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;

public class User {

    private String username;
    private String passwordHash;  // سنحفظ الـ hash هون للكائن الحالي
    private String salt;          // الملح المستخدم للـ hashing
    private boolean loggedIn;

    // دالة للاتصال بالداتابيز (SQLite)
    private static Connection connect() {
        String url = "jdbc:sqlite:database.db";  // اسم ملف الداتابيز
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    // كتلة static لإنشاء الجدول تلقائياً أول مرة
    // أضفنا حقل salt للأمان
    static {
        Connection conn = connect();
        String sql = "CREATE TABLE IF NOT EXISTS users (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " username text NOT NULL UNIQUE,\n"
                + " password_hash text NOT NULL,\n"
                + " salt text NOT NULL\n"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("تم إنشاء الجدول بنجاح.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // دالة لتوليد salt عشوائي (للأمان)
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // دالة لتشفير كلمة المرور باستخدام SHA-256 مع salt
    // (الـ hashing غير قابل للفك، بس نقارن الـ hash الجديد مع المخزن)
    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("خطأ في الـ hashing: " + e.getMessage());
        }
    }

    // دالة static لتسجيل مستخدم جديد (Sign Up)
    // ترجع User object إذا نجح، أو null إذا كان المستخدم موجود
    public static User register(String username, String password) {
        // تحقق إذا المستخدم موجود بالفعل
        Connection conn = connect();
        String checkSql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("المستخدم موجود بالفعل: " + username);
                return null;  // فشل التسجيل
            }
        } catch (SQLException e) {
            System.out.println("خطأ في التحقق: " + e.getMessage());
            return null;
        }

        // توليد salt و hashing
        String salt = generateSalt();
        String passwordHash = hashPassword(password, salt);

        // حفظ في الداتابيز
        String sql = "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.setString(3, salt);
            pstmt.executeUpdate();
            System.out.println("تم تسجيل المستخدم بنجاح: " + username);
            // إرجاع كائن User جديد
            return new User(username, passwordHash, salt);
        } catch (SQLException e) {
            System.out.println("خطأ في التسجيل: " + e.getMessage());
            return null;
        }
    }

    // constructor خاص (private) للاستخدام الداخلي بعد الـ login
    protected User(String username, String passwordHash, String salt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.loggedIn = false;  // سيتم تحديثه في الـ login
    }

    public User(String username, String password) {
        this.username = username;
        this.passwordHash = password; // مؤقتًا، أو يمكنك عمل hash هنا
        this.salt = "";               // تركها فارغة أو توليد salt جديد
        this.loggedIn = false;
    }


    // دالة static لتسجيل الدخول (Login)
    // ترجع User object إذا نجح، أو null إذا فشل
    public static User login(String username, String password) {
        Connection conn = connect();
        String sql = "SELECT password_hash, salt FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");

                // hashing كلمة المرور المدخلة ومقارنتها
                String inputHash = hashPassword(password, salt);
                if (storedHash.equals(inputHash)) {
                    System.out.println("Login successful, welcome " + username + "!");
                    User user = new User(username, storedHash, salt);
                    user.loggedIn = true;
                    return user;
                } else {
                    System.out.println("Invalid username or password.");
                    return null;
                }
            } else {
                System.out.println("المستخدم غير موجود.");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("خطأ في الدخول: " + e.getMessage());
            return null;
        }
    }

    public String getUsername() {
        return username;
    }

    // لا نرجع كلمة المرور الأصلية، بس الـ hash إذا لزم
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void logout() {
        if (this.loggedIn) {
            this.loggedIn = false;
            System.out.println("Logged out successfully.");
        } else {
            System.out.println("You are not logged in yet.");
        }
    }

    protected void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
}