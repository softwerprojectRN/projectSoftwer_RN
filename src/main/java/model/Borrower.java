package model;

import java.util.ArrayList;
import java.util.List;

public class Borrower extends User {
    private List<MediaRecord> borrowedMedia;
    private double fineBalance;

    public Borrower(int id, String username, String passwordHash, String salt) {
        super(id, username, passwordHash, salt);
        this.borrowedMedia = new ArrayList<>();
        this.fineBalance = 0.0;
    }

    public List<MediaRecord> getBorrowedMedia() {
        return new ArrayList<>(borrowedMedia);
    }

    public void setBorrowedMedia(List<MediaRecord> borrowedMedia) {
        this.borrowedMedia = borrowedMedia;
    }

    public double getFineBalance() {
        return fineBalance;
    }

    public void setFineBalance(double fineBalance) {
        this.fineBalance = fineBalance;
    }

    public List<MediaRecord> getOverdueMedia() {
        List<MediaRecord> overdue = new ArrayList<>();
        for (MediaRecord record : borrowedMedia) {
            if (record.isOverdue()) {
                overdue.add(record);
            }
        }
        return overdue;
    }
}