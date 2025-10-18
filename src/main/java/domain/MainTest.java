package domain;
import domain.*;

import java.util.List;

public class MainTest {
    public static void main(String[] args) {
        System.out.println("=== اختبار نظام إدارة المكتبة ===\n");

        // اختبار إضافة الكتب
        testBookOperations();

        // اختبار تسجيل المستخدمين
        testUserRegistration();

        // اختبار تسجيل الدخول
        testUserLogin();

        // اختبار استعارة الكتب
        testBookBorrowing();

        // اختبار إرجاع الكتب
        testBookReturning();

        // اختبار تسجيل الأدمن
        testAdminRegistration();

        // اختبار إلغاء تسجيل المستخدم
        testUserUnregistration();

        System.out.println("\n=== انتهاء الاختبارات ===");
    }

    private static void testBookOperations() {
        System.out.println("--- اختبار عمليات الكتب ---");

        // إضافة كتب جديدة
        Book book1 = Book.addBook("Date Structure", "Sami Jo", "1234567890");
        if (book1 != null) {
            System.out.println("✓ تم إضافة الكتاب: " + book1.getTitle());
        }

        Book book2 = Book.addBook("Python Basics", "Jane Smith", "0987654321");
        if (book2 != null) {
            System.out.println("✓ تم إضافة الكتاب: " + book2.getTitle());
        }

        // محاولة إضافة كتاب بنفس ISBN
        Book duplicateBook = Book.addBook("Another Java Book", "Another Author", "1234567890");
        if (duplicateBook == null) {
            System.out.println("✓ تم رفض إضافة كتاب بنفس ISBN");
        }

        // استرجاع جميع الكتب
        List<Book> allBooks = Book.getAllBooks();
        System.out.println("✓ عدد الكتب في قاعدة البيانات: " + allBooks.size());
        for (Book book : allBooks) {
            System.out.println("  - " + book.toString());
        }
        System.out.println();
    }

    private static void testUserRegistration() {
        System.out.println("--- اختبار تسجيل المستخدمين ---");

        // تسجيل مستخدمين جدد
        User user1 = User.register("malek omar", "securepass");
        if (user1 != null) {
            System.out.println("✓ تم تسجيل المستخدم: " + user1.getUsername());
        }

        User user2 = User.register("sara osama", "securepass");
        if (user2 != null) {
            System.out.println("✓ تم تسجيل المستخدم: " + user2.getUsername());
        }

        // محاولة تسجيل مستخدم موجود مسبقاً
        User duplicateUser = User.register("malek omar", "newpassword");
        if (duplicateUser == null) {
            System.out.println("✓ تم رفض تسجيل مستخدم موجود مسبقاً");
        }
        System.out.println();
    }

    private static void testUserLogin() {
        System.out.println("--- اختبار تسجيل الدخول ---");

        // تسجيل دخول صحيح
        User user = User.login("malek omar", "securepass");
        if (user != null && user.isLoggedIn()) {
            System.out.println("✓ تم تسجيل دخول المستخدم بنجاح: " + user.getUsername());
        }

        // تسجيل دخول خاطئ
        User wrongUser = User.login("malek omar", "wrongpass");
        if (wrongUser == null) {
            System.out.println("✓ تم رفض تسجيل الدخول بكلمة مرور خاطئة");
        }

        // تسجيل دخول مستخدم غير موجود
        User nonExistentUser = User.login("nonexistent", "password");
        if (nonExistentUser == null) {
            System.out.println("✓ تم رفض تسجيل الدخول لمستخدم غير موجود");
        }
        System.out.println();
    }

    private static void testBookBorrowing() {
        System.out.println("--- اختبار استعارة الكتب ---");

        // تسجيل الدخول كمستخدم
        User user = User.login("malek omar", "securepass");
        if (user != null) {
            Borrower borrower = new Borrower(user.getUsername(), user.getPasswordHash(), "");
            borrower.setLoggedIn(true);

            // الحصول على أول كتاب متاح
            List<Book> allBooks = Book.getAllBooks();
            if (!allBooks.isEmpty()) {
                Book availableBook = null;
                for (Book book : allBooks) {
                    if (book.isAvailable()) {
                        availableBook = book;
                        break;
                    }
                }

                if (availableBook != null) {
                    // استعارة الكتاب
                    boolean borrowed = BorrowingService.borrowBook(borrower, availableBook);
                    if (borrowed) {
                        System.out.println("✓ تم استعارة الكتاب: " + availableBook.getTitle());

                        // محاولة استعارة نفس الكتاب مرة أخرى
                        boolean borrowedAgain = BorrowingService.borrowBook(borrower, availableBook);
                        if (!borrowedAgain) {
                            System.out.println("✓ تم رفض استعارة كتاب غير متاح");
                        }
                    }
                }
            }
        }
        System.out.println();
    }

    private static void testBookReturning() {
        System.out.println("--- اختبار إرجاع الكتب ---");

        // تسجيل الدخول كمستخدم
        User user = User.login("malek omar", "securepass");
        if (user != null) {
            Borrower borrower = new Borrower(user.getUsername(), user.getPasswordHash(), "");
            borrower.setLoggedIn(true);

            // الحصول على الكتب المستعارة
            List<Borrower.BookRecord> borrowedBooks = borrower.getBorrowedBooks();
            if (!borrowedBooks.isEmpty()) {
                Borrower.BookRecord record = borrowedBooks.get(0);
                Book book = record.getBook();

                // إرجاع الكتاب
                boolean returned = BorrowingService.returnBook(borrower, book);
                if (returned) {
                    System.out.println("✓ تم إرجاع الكتاب: " + book.getTitle());
                }
            } else {
                System.out.println("لا توجد كتب مستعارة حالياً");
            }
        }
        System.out.println();
    }

    private static void testAdminRegistration() {
        System.out.println("--- اختبار تسجيل الأدمن ---");

        // تسجيل أدمن جديد
        Admin admin = Admin.register("admin", "admin123");
        if (admin != null) {
            System.out.println("✓ تم تسجيل الأدمن: " + admin.getUsername());
        }

        // محاولة تسجيل أدمن بنفس الاسم
        Admin duplicateAdmin = Admin.register("admin", "newpass");
        if (duplicateAdmin == null) {
            System.out.println("✓ تم رفض تسجيل أدمن موجود مسبقاً");
        }

        // تسجيل دخول الأدمن
        Admin loggedInAdmin = Admin.login("admin", "admin123");
        if (loggedInAdmin != null && loggedInAdmin.isLoggedIn()) {
            System.out.println("✓ تم تسجيل دخول الأدمن بنجاح");
        }
        System.out.println();
    }

    private static void testUserUnregistration() {
        System.out.println("--- اختبار إلغاء تسجيل المستخدم ---");

        // تسجيل الدخول كأدمن
        Admin admin = Admin.login("admin", "admin123");
        if (admin != null) {
            // تسجيل مستخدم جديد لإلغاء تسجيله
            User newUser = User.register("testuser", "testpass");
            if (newUser != null) {
                System.out.println("✓ تم تسجيل مستخدم جديد للاختبار: " + newUser.getUsername());

                // محاولة إلغاء تسجيل المستخدم
                boolean unregistered = Admin.unregisterUser("testuser");
                if (unregistered) {
                    System.out.println("✓ تم إلغاء تسجيل المستخدم بنجاح");
                }
            }

            // محاولة إلغاء تسجيل مستخدم لديه كتب مستعارة
            User userWithBooks = User.login("malek omar", "securepass");
            if (userWithBooks != null) {
                Borrower borrower = new Borrower(userWithBooks.getUsername(), userWithBooks.getPasswordHash(), "");
                borrower.setLoggedIn(true);

                // استعارة كتاب
                List<Book> allBooks = Book.getAllBooks();
                if (!allBooks.isEmpty()) {
                    for (Book book : allBooks) {
                        if (book.isAvailable()) {
                            BorrowingService.borrowBook(borrower, book);
                            break;
                        }
                    }
                }

                // محاولة إلغاء تسجيل المستخدم الذي لديه كتب مستعارة
                boolean unregistered = Admin.unregisterUser("malek omar");
                if (!unregistered) {
                    System.out.println("✓ تم رفض إلغاء تسجيل مستخدم لديه كتب مستعارة");
                }
            }
        }
        System.out.println();
    }
}