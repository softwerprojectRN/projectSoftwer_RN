package domain;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CD extends Media {
    private String artist;
    private String genre;
    private int duration; // بالدقائق

    // كتلة static لإنشاء جدول الأقراص المدمجة
    static {
        Connection conn = connect();
        String sql = "CREATE TABLE IF NOT EXISTS cds (\n"
                + " id integer PRIMARY KEY,\n"
                + " artist text NOT NULL,\n"
                + " genre text,\n"
                + " duration integer,\n"
                + " FOREIGN KEY (id) REFERENCES media(id) ON DELETE CASCADE\n"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("The CDs table has been created successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Constructor
    public CD(int id, String title, String artist, String genre, int duration, boolean isAvailable) {
        super(id, title, isAvailable, "cd");
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
    }

    // دالة static لإضافة قرص مدمج جديد
    public static CD addCD(String title, String artist, String genre, int duration) {
        Connection conn = connect();

        // إدراج في جدول media أولاً
        String mediaSql = "INSERT INTO media (title, media_type, available) VALUES (?, 'cd', 1)";
        try (PreparedStatement pstmt = conn.prepareStatement(mediaSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, title);
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int newId = generatedKeys.getInt(1);

                // ثم إدراج في جدول cds
                String cdSql = "INSERT INTO cds (id, artist, genre, duration) VALUES (?, ?, ?, ?)";
                try (PreparedStatement cdStmt = conn.prepareStatement(cdSql)) {
                    cdStmt.setInt(1, newId);
                    cdStmt.setString(2, artist);
                    cdStmt.setString(3, genre);
                    cdStmt.setInt(4, duration);
                    cdStmt.executeUpdate();

                    System.out.println("CD added successfully: " + title);
                    return new CD(newId, title, artist, genre, duration, true);
                }
            } else {
                System.out.println("Error retrieving media ID.");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Error adding CD: " + e.getMessage());
            return null;
        }
    }

    // دالة static للحصول على جميع الأقراص المدمجة
    public static List<CD> getAllCDs() {
        List<CD> cds = new ArrayList<>();
        Connection conn = connect();
        String sql = "SELECT m.id, m.title, m.available, c.artist, c.genre, c.duration " +
                "FROM media m JOIN cds c ON m.id = c.id WHERE m.media_type = 'cd'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String artist = rs.getString("artist");
                String genre = rs.getString("genre");
                int duration = rs.getInt("duration");
                boolean available = rs.getInt("available") == 1;
                cds.add(new CD(id, title, artist, genre, duration, available));
            }
            System.out.println("Retrieved " + cds.size() + " CDs from database.");
        } catch (SQLException e) {
            System.out.println("Error fetching CDs: " + e.getMessage());
        }
        return cds;
    }

    // Getters
    public String getArtist() { return artist; }
    public String getGenre() { return genre; }
    public int getDuration() { return duration; }

    @Override
    public String toString() {
        return super.toString() + ", Artist: '" + artist + "', Genre: " + genre + ", Duration: " + duration + " min";
    }
}