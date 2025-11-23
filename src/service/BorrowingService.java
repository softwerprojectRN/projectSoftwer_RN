package service;

import dao.BorrowRecordDAO;
import dao.FineDAO;
import dao.MediaDAO;
import model.Borrower;
import model.Media;
import model.MediaRecord;
import model.UserWithOverdueBooks;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BorrowingService {
    private static final Map<String, Integer> BORROW_DAYS = new HashMap<>();
    private static final Map<String, Double> FINE_PER_DAY = new HashMap<>();

    private final BorrowRecordDAO borrowRecordDAO;
    private final FineDAO fineDAO;
    private final MediaDAO mediaDAO;

    static {
        BORROW_DAYS.put("book", 28);
        FINE_PER_DAY.put("book", 10.0);
        BORROW_DAYS.put("cd", 7);
        FINE_PER_DAY.put("cd", 20.0);
    }

    public BorrowingService() {
        this.borrowRecordDAO = new BorrowRecordDAO();
        this.fineDAO = new FineDAO();
        this.mediaDAO = new MediaDAO();

        this.borrowRecordDAO.initializeTable();
        this.fineDAO.initializeTable();
    }

    public boolean borrowMedia(Borrower borrower, Media media) {
        // Validation checks
        if (!borrower.isLoggedIn()) {
            System.out.println("Error: You must be logged in to borrow media.");
            return false;
        }

        if (borrower.getFineBalance() > 0) {
            System.out.println("Error: Please pay your fine (" + borrower.getFineBalance() + ") first.");
            return false;
        }

        if (!borrower.getOverdueMedia().isEmpty()) {
            System.out.println("Error: You must return overdue media first.");
            return false;
        }

        if (!media.isAvailable()) {
            System.out.println("Error: Media '" + media.getTitle() + "' is not available.");
            return false;
        }

        // Calculate due date
        LocalDate dueDate = LocalDate.now().plusDays(BORROW_DAYS.get(media.getMediaType()));

        // Update media availability
        mediaDAO.updateAvailability(media.getId(), false);
        media.setAvailable(false);

        // Create borrow record
        int recordId = borrowRecordDAO.insert(
                borrower.getId(),
                media.getId(),
                media.getMediaType(),
                media.getTitle(),
                LocalDate.now(),
                dueDate
        );

        if (recordId != -1) {
            // Add to borrower's list
            MediaRecord record = new MediaRecord(recordId, media, dueDate);
            List<MediaRecord> borrowed = borrower.getBorrowedMedia();
            borrowed.add(record);
            borrower.setBorrowedMedia(borrowed);

            System.out.println("Successfully borrowed '" + media.getTitle() + "'. Due date: " + dueDate);
            return true;
        }

        return false;
    }

    public boolean returnMedia(Borrower borrower, Media media) {
        for (MediaRecord record : borrower.getBorrowedMedia()) {
            if (record.getMedia().getId() == media.getId()) {
                // Update media availability
                mediaDAO.updateAvailability(media.getId(), true);
                media.setAvailable(true);

                // Calculate fine if overdue
                double mediaFine = 0.0;
                if (record.isOverdue()) {
                    long overdueDays = record.getOverdueDays();
                    mediaFine = overdueDays * FINE_PER_DAY.get(media.getMediaType());
                    System.out.println("Media is " + overdueDays + " days overdue. Fine: " + mediaFine);
                }

                // Update borrow record
                borrowRecordDAO.markAsReturned(record.getRecordId(), LocalDate.now(), mediaFine);

                // Update fine balance
                if (mediaFine > 0) {
                    double newBalance = borrower.getFineBalance() + mediaFine;
                    borrower.setFineBalance(newBalance);
                    fineDAO.updateFine(borrower.getId(), newBalance);
                }

                // Remove from borrower's list
                List<MediaRecord> borrowed = borrower.getBorrowedMedia();
                borrowed.remove(record);
                borrower.setBorrowedMedia(borrowed);

                System.out.println("Successfully returned '" + media.getTitle() + "'. Total fine balance: " + borrower.getFineBalance());
                return true;
            }
        }

        System.out.println("Error: This media is not borrowed by you.");
        return false;
    }

    public List<UserWithOverdueBooks> getUsersWithOverdueBooks() {
        return borrowRecordDAO.getUsersWithOverdueBooks();
    }

    public static int getBorrowDays(String mediaType) {
        return BORROW_DAYS.getOrDefault(mediaType, 0);
    }

    public static double getFinePerDay(String mediaType) {
        return FINE_PER_DAY.getOrDefault(mediaType, 0.0);
    }
}