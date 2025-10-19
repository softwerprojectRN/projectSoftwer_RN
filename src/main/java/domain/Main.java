package domain;

import domain.*;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static Admin currentAdmin = null;
    private static Borrower currentBorrower = null;

    public static void main(String[] args) {
        System.out.println("===== نظام إدارة المكتبة - وضع الاختبار =====");

        // اختبار الاتصال بقاعدة البيانات
        testDatabaseConnection();

        // اختبار وظائف المدير
        testAdminFeatures();

        // اختبار وظائف المستخدم
        testUserFeatures();

        // اختبار الاستعارة والإرجاع
        testBorrowingAndReturning();

        // اختبار إدارة الغرامات
        testFineManagement();

        // اختبار وظيفة حذف المستخدم من قبل المدير
        testAdminUnregisterUser();





        testOverdueReport();

        testFinePayment(); // <<<< أضف هذا السطر


        System.out.println("\n===== اكتملت جميع الاختبارات =====");
    }

    private static void testDatabaseConnection() {
        System.out.println("\n--- اختبار الاتصال بقاعدة البيانات ---");
        Connection conn = Media.connect();
        if (conn != null) {
            System.out.println("تم الاتصال بقاعدة البيانات بنجاح!");
            try {
                conn.close();
            } catch (Exception e) {
                System.out.println("خطأ أثناء إغلاق الاتصال: " + e.getMessage());
            }
        } else {
            System.out.println("فشل الاتصال بقاعدة البيانات!");
        }
    }

    private static void testAdminFeatures() {
        System.out.println("\n--- اختبار وظائف المدير ---");

        // تسجيل مدير جديد
        System.out.println("محاولة تسجيل مدير جديد...");
        Admin admin = Admin.register("admin1", "admin123");
        if (admin != null) {
            System.out.println("تم تسجيل المدير بنجاح: " + admin.getUsername());
        } else {
            System.out.println("فشل تسجيل المدير أو أنه موجود بالفعل");
        }

        // محاولة تسجيل نفس المدير مرة أخرى
        System.out.println("محاولة تسجيل مدير موجود مسبقًا...");
        Admin admin2 = Admin.register("admin1", "admin123");
        if (admin2 == null) {
            System.out.println("تم اكتشاف المدير المكرر بنجاح (هذا السلوك صحيح)");
        }

        // تسجيل دخول المدير
        System.out.println("محاولة تسجيل دخول المدير...");
        currentAdmin = Admin.login("admin1", "admin123");
        if (currentAdmin != null) {
            System.out.println("تم تسجيل دخول المدير بنجاح");
            currentAdmin.showAdminInfo();
        } else {
            System.out.println("فشل تسجيل دخول المدير");
        }

        // محاولة تسجيل الدخول بكلمة مرور خاطئة
        System.out.println("محاولة تسجيل الدخول بكلمة مرور خاطئة...");
        Admin adminWrong = Admin.login("admin1", "wrongpass");
        if (adminWrong == null) {
            System.out.println("تم رفض كلمة المرور الخاطئة بنجاح (هذا السلوك صحيح)");
        }
    }

    private static void testUserFeatures() {
        System.out.println("\n--- اختبار وظائف المستخدم ---");

        // تسجيل مستخدم جديد
        System.out.println("محاولة تسجيل مستخدم جديد...");
        User user = User.register("user1", "user123");
        if (user != null) {
            System.out.println("تم تسجيل المستخدم بنجاح: " + user.getUsername());
        } else {
            System.out.println("فشل تسجيل المستخدم أو أنه موجود بالفعل");
        }

        // تسجيل دخول المستخدم
        System.out.println("محاولة تسجيل دخول المستخدم...");
        User loginUser = User.login("user1", "user123");
        if (loginUser != null) {
            System.out.println("تم تسجيل دخول المستخدم بنجاح");
            // إنشاء كائن Borrower لاختبار وظائف الاستعارة
            currentBorrower = new Borrower(loginUser.getUsername(),
                    loginUser.getPasswordHash(),
                    ""); // ملاحظة: يجب جلب الـ salt الفعلي من قاعدة البيانات
            currentBorrower.setLoggedIn(true);
        } else {
            System.out.println("فشل تسجيل دخول المستخدم");
        }
    }

    private static void testBorrowingAndReturning() {
        System.out.println("\n--- اختبار الاستعارة والإرجاع ---");

        // إضافة كتاب جديد
        System.out.println("إضافة كتاب جديد للاختبار...");
        Book book1 = Book.addBook("Java Programming", "Bruce Eckel", "9787111213826");
        if (book1 != null) {
            System.out.println("تمت إضافة الكتاب بنجاح: " + book1.getTitle());
        }

        // إضافة قرص مدمج جديد
        System.out.println("إضافة قرص مدمج جديد للاختبار...");
        CD cd1 = CD.addCD("Classical Music", "Beethoven", "Classical", 60);
        if (cd1 != null) {
            System.out.println("تمت إضافة القرص المدمج بنجاح: " + cd1.getTitle());
        }

        // جلب جميع الكتب والأقراص المدمجة وعرضها
        System.out.println("\nقائمة جميع الكتب في قاعدة البيانات:");
        List<Book> books = Book.getAllBooks();
        for (Book book : books) {
            System.out.println(book);
        }

        System.out.println("\nقائمة جميع الأقراص المدمجة في قاعدة البيانات:");
        List<CD> cds = CD.getAllCDs();
        for (CD cd : cds) {
            System.out.println(cd);
        }

        // المستخدم يستعير كتابًا
        if (currentBorrower != null && book1 != null) {
            System.out.println("\nالمستخدم يحاول استعارة كتاب...");
            boolean borrowResult = BorrowingService.borrowMedia(currentBorrower, book1);
            if (borrowResult) {
                System.out.println("تمت الاستعارة بنجاح");
            } else {
                System.out.println("فشلت عملية الاستعارة");
            }

            // عرض المواد المستعارة حاليًا من قبل المستخدم
            System.out.println("\nالمواد المستعارة حاليًا من قبل المستخدم:");
            for (Borrower.MediaRecord record : currentBorrower.getBorrowedMedia()) {
                System.out.println("- " + record.getMedia().getTitle() + ", تاريخ الإرجاع: " + record.getDueDate());
            }

            // المستخدم يعيد الكتاب
            System.out.println("\nالمستخدم يعيد الكتاب...");
            boolean returnResult = BorrowingService.returnMedia(currentBorrower, book1);
            if (returnResult) {
                System.out.println("تم الإرجاع بنجاح");
            } else {
                System.out.println("فشلت عملية الإرجاع");
            }
        }
    }

    private static void testFineManagement() {
        System.out.println("\n--- اختبار إدارة الغرامات ---");

        // إضافة كتاب آخر لاختبار الغرامات
        Book book2 = Book.addBook("Introduction to Algorithms", "Thomas H. Cormen", "9787111407928");

        if (currentBorrower != null && book2 != null) {
            // المستخدم يستعير الكتاب
            BorrowingService.borrowMedia(currentBorrower, book2);

            // عرض رصيد الغرامات الحالي
            System.out.println("رصيد الغرامات الحالي: " + currentBorrower.getFineBalance());

            // محاكاة دفع الغرامة (إذا وجدت)
            if (currentBorrower.getFineBalance() > 0) {
                System.out.println("محاولة دفع الغرامة...");
                boolean payResult = currentBorrower.payFine(currentBorrower.getFineBalance());
                if (payResult) {
                    System.out.println("تم دفع الغرامة بنجاح");
                } else {
                    System.out.println("فشل دفع الغرامة");
                }
            }

            // المستخدم يعيد الكتاب
            BorrowingService.returnMedia(currentBorrower, book2);
        }
    }

    private static void testAdminUnregisterUser() {
        System.out.println("\n--- اختبار وظيفة حذف المستخدم من قبل المدير ---");

        // تسجيل مستخدم اختبار
        User testUser = User.register("testuser", "testpass");

        if (currentAdmin != null && testUser != null) {
            // المدير يحذف المستخدم
            System.out.println("المدير يحاول حذف المستخدم 'testuser'...");
            boolean unregisterResult = Admin.unregisterUser("testuser");
            if (unregisterResult) {
                System.out.println("تم حذف المستخدم بنجاح");
            } else {
                System.out.println("فشل حذف المستخدم");
            }

            // محاولة حذف مستخدم غير موجود
            System.out.println("محاولة حذف مستخدم غير موجود...");
            boolean unregisterResult2 = Admin.unregisterUser("nonexistentuser");
            if (!unregisterResult2) {
                System.out.println("تم اكتشاف أن المستخدم غير موجود بنجاح (هذا السلوك صحيح)");
            }
        }
    }

    private static void testOverdueReport() {
        System.out.println("\n--- اختبار تقرير المواد المتأخرة ---");

        if (currentBorrower == null) {
            System.out.println("لا يوجد مستخدم مسجل دخوله لتشغيل الاختبار.");
            return;
        }

        // إضافة كتاب وقرص مدمج للاختبار
        Book overdueBook = Book.addBook("Design Patterns", "Erich Gamma", "9780201633610");
        CD overdueCD = CD.addCD("Best of 90s", "Various Artists", "Pop", 75);

        if (overdueBook != null && overdueCD != null) {
            // المستخدم يستعير المادتين
            BorrowingService.borrowMedia(currentBorrower, overdueBook);
            BorrowingService.borrowMedia(currentBorrower, overdueCD);

            // لمحاكاة التأخير، سنقوم بتعديل تاريخ الاستحقاق يدويًا
            // نجعل تاريخ الاستحقاق قبل 10 أيام من اليوم
            LocalDate pastDate = LocalDate.now().minusDays(10);
            for (Borrower.MediaRecord record : currentBorrower.getBorrowedMedia()) {
                record.setDueDate(pastDate);
            }

            System.out.println("تم محاكاة وجود مواد مستعارة متأخرة 10 أيام.");
            System.out.println("الغرامة المتوقعة للكتاب (10 أيام * 10.0) = 100.0");
            System.out.println("الغرامة المتوقعة للقرص المدمج (10 أيام * 20.0) = 200.0");
            System.out.println("الإجمالي المتوقع = 300.0\n");

            // الآن، قم بإنشاء وعرض التقرير
            currentBorrower.generateOverdueReport();

            // نظيف بعد الاختبار
            BorrowingService.returnMedia(currentBorrower, overdueBook);
            BorrowingService.returnMedia(currentBorrower, overdueCD);
        }
    }

    private static void testFinePayment() {
        System.out.println("\n--- اختبار دفع الغرامات ---");

        if (currentBorrower == null) {
            System.out.println("لا يوجد مستخدم مسجل دخوله لتشغيل الاختبار.");
            return;
        }

        // التأكد من أن رصيد الغرامات يبدأ من الصفر
        currentBorrower.setFineBalance(0.0);
        currentBorrower.saveFineBalance();
        System.out.println("تم تصفير رصيد الغرامات لبدء الاختبار. الرصيد الحالي: " + currentBorrower.getFineBalance());

        // 1. إنشاء غرامة عن طريق استعارة مادة وجعلها متأخرة
        Book fineBook = Book.addBook("Late Payment Book", "Test Author", "1234567890");
        if (fineBook != null) {
            BorrowingService.borrowMedia(currentBorrower, fineBook);
            // محاكاة التأخير 5 أيام (غرامة الكتاب 10 شيكل في اليوم)
            LocalDate pastDate = LocalDate.now().minusDays(5);
            for (Borrower.MediaRecord record : currentBorrower.getBorrowedMedia()) {
                if (record.getMedia().getId() == fineBook.getId()) {
                    record.setDueDate(pastDate);
                    break;
                }
            }
            System.out.println("تم استعارة كتاب وتأخيره 5 أيام لإنشاء غرامة.");
        }

        // 2. إرجاع المادة لتحديث رصيد الغرامة في قاعدة البيانات
        BorrowingService.returnMedia(currentBorrower, fineBook);
        System.out.println("تم إرجاع الكتاب.");
        double fineAmount = currentBorrower.getFineBalance();
        System.out.printf("الغرامة المتراكمة: %.2f\n", fineAmount);

        if (fineAmount > 0) {
            // 3. محاولة دفع الغرامة بالكامل
            System.out.println("\nمحاولة دفع الغرامة بالكامل...");
            boolean paid = currentBorrower.payFine(fineAmount);
            if (paid) {
                System.out.printf("تم الدفع بنجاح. الرصيد الجديد: %.2f\n", currentBorrower.getFineBalance());
            } else {
                System.out.println("فشل الدفع.");
            }

            // 4. محاولة دفع مبلغ أكبر من الغرامة (يجب أن يفشل)
            System.out.println("\nمحاولة دفع مبلغ أكبر من الغرامة (يجب أن يفشل)...");
            boolean overPaid = currentBorrower.payFine(10.0);
            if (!overPaid) {
                System.out.println("تم رفض الدفع بشكل صحيح لأن المبلغ أكبر من الرصيد.");
            }

            // 5. محاولة دفع مبلغ سالب (يجب أن يفشل)
            System.out.println("\nمحاولة دفع مبلغ سالب (يجب أن يفشل)...");
            boolean negativePaid = currentBorrower.payFine(-5.0);
            if (!negativePaid) {
                System.out.println("تم رفض الدفع بشكل صحيح لأن المبلغ سالب.");
            }
        } else {
            System.out.println("لم يتم إنشاء غرامة، لا يمكن اختبار الدفع.");
        }
    }
}