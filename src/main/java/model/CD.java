package model;

/**
 * Represents a CD in the library system.
 *
 * <p>Extends {@link Media} with CD-specific attributes such as artist, genre,
 * and duration. This class is used to model audio CDs available in the library.</p>
 *
 * @author Library
 * @version 1.1
 */

public class CD extends Media {
    /** The artist of the CD */
    private String artist;

    /** The genre of the CD */
    private String genre;

    /** The duration of the CD in minutes */
    private int duration;

    /**
     * Constructs a CD object.
     *
     * @param id the CD's unique identifier
     * @param title the CD's title
     * @param artist the CD's artist
     * @param genre the CD's genre
     * @param duration the CD's duration in minutes
     * @param isAvailable the availability status
     */
    public CD(int id, String title, String artist, String genre, int duration, boolean isAvailable) {
        super(id, title, isAvailable, "cd");
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
    }

    /**
     * Gets the artist of the CD.
     *
     * @return the artist's name
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Gets the genre of the CD.
     *
     * @return the genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Gets the duration of the CD.
     *
     * @return the duration in minutes
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Returns a string representation of the CD.
     *
     * @return formatted string with CD details
     */
    @Override
    public String toString() {
        return super.toString() + ", Artist: '" + artist + "', Genre: " + genre +
                ", Duration: " + duration + " min";
    }
}