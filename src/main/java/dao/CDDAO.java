package dao;

import model.CD;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CDDAO {

    public void initializeTable() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String sql = "CREATE TABLE IF NOT EXISTS cds (\n" +
                "  id INTEGER PRIMARY KEY,\n" +
                "  artist TEXT NOT NULL,\n" +
                "  genre TEXT,\n" +
                "  duration INTEGER,\n" +
                "  FOREIGN KEY (id) REFERENCES media(id) ON DELETE CASCADE\n" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("CDs table created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating CDs table: " + e.getMessage());
        }
    }

    public CD findById(int id) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        String sql = "SELECT m.id, m.title, m.available, c.artist, c.genre, c.duration " +
                "FROM media m JOIN cds c ON m.id = c.id WHERE m.id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new CD(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("genre"),
                        rs.getInt("duration"),
                        rs.getInt("available") == 1
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding CD: " + e.getMessage());
        }
        return null;
    }

    public int insert(int mediaId, String artist, String genre, int duration) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return -1;

        String sql = "INSERT INTO cds (id, artist, genre, duration) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mediaId);
            pstmt.setString(2, artist);
            pstmt.setString(3, genre);
            pstmt.setInt(4, duration);
            pstmt.executeUpdate();
            return mediaId;
        } catch (SQLException e) {
            System.err.println("Error inserting CD: " + e.getMessage());
            return -1;
        }
    }

    public List<CD> findAll() {
        List<CD> cds = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return cds;

        String sql = "SELECT m.id, m.title, m.available, c.artist, c.genre, c.duration " +
                "FROM media m JOIN cds c ON m.id = c.id WHERE m.media_type = 'cd'";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cds.add(new CD(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("genre"),
                        rs.getInt("duration"),
                        rs.getInt("available") == 1
                ));
            }
            System.out.println("Retrieved " + cds.size() + " CDs from database.");
        } catch (SQLException e) {
            System.err.println("Error fetching CDs: " + e.getMessage());
        }
        return cds;
    }

    public List<CD> searchByTitle(String title) {
        List<CD> cds = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return cds;

        String sql = "SELECT m.id, m.title, m.available, c.artist, c.genre, c.duration " +
                "FROM media m JOIN cds c ON m.id = c.id " +
                "WHERE m.media_type = 'cd' AND m.title LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + title + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                cds.add(new CD(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("genre"),
                        rs.getInt("duration"),
                        rs.getInt("available") == 1
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error searching CDs by title: " + e.getMessage());
        }
        return cds;
    }

    public List<CD> searchByArtist(String artist) {
        List<CD> cds = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return cds;

        String sql = "SELECT m.id, m.title, m.available, c.artist, c.genre, c.duration " +
                "FROM media m JOIN cds c ON m.id = c.id " +
                "WHERE m.media_type = 'cd' AND c.artist LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + artist + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                cds.add(new CD(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("genre"),
                        rs.getInt("duration"),
                        rs.getInt("available") == 1
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error searching CDs by artist: " + e.getMessage());
        }
        return cds;
    }

    public List<CD> searchByGenre(String genre) {
        List<CD> cds = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return cds;

        String sql = "SELECT m.id, m.title, m.available, c.artist, c.genre, c.duration " +
                "FROM media m JOIN cds c ON m.id = c.id " +
                "WHERE m.media_type = 'cd' AND c.genre LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + genre + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                cds.add(new CD(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("genre"),
                        rs.getInt("duration"),
                        rs.getInt("available") == 1
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error searching CDs by genre: " + e.getMessage());
        }
        return cds;
    }
}