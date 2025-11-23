package model;

public class CD extends Media {
    private String artist;
    private String genre;
    private int duration;

    public CD(int id, String title, String artist, String genre, int duration, boolean isAvailable) {
        super(id, title, isAvailable, "cd");
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
    }

    public String getArtist() { return artist; }
    public String getGenre() { return genre; }
    public int getDuration() { return duration; }

    @Override
    public String toString() {
        return super.toString() + ", Artist: '" + artist + "', Genre: " + genre + ", Duration: " + duration + " min";
    }
}