package domain;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

// هذا الكلاس يمثل المستخدم الذي يستطيع استعارة الكتب
public class Borrower extends User {

    public Borrower(String username, String password) {
        super(username, password);
    }

    private ArrayList<BookRecord> borrowedBooks = new ArrayList<>();

    public void borrowBook(Book book) {

        if(!isLoggedIn()){
            System.out.println("You must log in first to borrow books.");
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

    public void returnBook(Book book) {
        // نبحث عن الكتاب في قائمة المستعارات
        BookRecord recordToRemove = null;

        for (BookRecord record : borrowedBooks) {
            if (record.getBook() == book) { // وجدنا الكتاب
                book.returnBook(); // إعادة الكتاب متاح
                recordToRemove = record; // نحفظ السجل للحذف
                System.out.println("Book '" + book.getTitle() + "' returned successfully.");
                break;
            }
        }

        if (recordToRemove != null) {
            borrowedBooks.remove(recordToRemove); // إزالة الكتاب من قائمة المستعارات
        } else {
            System.out.println("This book was not borrowed by you.");
        }
    }

    // عرض الكتب المستعارة
    public void showBorrowedBooks() {
        if (borrowedBooks.isEmpty()) {
            System.out.println("No borrowed books.");
        } else {
            System.out.println("Borrowed books:");
            for (BookRecord record : borrowedBooks) {
                System.out.println("- " + record.getBook().getTitle() + ", due: " + record.getDueDate());
            }
        }
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
    }


}

