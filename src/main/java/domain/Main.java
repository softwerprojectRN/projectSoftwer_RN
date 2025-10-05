package domain;

public class Main {
    public static void main(String[] args) {
        // 1. إنشاء نسخة من نظام المكتبة
        LibrarySystem system = new LibrarySystem();

        System.out.println("--- Testing Login Scenarios ---");

        // 2. محاولة تسجيل الدخول بكلمة مرور خاطئة (US1.1 - Failure Case)
        System.out.println("\nAttempting login with wrong password...");
        system.login("admin", "wrongpass");
        System.out.println("Is anyone logged in? " + system.isLoggedIn());

        // 3. محاولة تسجيل الدخول ببيانات صحيحة (US1.1 - Success Case)
        System.out.println("\nAttempting login with correct credentials...");
        system.login("rahaf", "1234");
        System.out.println("Is anyone logged in? " + system.isLoggedIn());

        // 4. محاولة تسجيل الخروج (US1.2)
        System.out.println("\nAttempting to logout...");
        system.logout();
        System.out.println("Is anyone logged in? " + system.isLoggedIn());

        // 5. محاولة تسجيل الخروج مرة أخرى (لا يوجد مستخدم مسجل)
        System.out.println("\nAttempting to logout again...");
        system.logout();
    }
}