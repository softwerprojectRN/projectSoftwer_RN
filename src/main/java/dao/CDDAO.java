package dao;

import model.CD;
import java.sql.*;
import java.util.List;

/**
 * {@code CDDAO} is a Data Access Object (DAO) class responsible for
 * managing database operations related to the {@link CD} model.
 * It extends {@link BaseDAO} to leverage common database operations such as
 * table creation, query execution, and result mapping.
 *
 * <p>This class provides methods to:</p>
 * <ul>
 *     <li>Initialize the "cds" table with a foreign key reference to "media".</li>
 *     <li>Insert new CD records.</li>
 *     <li>Find a CD by its ID.</li>
 *     <li>Retrieve all CDs.</li>
 *     <li>Search CDs by title, artist, or genre.</li>
 * </ul>
 *
 * <p>The class uses a {@code BASE_QUERY} to join the "media" and "cds" tables
 * to provide complete information about each CD.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * CDDAO cdDAO = new CDDAO();
 * cdDAO.initializeTable();
 * CD cd = cdDAO.findById(1);
 * List<CD> allCDs = cdDAO.findAll();
 * List<CD> searchResults = cdDAO.searchByArtist("Beatles");
 * }
 * </pre>
 *
 * @author Library
 * @version 1.1
 *
 */
public class CDDAO extends BaseDAO {
    /**
     * Base SQL query joining "media" and "cds" tables.
     * Used to simplify SELECT queries.
     */
    private static final String BASE_QUERY =
            "SELECT m.id, m.title, m.available, c.artist, c.genre, c.duration " +
                    "FROM media m JOIN cds c ON m.id = c.id ";

    /**
     * Initializes the "cds" table in the database.
     * The table has a foreign key referencing the "media" table.
     * If the table already exists, no changes are made.
     */
    public void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS cds (\n" +
                " id INTEGER PRIMARY KEY,\n" +
                " artist TEXT NOT NULL,\n" +
                " genre TEXT,\n" +
                " duration INTEGER,\n" +
                " FOREIGN KEY (id) REFERENCES media(id) ON DELETE CASCADE\n" +
                ");";
        createTable(sql, "CDs");
    }
    /**
     * Finds a {@link CD} by its ID.
     *
     * @param id the ID of the CD
     * @return the {@link CD} object if found; {@code null} otherwise
     */
    public CD findById(int id) {
        return findOne(BASE_QUERY + "WHERE m.id = ?", this::mapCD, id);
    }

    /**
     * Inserts a new CD into the "cds" table.
     *
     * @param mediaId  the ID of the corresponding media record
     * @param artist   the artist of the CD
     * @param genre    the genre of the CD
     * @param duration the duration of the CD in minutes
     * @return the generated key for the inserted CD; -1 if insertion fails
     */
    public int insert(int mediaId, String artist, String genre, int duration) {
        return executeInsert("INSERT INTO cds (id, artist, genre, duration) VALUES (?, ?, ?, ?)",
                mediaId, artist, genre, duration);
    }

    /**
     * Retrieves all CDs from the database.
     *
     * @return a list of {@link CD} objects
     */
    public List<CD> findAll() {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'cd'", this::mapCD);
    }

    /**
     * Searches for CDs whose title contains the specified string.
     *
     * @param title the title pattern to search for
     * @return a list of matching {@link CD} objects
     */
    public List<CD> searchByTitle(String title) {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'cd' AND m.title LIKE ?",
                this::mapCD, "%" + title + "%");
    }

    /**
     * Searches for CDs whose artist contains the specified string.
     *
     * @param artist the artist pattern to search for
     * @return a list of matching {@link CD} objects
     */
    public List<CD> searchByArtist(String artist) {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'cd' AND c.artist LIKE ?",
                this::mapCD, "%" + artist + "%");
    }

    /**
     * Searches for CDs whose genre contains the specified string.
     *
     * @param genre the genre pattern to search for
     * @return a list of matching {@link CD} objects
     */
    public List<CD> searchByGenre(String genre) {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'cd' AND c.genre LIKE ?",
                this::mapCD, "%" + genre + "%");
    }

    /**
     * Maps a {@link ResultSet} row to a {@link CD} object.
     *
     * @param rs the result set positioned at the current row
     * @return the mapped {@link CD} object
     * @throws SQLException if a database access error occurs
     */
    private CD mapCD(ResultSet rs) throws SQLException {
        return new CD(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("artist"),
                rs.getString("genre"),
                rs.getInt("duration"),
                rs.getInt("available") == 1
        );
    }
}