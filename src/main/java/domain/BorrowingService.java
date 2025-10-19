package domain;

import java.time.LocalDate;

public class BorrowingService {

    private static final int BORROW_DAYS = 28;        // 28 يوم للاستحقاق
    private static final double FINE_PER_DAY = 1.0;   // 1 دولار لكل يوم تأخير

    // دالة رئيسية للاستعارة (مع كل الشيكات والمنطق)
    public static boolean borrowBook(Borrower borrower, Book book) {
        // الشيكات الأساسية
        if (!borrower.isLoggedIn()) {
            System.out.println("يجب تسجيل الدخول أولاً.");
            return false;
        }
        if (borrower.getFineBalance() > 0) {
            System.out.println("ادفع غرامتك (" + borrower.getFineBalance() + ") أولاً.");
            return false;
        }
        // Add this check for overdue books
        if (!borrower.getOverdueBooks().isEmpty()) {
            System.out.println("يجب إرجاع الكتب المتأخرة أولاً.");
            return false;
        }
        if (!book.isAvailable()) {
            System.out.println("الكتاب '" + book.getTitle() + "' غير متاح.");
            return false;
        }

        // تنفيذ الاستعارة
        book.borrow();  // تحديث حالة الكتاب
        LocalDate dueDate = LocalDate.now().plusDays(BORROW_DAYS);
        borrower.addBorrowRecord(book, dueDate);  // حفظ السجل في Borrower
        System.out.println("تم استعارة الكتاب '" + book.getTitle() + "'. تاريخ الاستحقاق: " + dueDate);
        return true;
    }

    // دالة للإرجاع (مع حساب الغرامة)
    public static boolean returnBook(Borrower borrower, Book book) {
        for (Borrower.BookRecord record : borrower.getBorrowedBooks()) {
            if (record.getBook().getIsbn().equals(book.getIsbn())) {
                book.returnBook();  // تحديث حالة الكتاب
                double bookFine = 0.0;
                if (record.isOverdue()) {
                    long overdueDays = record.getOverdueDays();
                    bookFine = overdueDays * FINE_PER_DAY;
                    System.out.println("الكتاب متأخر بـ " + overdueDays + " أيام. غرامة الكتاب: " + bookFine);
                }
                borrower.removeBorrowRecord(record, bookFine);  // إزالة السجل وإضافة الغرامة
                System.out.println("تم إرجاع الكتاب '" + book.getTitle() + "' بنجاح. الرصيد الكلي: " + borrower.getFineBalance());
                return true;
            }
        }
        System.out.println("هذا الكتاب غير مستعار منك.");
        return false;
    }

    // getters للثوابت (لو عايز تغيّرها)
    public static int getBorrowDays() { return BORROW_DAYS; }
    public static double getFinePerDay() { return FINE_PER_DAY;}
}

