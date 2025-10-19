package domain;

import java.sql.*;

public abstract class Media {
    protected int id;
    protected String title;
    protected boolean isAvailable;
    protected String mediaType; // "book" or "cd"

    // دالة للاتصال بالداتابيز
    public static Connection connect() {
        String url = "jdbc:sqlite:database.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    // كتلة static لإنشاء جدول المواد
    static {
        Connection conn = connect();
        String sql = "CREATE TABLE IF NOT EXISTS media (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " title text NOT NULL,\n"
                + " media_type text NOT NULL,\n"
                + " available integer NOT NULL DEFAULT 1\n"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("The media table has been created successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Constructor
    public Media(int id, String title, boolean isAvailable, String mediaType) {
        this.id = id;
        this.title = title;
        this.isAvailable = isAvailable;
        this.mediaType = mediaType;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public boolean isAvailable() { return isAvailable; }
    public String getMediaType() { return mediaType; }

    // تحديث حالة التوافر
    public void updateAvailability(boolean available) {
        if (id == 0) {
            System.out.println("Cannot update media without an ID.");
            return;
        }
        Connection conn = connect();
        String sql = "UPDATE media SET available = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, available ? 1 : 0);
            pstmt.setInt(2, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                this.isAvailable = available;
                System.out.println("Media status updated: " + title);
            } else {
                System.out.println("Media not found (ID not found).");
            }
        } catch (SQLException e) {
            System.out.println("Error updating media status: " + e.getMessage());
        }
    }

    public void borrow() {
        if (isAvailable) {
            updateAvailability(false);
        } else {
            System.out.println("This media is not available for borrowing.");
        }
    }

    public void returnMedia() {
        if (!isAvailable) {
            updateAvailability(true);
        } else {
            System.out.println("This media is already available.");
        }
    }


    @Override
    public String toString() {
        String available = isAvailable ? "Yes" : "No";
        return "ID: " + id + ", Title: '" + title + "', Type: " + mediaType + ", Available: " + available;
    }
}