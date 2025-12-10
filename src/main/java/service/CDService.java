package service;

import dao.CDDAO;
import dao.MediaDAO;
import model.CD;

import java.util.List;

/**
 * Service class responsible for managing CD-related operations such as adding,
 * retrieving, and searching CDs in the system.
 *
 * <p>This class coordinates between the {@link CDDAO} and {@link MediaDAO}
 * to persist CD and media records while providing business logic for validation
 * and search functionality.</p>
 *
 * @version 1.1
 */

public class CDService {

    /** DAO for CD-specific database operations */
    private final CDDAO cdDAO;

    /** DAO for shared media database operations */
    private final MediaDAO mediaDAO;

    /**
     * Constructs a new {@code CDService} and initializes the underlying tables.
     * <p>
     * Upon creation, both CD and media tables are initialized if they do not exist.
     * </p>
     */
    public CDService() {
        this.cdDAO = new CDDAO();
        this.mediaDAO = new MediaDAO();
        this.cdDAO.initializeTable();
        this.mediaDAO.initializeTable();
    }


    /**
     * Adds a new CD to the system after validating input parameters.
     *
     * <p>Validation rules:</p>
     * <ul>
     *     <li>Title and artist must not be null or empty</li>
     *     <li>Duration must be positive</li>
     * </ul>
     *
     * @param title    the title of the CD
     * @param artist   the performing artist
     * @param genre    the music genre (can be empty)
     * @param duration the duration in minutes (must be positive)
     * @return a newly created {@link CD} object if successfully added,
     *         otherwise {@code null}
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
     * @return a list of all {@link CD} objects
     */
    public List<CD> getAllCDs() {
        return cdDAO.findAll();
    }

    /**
     * Searches for CDs using a search term and specified search type.
     *
     * <p>Supported search types: "title", "artist", "genre".
     * If an invalid search type is provided, it defaults to "title".</p>
     *
     * @param searchTerm the query string to search for; must not be empty
     * @param searchType the field to search by ("title", "artist", "genre")
     * @return a list of {@link CD} objects matching the criteria; empty list if no matches
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
     * @param id the CD's database ID
     * @return the {@link CD} object if found, otherwise {@code null}
     */
    public CD findById(int id) {
        return cdDAO.findById(id);
    }
}