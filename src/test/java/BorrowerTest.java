import domain.Book;
import domain.Borrower;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test class for the Borrower class.
 * This suite is designed to achieve 100% coverage for Methods, Lines, and Branches.
 */
class BorrowerTest {

    private Borrower borrower;
    private Book availableBook;
    private Book anotherBook;

    @BeforeEach
    void setUp() {
        borrower = new Borrower("testuser", "password");
        borrower.login("testuser", "password");

        availableBook = new Book("The Great Gatsby", "F. Scott Fitzgerald", "978-0743273565");
        anotherBook = new Book("1984", "George Orwell", "978-0451524935");
    }

    // ========================================================================
    // Test Cases for borrowBook() - US2.1
    // ========================================================================
    //يتأكد إنو لما المستخدم يستعير كتاب، حالة الكتاب تتغير إلى غير متاح (isAvailable = false).
    @Test
    void testBorrowBook_MarksBookAsUnavailable() {
        borrower.borrowBook(availableBook);
        assertFalse(availableBook.isAvailable(), "Book should be marked as unavailable after borrowing.");
    }
//تأكد إنو الكتاب المٌستعار ينضاف إلى قائمة الكتب المستعارة عند المستخدم (borrowedBooks).
    @Test
    void testBorrowBook_AddsBookToBorrowedList() {
        borrower.borrowBook(availableBook);
        assertEquals(1, borrower.getBorrowedBooks().size(), "Borrower's book list should contain one book.");
    }

    @Test
    void testBorrowBook_FailsWhenNotLoggedIn() {
        Borrower loggedOutBorrower = new Borrower("user2", "pass");
        loggedOutBorrower.borrowBook(availableBook);
        assertEquals(0, loggedOutBorrower.getBorrowedBooks().size(), "Logged out user should not be able to borrow.");
    }
//التأكد إنو المستخدم اللي عليه غرامة (fineBalance > 0) ما يقدر يستعير كتاب.
    @Test
    void testBorrowBook_FailsWithUnpaidFines() {
        setFineBalance(borrower, 10.0);
        borrower.borrowBook(availableBook);
        assertEquals(0, borrower.getBorrowedBooks().size(), "User with fines should not be able to borrow.");
    }
//التأكد إنو المستخدم ما يقدر يستعير كتاب أصلاً مش متاح.
    @Test
    void testBorrowBook_FailsWhenBookIsNotAvailable() {
        availableBook.setAvailable(false);
        borrower.borrowBook(availableBook);
        assertEquals(0, borrower.getBorrowedBooks().size(), "User should not be able to borrow an unavailable book.");
    }

    // ========================================================================
    // Test Cases for returnBook() and Fine Calculation - US2.2 & US2.3
    // ========================================================================
//التأكد إنو لما ترجعي الكتاب بالوقت، يرجع متاح (isAvailable = true).
    @Test
    void testReturnBook_OnTime_MarksBookAsAvailable() {
        borrower.borrowBook(availableBook);
        borrower.returnBook(availableBook);
        assertTrue(availableBook.isAvailable(), "Book should be marked as available after on-time return.");
    }
//: التأكد إنو بعد إرجاع الكتاب، ينحذف من قائمة الكتب المستعارة.
    @Test
    void testReturnBook_OnTime_RemovesBookFromBorrowedList() {
        borrower.borrowBook(availableBook);
        borrower.returnBook(availableBook);
        assertEquals(0, borrower.getBorrowedBooks().size(), "Book should be removed from borrower's list after return.");
    }
//التأكد إنو إذا رجع الكتاب بالموعد، ما تنضاف أي غرامة للمستخدم (fineBalance = 0).
    @Test
    void testReturnBook_OnTime_DoesNotAddFine() {
        borrower.borrowBook(availableBook);
        borrower.returnBook(availableBook);
        assertEquals(0.0, borrower.getFineBalance(), "Fine balance should be zero for on-time return.");
    }
//التأكد إنو حتى لو الكتاب تأخر (متأخر 5 أيام مثلاً)، لما يرجع يصير متاح مرة ثانية.
    @Test
    void testReturnBook_Overdue_MarksBookAsAvailable() {
        borrower.borrowBook(availableBook);
        setDueDate(borrower.getBorrowedBooks().get(0), LocalDate.now().minusDays(5));
        borrower.returnBook(availableBook);
        assertTrue(availableBook.isAvailable(), "Overdue book should be marked as available after return.");
    }
//التأكد إنو الكتاب المتأخر كمان ينشال من قائمة الكتب المستعارة بعد الإرجاع
    @Test
    void testReturnBook_Overdue_RemovesBookFromBorrowedList() {
        borrower.borrowBook(availableBook);
        setDueDate(borrower.getBorrowedBooks().get(0), LocalDate.now().minusDays(5));
        borrower.returnBook(availableBook);
        assertEquals(0, borrower.getBorrowedBooks().size(), "Overdue book should be removed from borrower's list after return.");
    }
//التأكد إنو الغرامة المحسوبة تساوي عدد الأيام المتأخرة.
    @Test
    void testReturnBook_Overdue_AddsCorrectFine() {
        borrower.borrowBook(availableBook);
        setDueDate(borrower.getBorrowedBooks().get(0), LocalDate.now().minusDays(5));
        borrower.returnBook(availableBook);
        assertEquals(5.0, borrower.getFineBalance(), "Fine balance should be 5.0 for a 5-day overdue book.");
    }
    @Test
    void testReturnBook_FailsIfBookNotBorrowedByUser() {
        borrower.borrowBook(availableBook);
        borrower.returnBook(anotherBook);
        assertEquals(1, borrower.getBorrowedBooks().size(), "Borrowed books list should not change if returning a non-borrowed book.");
    }
    @Test
    void testReturnBook_FailsWhenBorrowedListIsEmpty() {
        // Covers the case where the 'for' loop is never entered.
        borrower.returnBook(availableBook);
        assertEquals(0, borrower.getBorrowedBooks().size(), "List should remain empty if user tries to return a book they never borrowed.");
    }

    // ========================================================================
    // Test Cases for payFine() - US2.3
    // ========================================================================
//لما المستخدم يدفع الغرامة كاملة → الرصيد يصير صفر.
    @Test
    void testPayFine_FullPayment() {
        setFineBalance(borrower, 10.0);
        borrower.payFine(10.0);
        assertEquals(0.0, borrower.getFineBalance(), "Fine balance should be zero after full payment.");
    }
//التأكد إنو الدفع الجزئي ينقص الغرامة بمقدار المبلغ المدفوع.
    @Test
    void testPayFine_PartialPayment() {
        setFineBalance(borrower, 10.0);
        borrower.payFine(4.0);
        assertEquals(6.0, borrower.getFineBalance(), "Fine balance should be reduced by the partial payment.");
    }
//إذا المستخدم دفع أكثر من الغرامة، الرصيد ما بصير سالب، يثبت عند صفر.
    @Test
    void testPayFine_Overpayment() {
        setFineBalance(borrower, 10.0);
        borrower.payFine(15.0);
        assertEquals(0.0, borrower.getFineBalance(), "Fine balance should be zero, not negative, after overpayment.");
    }
//ذا حاول يدفع صفر أو سالب → ما يصير أي تغيير على الغرامة.
    @Test
    void testPayFine_FailsWithInvalidAmount() {

        setFineBalance(borrower, 10.0);
        borrower.payFine(0.0);
        assertEquals(10.0, borrower.getFineBalance(), "Fine balance should not change with invalid payment.");
    }
//يغطي حالة إنو المستخدم ما عليه أي غرامة، ومع هيك يحاول يدفع.
    @Test
    void testPayFine_FailsWhenNoFinesToPay() {

        borrower.payFine(5.0);
        assertEquals(0.0, borrower.getFineBalance(), "Fine balance should remain zero if there are no fines to pay.");
    }

    // ========================================================================
    // Test Cases for getOverdueBooks() - US2.2
    // ========================================================================

    @Test
    void testGetOverdueBooks_ReturnsEmptyList() {
        borrower.borrowBook(availableBook);
        List<Borrower.BookRecord> overdueBooks = borrower.getOverdueBooks();
        assertTrue(overdueBooks.isEmpty(), "Should return an empty list when no books are overdue.");
    }
//التأكد إنو الدالة ترجع العدد الصحيح من الكتب المتأخرة فقط.
    @Test
    void testGetOverdueBooks_ReturnsCorrectCount() {
        borrower.borrowBook(availableBook);
        borrower.borrowBook(anotherBook);
        setDueDate(borrower.getBorrowedBooks().get(1), LocalDate.now().minusDays(2));
        List<Borrower.BookRecord> overdueBooks = borrower.getOverdueBooks();
        assertEquals(1, overdueBooks.size(), "Should return a list with one overdue book.");
    }
//يتأكد إنو الكتاب اللي راجعته الدالة فعلاً هو المتأخر (مش غيره).
    @Test
    void testGetOverdueBooks_ReturnsCorrectBook() {
        borrower.borrowBook(availableBook);
        borrower.borrowBook(anotherBook);
        setDueDate(borrower.getBorrowedBooks().get(1), LocalDate.now().minusDays(2));
        List<Borrower.BookRecord> overdueBooks = borrower.getOverdueBooks();
        assertEquals(anotherBook, overdueBooks.get(0).getBook(), "The returned book should be the overdue one.");
    }

    // ========================================================================
    // Test Cases for uncalled methods (to improve Method and Line Coverage)
    // ========================================================================

    @Test
    void testShowBorrowedBooks_WhenListIsEmpty() {
        borrower.showBorrowedBooks();
        assertTrue(true, "Method executed without throwing an exception.");
    }

    @Test
    void testShowBorrowedBooks_WhenListHasNoOverdueBooks() {
        // Covers the 'else' part of the ternary operator in showBorrowedBooks().
        borrower.borrowBook(availableBook);
        borrower.showBorrowedBooks();
        assertTrue(true, "Method executed without throwing an exception.");
    }

    @Test
    void testShowOverdueBooks_WhenListIsEmpty() {
        borrower.showOverdueBooks();
        assertTrue(true, "Method executed without throwing an exception.");
    }

    @Test
    void testShowOverdueBooks_WhenListHasNoOverdueBooks() {
        // Covers the 'if(!anyOverdue)' branch in showOverdueBooks().
        borrower.borrowBook(availableBook);
        borrower.showOverdueBooks();
        assertTrue(true, "Method executed without throwing an exception.");
    }

    @Test
    void testShowOverdueBooks_WhenListHasOverdueBooks() {
        borrower.borrowBook(availableBook);
        setDueDate(borrower.getBorrowedBooks().get(0), LocalDate.now().minusDays(2));
        borrower.showOverdueBooks();
        assertTrue(true, "Method executed without throwing an exception.");
    }

    @Test
    void testCheckOverdueBooks_WhenListIsEmpty() {
        borrower.checkOverdueBooks();
        assertTrue(true, "Method executed without throwing an exception.");
    }

    @Test
    void testCheckOverdueBooks_WhenListHasNoOverdueBooks() {
        // Covers the 'if (!found)' branch in checkOverdueBooks().
        borrower.borrowBook(availableBook);
        borrower.checkOverdueBooks();
        assertTrue(true, "Method executed without throwing an exception.");
    }

    @Test
    void testCheckOverdueBooks_WhenListHasOverdueBooks() {
        borrower.borrowBook(availableBook);
        setDueDate(borrower.getBorrowedBooks().get(0), LocalDate.now().minusDays(2));
        borrower.checkOverdueBooks();
        assertTrue(true, "Method executed without throwing an exception.");
    }

    // ========================================================================
    // Test Cases for the inner class BookRecord (for 100% Branch Coverage)
    // ========================================================================

    @Test
    void testGetOverdueDays_ForNonOverdueBook() {
        // Covers the 'if(!isOverdue()) return 0;' branch in BookRecord.getOverdueDays().
        Borrower.BookRecord onTimeRecord = borrower.new BookRecord(availableBook, LocalDate.now().plusDays(10));
        assertEquals(0, onTimeRecord.getOverdueDays(), "Overdue days should be 0 for a non-overdue book.");
    }

    @Test
    void testGetOverdueDays_ForOverdueBook() {
        Borrower.BookRecord overdueRecord = borrower.new BookRecord(availableBook, LocalDate.now().minusDays(3));
        assertEquals(3, overdueRecord.getOverdueDays(), "Overdue days should be 3 for a book overdue by 3 days.");
    }


    // ========================================================================
    // Helper Methods (using Reflection to test private fields)
    // ========================================================================

    private void setFineBalance(Borrower borrower, double amount) {
        try {
            Field fineBalanceField = Borrower.class.getDeclaredField("fineBalance");
            fineBalanceField.setAccessible(true);
            fineBalanceField.set(borrower, amount);
        } catch (Exception e) {
            fail("Failed to set fine balance for test: " + e.getMessage());
        }
    }

    private void setDueDate(Borrower.BookRecord record, LocalDate date) {
        try {
            Field dueDateField = record.getClass().getDeclaredField("dueDate");
            dueDateField.setAccessible(true);
            dueDateField.set(record, date);
        } catch (Exception e) {
            fail("Failed to set due date for test: " + e.getMessage());
        }
    }
}