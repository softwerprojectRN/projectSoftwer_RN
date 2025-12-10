package dao;

import model.CD;
import java.sql.*;
import java.util.List;

public class CDDAO extends BaseDAO {
    private static final String BASE_QUERY =
            "SELECT m.id, m.title, m.available, c.artist, c.genre, c.duration " +
                    "FROM media m JOIN cds c ON m.id = c.id ";

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

    public CD findById(int id) {
        return findOne(BASE_QUERY + "WHERE m.id = ?", this::mapCD, id);
    }

    public int insert(int mediaId, String artist, String genre, int duration) {
        return executeInsert("INSERT INTO cds (id, artist, genre, duration) VALUES (?, ?, ?, ?)",
                mediaId, artist, genre, duration);
    }

    public List<CD> findAll() {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'cd'", this::mapCD);
    }

    public List<CD> searchByTitle(String title) {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'cd' AND m.title LIKE ?",
                this::mapCD, "%" + title + "%");
    }

    public List<CD> searchByArtist(String artist) {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'cd' AND c.artist LIKE ?",
                this::mapCD, "%" + artist + "%");
    }

    public List<CD> searchByGenre(String genre) {
        return findMany(BASE_QUERY + "WHERE m.media_type = 'cd' AND c.genre LIKE ?",
                this::mapCD, "%" + genre + "%");
    }

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