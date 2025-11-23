package model;

public class Media {
    protected int id;
    protected String title;
    protected boolean isAvailable;
    protected String mediaType;

    public Media(int id, String title, boolean isAvailable, String mediaType) {
        this.id = id;
        this.title = title;
        this.isAvailable = isAvailable;
        this.mediaType = mediaType;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public boolean isAvailable() { return isAvailable; }
    public String getMediaType() { return mediaType; }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    @Override
    public String toString() {
        String available = isAvailable ? "Yes" : "No";
        return "ID: " + id + ", Title: '" + title + "', Type: " + mediaType + ", Available: " + available;
    }
}