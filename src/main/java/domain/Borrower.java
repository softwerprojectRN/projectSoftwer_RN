package domain;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

// هذا الكلاس يمثل المستخدم الذي يستطيع استعارة الكتب
public class Borrower extends User {

    private ArrayList<BookRecord> borrowedBooks = new ArrayList<>();
    private double fineBalance = 0.0; //US2.3 Pay fine

    public Borrower(String username, String password) {
        super(username, password);
    }



    public void borrowBook(Book book) {

        if(!isLoggedIn()){
            System.out.println("You must log in first to borrow books.");
            return;
        }
        if (fineBalance > 0) {
            System.out.println("You have unpaid fines. Please pay them before borrowing new books.");
            return;
        }



        if (book.isAvailable()) {
            book.borrow();
            LocalDate dueDate = LocalDate.now().plusDays(28); // تاريخ الاسترجاع بعد 28 يوم
            borrowedBooks.add(new BookRecord(book, dueDate));
            System.out.println("Book '" + book.getTitle() + "' borrowed successfully. Due date: " + dueDate);
        } else {
            System.out.println("Book '" + book.getTitle() + "' is already borrowed.");
        }
    }

    // إعادة كتاب
    public void returnBook(Book book) {
        BookRecord recordToRemove = null;

        for(BookRecord record : borrowedBooks) {
            if(record.getBook().equals(book)) {
                book.returnBook();
                recordToRemove = record;

                // حساب الغرامة إذا متأخر
                if(record.isOverdue()) {
                    long overdueDays = record.getOverdueDays();
                    double fine = overdueDays * 1.0; // مثال: 1 وحدة غرامة لكل يوم تأخير
                    fineBalance += fine;
                    System.out.println("Book '" + book.getTitle() + "' is overdue by " + overdueDays +
                            " days. Fine added: " + fine);
                }

                System.out.println("Book '" + book.getTitle() + "' returned successfully.");
                break;
            }
        }
        if(recordToRemove != null) {
            borrowedBooks.remove(recordToRemove);
        } else {
            System.out.println("This book was not borrowed by you.");
        }
    }

     /// ما بحس في منو فايدة هاض الفنكشن
    // عرض الكتب المستعارة
    public void showBorrowedBooks() {
        if(borrowedBooks.isEmpty()) {
            System.out.println("No borrowed books.");
            return;
        }

        System.out.println("Borrowed books:");
        for(BookRecord record : borrowedBooks) {
            System.out.println("- " + record.getBook().getTitle() + ", due: " + record.getDueDate() +
                    (record.isOverdue() ? " (Overdue!)" : ""));
        }
    }

    /// ما بحس في منو فايدة هاض الفنكشن
    // عرض الكتب المتأخرة فقط
    public void showOverdueBooks() {
        boolean anyOverdue = false;
        for(BookRecord record : borrowedBooks) {
            if(record.isOverdue()) {
                if(!anyOverdue) {
                    System.out.println("Overdue books:");
                    anyOverdue = true;
                }
                System.out.println("- " + record.getBook().getTitle() + ", overdue by " +
                        record.getOverdueDays() + " days");
            }
        }
        if(!anyOverdue) System.out.println("No overdue books.");
    }


    // التحقق من الكتب المتأخرة وتحديث الغرامة
    public void checkOverdueBooks() {
        LocalDate today = LocalDate.now();
        boolean found = false;

        for (BookRecord record : borrowedBooks) {
            if (today.isAfter(record.getDueDate())) {
                found = true;
                long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(record.getDueDate(), today);
                System.out.println("Book '" + record.getBook().getTitle() + "' is overdue by " + overdueDays + " days.");
            }
        }

        if (!found)
            System.out.println("No overdue books detected.");
    }

    // دفع الغرامة
    public void payFine(double amount) {
        if (amount <= 0) {
            System.out.println("Invalid payment amount.");
            return;
        }

        if (fineBalance == 0) {
            System.out.println("No fines to pay.");
            return;
        }

        fineBalance -= amount;
        if (fineBalance < 0) fineBalance = 0;

        System.out.println("Payment successful. Remaining fine: " + fineBalance);
    }

    public double getFineBalance() {
        return fineBalance;
    }

    public List<BookRecord> getBorrowedBooks() {
        return borrowedBooks;
    }


    // كلاس داخلي لتخزين الكتاب وتاريخ الاسترجاع
    public class BookRecord {
        private Book book;
        private LocalDate dueDate;

        public BookRecord(Book book, LocalDate dueDate) {
            this.book = book;
            this.dueDate = dueDate;
        }

        public Book getBook() {
            return book;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }
        public boolean isOverdue() {
            return LocalDate.now().isAfter(dueDate);
        }

        public long getOverdueDays() {
            if(!isOverdue()) return 0; // إذا الكتاب ليس متأخر → 0
            //الغرض منها حساب عدد الأيام التي تأخر فيها الكتاب عن موعده.
            return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        }
    }

//////US2.2 Overdue book detection

    public List<BookRecord> getOverdueBooks (){
        List<BookRecord> overdue = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (BookRecord record : borrowedBooks) {
            if (today.isAfter(record.getDueDate())) {
                overdue.add(record);
            }
        }
        return overdue;
    }
}

