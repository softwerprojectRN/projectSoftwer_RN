package model;

import java.time.LocalDate;

/**
 * Represents a record of a borrowed media item in the library system.
 *
 * <p>This class tracks the borrowed media, its due date, and provides methods
 * to check overdue status and calculate overdue days.</p>
 *
 * @author Library Management
 * @version 1.1
 */
public class MediaRecord {
    /** The unique identifier for this borrow record */
    private int recordId;

    /** The borrowed media item */
    private Media media;

    /** The due date for return */
    private LocalDate dueDate;

    /**
     * Constructs a new {@code MediaRecord}.
     *
     * @param recordId the record's unique identifier
     * @param media the borrowed media item
     * @param dueDate the due date for returning the media
     */
    public MediaRecord(int recordId, Media media, LocalDate dueDate) {
        this.recordId = recordId;
        this.media = media;
        this.dueDate = dueDate;
    }

    /**
     * Gets the record ID.
     *
     * @return the record ID
     */
    public int getRecordId() {
        return recordId;
    }

    /**
     * Sets the record ID.
     *
     * @param recordId the new record ID
     */
    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    /**
     * Gets the borrowed media.
     *
     * @return the Media object
     */
    public Media getMedia() {
        return media;
    }

    /**
     * Gets the due date.
     *
     * @return the due date
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Sets the due date.
     *
     * @param dueDate the new due date
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Checks if the media is overdue.
     *
     * @return true if overdue, false otherwise
     */
    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate);
    }

    /**
     * Calculates the number of days overdue.
     *
     * @return the number of overdue days, 0 if not overdue
     */
    public long getOverdueDays() {
        if (!isOverdue()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }
}