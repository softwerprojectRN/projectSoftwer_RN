package domain;
//طبقة الخدمات (Service Layer)
//هذا هو "عقل" النظام.
// سيدير حالة تسجيل الدخول ويحتوي على منطق التحقق.
// سنستخدم Map لتخزين المسؤولين للبحث عنهم بسرعة.
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LibrarySystem {

    private Map<String, Admin> admins = new HashMap<>();

    private Admin loggedInAdmin = null;

    public LibrarySystem() {
        // في البداية، نضيف مسؤول افتراضي للنظام.
        admins.put("admin", new Admin("rahaf", "1234"));
        admins.put("admin", new Admin("nour", "5678"));

    }


    public boolean login(String username, String password) {
        // البحث عن المسؤول في قائمة المسؤولين
        Admin admin = admins.get(username);

        // التحقق من وجود المسؤول وصحة كلمة المرور
        if (admin != null && admin.getPassword().equals(password)) {
            this.loggedInAdmin = admin; // حفظ حالة تسجيل الدخول
            System.out.println("Login successful for user: " + username);
            return true;
        }

        System.out.println("Login failed. Invalid username or password.");
        return false;
    }


    public void logout() {
        if (isLoggedIn()) {
            System.out.println("Logging out user: " + loggedInAdmin.getUsername());
            this.loggedInAdmin = null; // إنهاء الجلسة
        } else {
            System.out.println("No user is currently logged in.");
        }
    }


    public boolean isLoggedIn() {
        return this.loggedInAdmin != null;
    }


}