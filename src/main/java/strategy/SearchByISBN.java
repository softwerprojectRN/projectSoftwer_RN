package strategy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchByISBN implements SearchStrategy {
    @Override
    public List<Book> search(String searchTerm) {
        List<Book> results = new ArrayList<>();
        Connection conn = Media.connect();
        String sql = "SELECT m.id, m.title, m.available, b.author, b.isbn " +
                "FROM media m JOIN books b ON m.id = b.id " +
                "WHERE m.media_type = 'book' AND b.isbn LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String isbn = rs.getString("isbn");
                boolean available = rs.getInt("available") == 1;
                results.add(new Book(id, title, author, isbn, available));
            }
        } catch (SQLException e) {
            System.out.println("خطأ في البحث بالرقم المعياري: " + e.getMessage());
        }

        return results;
    }
}