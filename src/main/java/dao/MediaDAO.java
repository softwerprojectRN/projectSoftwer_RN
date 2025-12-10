package dao;

import java.sql.*;

/**
 * {@code MediaDAO} is a Data Access Object (DAO) class responsible for
 * managing database operations related to the {@code media} table.
 * It extends {@link BaseDAO} to reuse common database operations such as
 * table creation, query execution, and result mapping.
 *
 * <p>This class provides methods to:</p>
 * <ul>
 *     <li>Initialize the "media" table.</li>
 *     <li>Insert new media records.</li>
 *     <li>Update the availability status of media items.</li>
 * </ul>
 *
 * <p>The "media" table stores information about media items such as
 * books, CDs, or other library resources, along with their availability status.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * MediaDAO mediaDAO = new MediaDAO();
 * mediaDAO.initializeTable();
 * int mediaId = mediaDAO.insert("Java Programming", "book");
 * mediaDAO.updateAvailability(mediaId, false);
 * }
 * </pre>
 *
 *
 * @author Library
 * @version 1.1
 *
 */
public class MediaDAO extends BaseDAO {

    /**
     * Initializes the "media" table in the database.
     * The table includes an auto-increment primary key, title, media type, and availability.
     * If the table already exists, no changes are made.
     */
    public void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS media (\n" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                " title TEXT NOT NULL,\n" +
                " media_type TEXT NOT NULL,\n" +
                " available INTEGER NOT NULL DEFAULT 1\n" +
                ");";
        createTable(sql, "Media");
    }

    /**
     * Inserts a new media record into the "media" table.
     *
     * @param title     the title of the media item
     * @param mediaType the type of media (e.g., "book", "cd")
     * @return the generated ID of the inserted media; -1 if insertion fails
     */
    public int insert(String title, String mediaType) {
        return executeInsert("INSERT INTO media (title, media_type, available) VALUES (?, ?, 1)",
                title, mediaType);
    }

    /**
     * Updates the availability status of a media item.
     *
     * @param mediaId   the ID of the media item
     * @param available {@code true} if the media is available; {@code false} otherwise
     * @return {@code true} if the update was successful; {@code false} otherwise
     */
    public boolean updateAvailability(int mediaId, boolean available) {
        return executeUpdate("UPDATE media SET available = ? WHERE id = ?",
                available ? 1 : 0, mediaId);
    }
}