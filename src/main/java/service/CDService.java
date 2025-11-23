package service;

import dao.CDDAO;
import dao.MediaDAO;
import model.CD;

import java.util.List;

public class CDService {
    private final CDDAO cdDAO;
    private final MediaDAO mediaDAO;

    public CDService() {
        this.cdDAO = new CDDAO();
        this.mediaDAO = new MediaDAO();
        this.cdDAO.initializeTable();
        this.mediaDAO.initializeTable();
    }

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

    public List<CD> getAllCDs() {
        return cdDAO.findAll();
    }

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

    public CD findById(int id) {
        return cdDAO.findById(id);
    }
}