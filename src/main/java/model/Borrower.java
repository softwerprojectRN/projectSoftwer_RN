package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a library borrower (patron) in the system.
 *
 * <p>Extends {@link User} to include borrowing-specific attributes such as
 * a list of borrowed media items and the borrower's fine balance.</p>
 *
 * <p>Provides methods to access borrowed media, set fine balances, and
 * retrieve overdue items.</p>
 *
 * @author Library
 * @version 1.1
 */
public class Borrower extends User {
    /** List of currently borrowed media items */
    private List<MediaRecord> borrowedMedia;

    /** The borrower's current fine balance */
    private double fineBalance;

    /**
     * Constructs a Borrower object.
     *
     * @param id the borrower's unique identifier
     * @param username the borrower's username
     * @param passwordHash the hashed password
     * @param salt the salt used for password hashing
     */
    public Borrower(int id, String username, String passwordHash, String salt) {
        super(id, username, passwordHash, salt);
        this.borrowedMedia = new ArrayList<>();
        this.fineBalance = 0.0;
    }

    /**
     * Gets a copy of the list of borrowed media.
     *
     * @return List of MediaRecord objects
     */
    public List<MediaRecord> getBorrowedMedia() {
        return new ArrayList<>(borrowedMedia);
    }

    /**
     * Sets the list of borrowed media.
     *
     * @param borrowedMedia the list of borrowed media records
     */
    public void setBorrowedMedia(List<MediaRecord> borrowedMedia) {
        this.borrowedMedia = borrowedMedia;
    }

    /**
     * Gets the current fine balance.
     *
     * @return the fine balance amount
     */
    public double getFineBalance() {
        return fineBalance;
    }

    /**
     * Sets the fine balance.
     *
     * @param fineBalance the new fine balance
     */
    public void setFineBalance(double fineBalance) {
        this.fineBalance = fineBalance;
    }

    /**
     * Returns all borrowed media items that are currently overdue.
     *
     * @return a list of overdue {@link MediaRecord} objects
     */
    public List<MediaRecord> getOverdueMedia() {
        List<MediaRecord> overdue = new ArrayList<>();

        for (MediaRecord mediaRecord : borrowedMedia) {
            if (mediaRecord.isOverdue()) {
                overdue.add(mediaRecord);
            }
        }
        return overdue;
    }

}