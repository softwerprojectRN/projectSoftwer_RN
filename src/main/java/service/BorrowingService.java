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

/**
 * Service class responsible for managing the borrowing and returning of media items
 * such as books and CDs. It handles validation, fine calculation, overdue tracking,
 * and communication with related DAOs.
 *
 * <p>This service ensures that users follow borrowing rules, such as paying fines
 * before borrowing and returning overdue items.</p>
 *
 * @author Library Management System
 * @version 1.0
 */
public class BorrowingService {

    /** Map storing allowed borrowing days for each media type. */
    private static final Map<String, Integer> BORROW_DAYS = new HashMap<>();

    /** Map storing fine-per-day policies for each media type. */
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

    /**
     * Constructs a BorrowingService instance and initializes required DAOs.
     */
    public BorrowingService() {
        this.borrowRecordDAO = new BorrowRecordDAO();
        this.fineDAO = new FineDAO();
        this.mediaDAO = new MediaDAO();
        this.borrowRecordDAO.initializeTable();
        this.fineDAO.initializeTable();
    }

    /**
     * Allows a borrower to borrow a media item if all borrowing conditions are met.
     *
     * @param borrower The borrower requesting to borrow media.
     * @param media    The media item being borrowed.
     * @return true if the borrowing operation was successful; false otherwise.
     */
    public boolean borrowMedia(Borrower borrower, Media media) {
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

        LocalDate dueDate = LocalDate.now().plusDays(BORROW_DAYS.get(media.getMediaType()));

        mediaDAO.updateAvailability(media.getId(), false);
        media.setAvailable(false);

        int recordId = borrowRecordDAO.insert(
                borrower.getId(),
                media.getId(),
                media.getMediaType(),
                media.getTitle(),
                LocalDate.now(),
                dueDate
        );

        if (recordId != -1) {
            MediaRecord record = new MediaRecord(recordId, media, dueDate);
            List<MediaRecord> borrowed = borrower.getBorrowedMedia();
            borrowed.add(record);
            borrower.setBorrowedMedia(borrowed);
            System.out.println("Successfully borrowed '" + media.getTitle() + "'. Due date: " + dueDate);
            return true;
        }
        return false;
    }

    /**
     * Handles the return process of a media item, calculates overdue fines if any,
     * and updates the borrower's fine balance.
     *
     * @param borrower The borrower returning the media.
     * @param media    The media item being returned.
     * @return true if successfully returned; false if the user did not borrow the item.
     */
    public boolean returnMedia(Borrower borrower, Media media) {
        for (MediaRecord record : borrower.getBorrowedMedia()) {
            if (record.getMedia().getId() == media.getId()) {
                mediaDAO.updateAvailability(media.getId(), true);
                media.setAvailable(true);

                double mediaFine = 0.0;
                if (record.isOverdue()) {
                    long overdueDays = record.getOverdueDays();
                    mediaFine = overdueDays * FINE_PER_DAY.get(media.getMediaType());
                    System.out.println("Media is " + overdueDays + " days overdue. Fine: " + mediaFine);
                }

                borrowRecordDAO.markAsReturned(record.getRecordId(), LocalDate.now(), mediaFine);

                if (mediaFine > 0) {
                    fineDAO.addFine(borrower.getId(), mediaFine);
                    double newBalance = fineDAO.getFineBalance(borrower.getId());
                    borrower.setFineBalance(newBalance);
                    System.out.println("Fine added: " + mediaFine);
                }

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

    /**
     * Retrieves a list of users who currently have overdue books.
     *
     * @return A list containing user and overdue book count information.
     */
    public List<UserWithOverdueBooks> getUsersWithOverdueBooks() {
        return borrowRecordDAO.getUsersWithOverdueBooks();
    }

    /**
     * Returns the allowed number of borrow days for a given media type.
     *
     * @param mediaType The type of media (e.g., "book", "cd").
     * @return Number of days allowed for borrowing.
     */
    public static int getBorrowDays(String mediaType) {
        return BORROW_DAYS.getOrDefault(mediaType, 0);
    }

    /**
     * Returns the fine per day for a given media type.
     *
     * @param mediaType The media type.
     * @return The daily fine amount.
     */
    public static double getFinePerDay(String mediaType) {
        return FINE_PER_DAY.getOrDefault(mediaType, 0.0);
    }
}
