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
 * such as books and CDs.
 *
 * <p>This class handles validation rules, fine calculation, overdue tracking,
 * and coordinates operations with {@link BorrowRecordDAO}, {@link FineDAO},
 * and {@link MediaDAO}.</p>
 *
 * <p>Borrowing rules enforced:</p>
 * <ul>
 *     <li>Users must be logged in to borrow media</li>
 *     <li>Users must have no outstanding fines</li>
 *     <li>Users must return overdue items before borrowing more</li>
 *     <li>Media must be available to borrow</li>
 * </ul>
 *
 * @version 1.1
 */
public class BorrowingService {

    /** Map storing allowed borrowing days per media type. */
    private static final Map<String, Integer> BORROW_DAYS = new HashMap<>();

    /** Map storing fine-per-day policies per media type. */
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
     * Constructs a new {@code BorrowingService} and initializes the required DAOs.
     * <p>
     * Ensures that the necessary database tables are initialized for borrowing
     * records and fines tracking.
     * </p>
     */
    public BorrowingService() {
        this.borrowRecordDAO = new BorrowRecordDAO();
        this.fineDAO = new FineDAO();
        this.mediaDAO = new MediaDAO();
        this.borrowRecordDAO.initializeTable();
        this.fineDAO.initializeTable();
    }

    /**
     * Allows a borrower to borrow a media item if all rules are satisfied.
     *
     * @param borrower The borrower requesting to borrow media
     * @param media    The media item being borrowed
     * @return {@code true} if borrowing is successful; {@code false} otherwise
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
     * Handles the return of a borrowed media item, calculates overdue fines,
     * updates borrower's fine balance, and marks the item as returned.
     *
     * @param borrower The borrower returning the media
     * @param media    The media item being returned
     * @return {@code true} if the return operation succeeds; {@code false} if the
     *         borrower did not borrow this media
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
     * Retrieves a list of users with currently overdue media items.
     *
     * @return a list of {@link UserWithOverdueBooks} containing user information
     *         and overdue counts
     */
    public List<UserWithOverdueBooks> getUsersWithOverdueBooks() {
        return borrowRecordDAO.getUsersWithOverdueBooks();
    }

    /**
     * Returns the allowed number of borrowing days for a specific media type.
     *
     * @param mediaType The media type (e.g., "book", "cd")
     * @return Number of days allowed for borrowing; 0 if unknown media type
     */
    public static int getBorrowDays(String mediaType) {
        return BORROW_DAYS.getOrDefault(mediaType, 0);
    }

    /**
     * Returns the daily fine amount for a specific media type.
     *
     * @param mediaType The media type
     * @return The fine per day; 0.0 if unknown media type
     */
    public static double getFinePerDay(String mediaType) {
        return FINE_PER_DAY.getOrDefault(mediaType, 0.0);
    }
}