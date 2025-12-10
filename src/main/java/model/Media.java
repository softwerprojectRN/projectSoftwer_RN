package model;

/**
 * Represents a media item in the library system.
 *
 * <p>This is the base class for all types of media, such as {@link Book} and {@link CD}.
 * It tracks common attributes including ID, title, availability status, and media type.</p>
 *
 * @author Library
 * @version 1.1
 */
public class Media {
    /** The unique identifier for the media */
    protected int id;

    /** The title of the media */
    protected String title;

    /** The availability status of the media */
    protected boolean isAvailable;

    /** The type of media (book or cd) */
    protected String mediaType;

    /**
     * Constructs a Media object.
     *
     * @param id the media's unique identifier
     * @param title the media's title
     * @param isAvailable the availability status
     * @param mediaType the type of media
     */
    public Media(int id, String title, boolean isAvailable, String mediaType) {
        this.id = id;
        this.title = title;
        this.isAvailable = isAvailable;
        this.mediaType = mediaType;
    }

    /**
     * Gets the media ID.
     *
     * @return the media ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the media title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Checks if the media is available.
     *
     * @return true if available, false otherwise
     */
    public boolean isAvailable() {
        return isAvailable;
    }

    /**
     * Gets the media type.
     *
     * @return the media type (book or cd)
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Sets the availability status.
     *
     * @param available the new availability status
     */
    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    /** Returns a string representation of the media. */

    @Override
    public String toString() {
        String available = isAvailable ? "Yes" : "No";
        return "ID: " + id + ", Title: '" + title + "', Type: " + mediaType +
                ", Available: " + available;
    }
}