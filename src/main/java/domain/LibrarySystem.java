package domain;
//طبقة الخدمات (Service Layer)
//هذا هو "عقل" النظام.
// سيدير حالة تسجيل الدخول ويحتوي على منطق التحقق.
// سنستخدم Map لتخزين المسؤولين للبحث عنهم بسرعة.
import java.util.*;
import java.util.ArrayList;
import java.util.List;

public class LibrarySystem {

    private Map<String, Admin> admins = new HashMap<>();
    private List<Book> books;

    private Admin loggedInAdmin = null;

    public LibrarySystem() {
        // في البداية، نضيف مسؤول افتراضي للنظام.
        admins.put("rahaf", new Admin("rahaf", "1234"));
        admins.put("nour", new Admin("nour", "5678"));
        books = new ArrayList<>();

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



    /**
     * Gets the currently logged-in admin.
     * @return an Optional containing the admin if logged in, otherwise an empty Optional.
*/
    public Optional<Admin> getCurrentAdmin() {
    return Optional.ofNullable(this.loggedInAdmin);
    }
    /////////////////////////////////////////////////////////////////////////////
    ///


    public List<Book> searchBooks(String query) {
        List<Book> results = new ArrayList<>();
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(query) ||
                    book.getAuthor().equalsIgnoreCase(query) ||
                    book.getIsbn().equalsIgnoreCase(query)) {
                results.add(book);
            }
        }
        return results;
    }



    public void addBook(Book newBook) {
        books.add(newBook);
    }
}