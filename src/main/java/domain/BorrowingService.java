package domain;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class BorrowingService {

    // تعريف فترات الاستعارة والغرامات لكل نوع من المواد
    private static final Map<String, Integer> BORROW_DAYS = new HashMap<>();
    private static final Map<String, Double> FINE_PER_DAY = new HashMap<>();

    static {
        // تهيئة القيم للكتب
        BORROW_DAYS.put("book", 28);        // 28 يوم للاستحقاق
        FINE_PER_DAY.put("book", 10.0);     // 10 شيكل لكل يوم تأخير

        // تهيئة القيم للأقراص المدمجة
        BORROW_DAYS.put("cd", 7);           // 7 أيام للاستحقاق
        FINE_PER_DAY.put("cd", 20.0);       // 20 شيكل لكل يوم تأخير
    }

    // دالة رئيسية للاستعارة (تعمل مع أي نوع من المواد)
    public static boolean borrowMedia(Borrower borrower, Media media) {
        // الشيكات الأساسية
        if (!borrower.isLoggedIn()) {
            System.out.println("يجب تسجيل الدخول أولاً.");
            return false;
        }
        if (borrower.getFineBalance() > 0) {
            System.out.println("ادفع غرامتك (" + borrower.getFineBalance() + ") أولاً.");
            return false;
        }
        if (!borrower.getOverdueMedia().isEmpty()) {
            System.out.println("يجب إرجاع المواد المتأخرة أولاً.");
            return false;
        }
        if (!media.isAvailable()) {
            System.out.println("المادة '" + media.getTitle() + "' غير متاحة.");
            return false;
        }

        // تنفيذ الاستعارة
        media.borrow();  // تحديث حالة المادة
        LocalDate dueDate = LocalDate.now().plusDays(BORROW_DAYS.get(media.getMediaType()));
        borrower.addBorrowRecord(media, dueDate);  // حفظ السجل في Borrower

        String mediaTypeArabic = media.getMediaType().equals("book") ? "الكتاب" : "القرص المدمج";
        System.out.println("تم استعارة " + mediaTypeArabic + " '" + media.getTitle() + "'. تاريخ الاستحقاق: " + dueDate);
        return true;
    }

    // دالة للإرجاع (مع حساب الغرامة المناسبة لنوع المادة)
    public static boolean returnMedia(Borrower borrower, Media media) {
        for (Borrower.MediaRecord record : borrower.getBorrowedMedia()) {
            if (record.getMedia().getId() == media.getId()) {
                media.returnMedia();  // تحديث حالة المادة
                double mediaFine = 0.0;
                if (record.isOverdue()) {
                    long overdueDays = record.getOverdueDays();
                    mediaFine = overdueDays * FINE_PER_DAY.get(media.getMediaType());
                    String mediaTypeArabic = media.getMediaType().equals("book") ? "الكتاب" : "القرص المدمج";
                    System.out.println(mediaTypeArabic + " متأخر بـ " + overdueDays + " أيام. غرامة: " + mediaFine);
                }
                borrower.removeBorrowRecord(record, mediaFine);  // إزالة السجل وإضافة الغرامة
                System.out.println("تم إرجاع '" + media.getTitle() + "' بنجاح. الرصيد الكلي: " + borrower.getFineBalance());
                return true;
            }
        }
        System.out.println("هذه المادة غير مستعارة منك.");
        return false;
    }

    // دوال مساعدة للحصول على فترات الاستعارة والغرامات
    public static int getBorrowDays(String mediaType) {
        return BORROW_DAYS.getOrDefault(mediaType, 0);
    }

    public static double getFinePerDay(String mediaType) {
        return FINE_PER_DAY.getOrDefault(mediaType, 0.0);
    }
}