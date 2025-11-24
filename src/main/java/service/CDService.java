package service;

import dao.CDDAO;
import dao.MediaDAO;
import model.CD;

import java.util.List;

/**
 * Service class responsible for managing CD-related operations such as adding,
 * searching, and retrieving CDs from the database.
 *
 * @author Library Management System
 * @version 1.0
 */
public class CDService {

    /** DAO for CD-specific operations */
    private final CDDAO cdDAO;

    /** DAO for shared media operations */
    private final MediaDAO mediaDAO;

    /**
     * Constructs a new {@code CDService} and initializes required tables.
     */
    public CDService() {
        this.cdDAO = new CDDAO();
        this.mediaDAO = new MediaDAO();
        this.cdDAO.initializeTable();
        this.mediaDAO.initializeTable();
    }

    /**
     * Adds a new CD to the system after validating the input.
     *
     * @param title    the title of the CD
     * @param artist   the artist of the CD
     * @param genre    the music genre (maybe empty)
     * @param duration the duration of the CD in minutes (must be positive)
     * @return a newly created {@link CD} object if successful, otherwise {@code null}
     */
    public CD addCD(String title, String artist, String genre, int duration) {
        // Validate input
        if (title == null || title.trim().isEmpty() ||
                artist == null || artist.trim().isEmpty()) {
            System.err.println("Error: Title and artist cannot be empty");
            return null;
        }

        if (duration <= 0) {
            System.err.println("Error: Duration must be positive");
            return null;
        }

        int mediaId = mediaDAO.insert(title.trim(), "cd");
        if (mediaId == -1) {
            System.err.println("Error: Failed to create media record");
            return null;
        }

        int cdId = cdDAO.insert(mediaId, artist.trim(), genre != null ? genre.trim() : "", duration);
        if (cdId != -1) {
            System.out.println("CD added successfully: " + title);
            return new CD(cdId, title, artist, genre, duration, true);
        }

        return null;
    }

    /**
     * Retrieves all CDs stored in the database.
     *
     * @return a list of {@link CD} objects
     */
    public List<CD> getAllCDs() {
        return cdDAO.findAll();
    }

    /**
     * Searches for CDs based on user-provided search term and search type.
     *
     * @param searchTerm the query to search for
     * @param searchType the field to search by (title, artist, or genre)
     * @return a list of matching {@link CD} objects
     */
    public List<CD> searchCDs(String searchTerm, String searchType) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            System.out.println("Search term cannot be empty");
            return List.of();
        }

        switch (searchType.toLowerCase()) {
            case "title":
                return cdDAO.searchByTitle(searchTerm);
            case "artist":
                return cdDAO.searchByArtist(searchTerm);
            case "genre":
                return cdDAO.searchByGenre(searchTerm);
            default:
                System.out.println("Invalid search type. Using title search.");
                return cdDAO.searchByTitle(searchTerm);
        }
    }

    /**
     * Finds a CD by its unique identifier.
     *
     * @param id the CD's ID
     * @return the matching {@link CD} or {@code null} if not found
     */
    public CD findById(int id) {
        return cdDAO.findById(id);
    }
}
