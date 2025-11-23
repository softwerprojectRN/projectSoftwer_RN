package service;

import dao.BorrowRecordDAO;
import dao.FineDAO;
import model.Borrower;
import model.MediaRecord;

import java.util.List;

public class BorrowerService {
    private final BorrowRecordDAO borrowRecordDAO;
    private final FineDAO fineDAO;

    public BorrowerService() {
        this.borrowRecordDAO = new BorrowRecordDAO();
        this.fineDAO = new FineDAO();
    }

    public void loadBorrowerData(Borrower borrower) {
        // Load borrowed media
        List<MediaRecord> borrowedMedia = borrowRecordDAO.findActiveByUserId(borrower.getId());
        borrower.setBorrowedMedia(borrowedMedia);

        // Load fine balance
        double fineBalance = fineDAO.getFineBalance(borrower.getId());
        borrower.setFineBalance(fineBalance);

        System.out.println("Loaded " + borrowedMedia.size() + " borrowed items for " + borrower.getUsername());
    }

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
                System.out.println("⚠️  OVERDUE by " + record.getOverdueDays() + " days");
            }
            System.out.println("---");
        }
        System.out.println("===========================\n");
    }
}