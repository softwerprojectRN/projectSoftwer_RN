package model;

import java.time.LocalDate;

public class MediaRecord {
    private int recordId;
    private Media media;
    private LocalDate dueDate;

    public MediaRecord(int recordId, Media media, LocalDate dueDate) {
        this.recordId = recordId;
        this.media = media;
        this.dueDate = dueDate;
    }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public Media getMedia() { return media; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate);
    }

    public long getOverdueDays() {
        if (!isOverdue()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }
}