package service;

import dao.BorrowRecordDAO;
import dao.FineDAO;
import model.Borrower;
import model.MediaRecord;

import java.util.List;

/**
 * Service class responsible for managing borrower-related operations,
 * including:
 * <ul>
 *     <li>Loading currently borrowed media and fine balances</li>
 *     <li>Generating detailed overdue reports with calculated fines</li>
 *     <li>Processing fine payments</li>
 *     <li>Displaying all borrowed media items with overdue warnings</li>
 * </ul>
 *
 * <p>This service interacts with {@link BorrowRecordDAO} and {@link FineDAO}
 * to retrieve and update borrower information.</p>
 *
 * @author Library
 * @version 1.1
 */
public class BorrowerService {

    /** DAO used for retrieving and updating borrow record information. */
    private final BorrowRecordDAO borrowRecordDAO;

    /** DAO used for managing borrower fines. */
    private final FineDAO fineDAO;

    /**
     * Constructs a {@code BorrowerService} instance and initializes DAOs.
     */
    public BorrowerService() {
        this.borrowRecordDAO = new BorrowRecordDAO();
        this.fineDAO = new FineDAO();
    }

    /**
     * Loads the borrower's current borrowed media items and fine balance.
     *
     * @param borrower The borrower whose data will be loaded.
     */
    public void loadBorrowerData(Borrower borrower) {
        List<MediaRecord> borrowedMedia = borrowRecordDAO.findActiveByUserId(borrower.getId());
        borrower.setBorrowedMedia(borrowedMedia);

        double fineBalance = fineDAO.getFineBalance(borrower.getId());
        borrower.setFineBalance(fineBalance);

        System.out.println("Loaded " + borrowedMedia.size() + " borrowed items for " + borrower.getUsername());
    }

    /**
     * Generates and prints a detailed overdue report for the borrower,
     * including overdue days and calculated fines per item.
     *
     * @param borrower The borrower for whom the overdue report is generated.
     */
    public void generateOverdueReport(Borrower borrower) {
        List<MediaRecord> overdueItems = borrower.getOverdueMedia();

        if (overdueItems.isEmpty()) {
            System.out.println("You have no overdue items.");
            return;
        }

        System.out.println("\n=== Overdue Items Report ===");
        double totalFine = 0.0;

        for (MediaRecord record : overdueItems) {
            long overdueDays = record.getOverdueDays();
            double finePerDay = BorrowingService.getFinePerDay(record.getMedia().getMediaType());
            double itemFine = overdueDays * finePerDay;
            totalFine += itemFine;

            String mediaType = record.getMedia().getMediaType().equals("book") ? "Book" : "CD";
            System.out.printf("- Title: '%s', Type: %s, Overdue Days: %d, Fine: %.2f\n",
                    record.getMedia().getTitle(), mediaType, overdueDays, itemFine);
        }

        System.out.println("-----------------------------");
        System.out.printf("Total Overdue Fines: %.2f\n", totalFine);
        System.out.println("=============================\n");
    }

    /**
     * Processes a payment towards the borrower's outstanding fines.
     *
     * @param borrower The borrower making the payment.
     * @param amount   The payment amount; must be positive and not exceed current fine balance.
     * @return {@code true} if the payment is successful; {@code false} otherwise.
     */
    public boolean payFine(Borrower borrower, double amount) {
        if (amount <= 0 || amount > borrower.getFineBalance()) {
            System.out.println("Invalid payment amount.");
            return false;
        }

        if (fineDAO.payFine(borrower.getId(), amount)) {
            borrower.setFineBalance(borrower.getFineBalance() - amount);
            System.out.printf("Payment of %.2f successful. New balance: %.2f\n",
                    amount, borrower.getFineBalance());
            return true;
        }

        return false;
    }

    /**
     * Displays all currently borrowed media items for the borrower,
     * including due dates and overdue warnings where applicable.
     *
     * @param borrower The borrower whose borrowed items will be displayed.
     */
    public void displayBorrowedMedia(Borrower borrower) {
        List<MediaRecord> borrowed = borrower.getBorrowedMedia();

        if (borrowed.isEmpty()) {
            System.out.println("You have no borrowed items.");
            return;
        }

        System.out.println("\n=== Your Borrowed Items ===");
        for (MediaRecord record : borrowed) {
            System.out.println(record.getMedia());
            System.out.println("Due Date: " + record.getDueDate());
            if (record.isOverdue()) {
                System.out.println("WARNING: OVERDUE by " + record.getOverdueDays() + " days");

            }
            System.out.println("---");
        }
        System.out.println("===========================\n");
    }
}