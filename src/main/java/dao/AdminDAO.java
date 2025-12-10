package dao;

import model.Admin;
import java.sql.*;

public class AdminDAO extends BaseDAO {

    public void initializeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS admins (\n" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                " username TEXT NOT NULL UNIQUE,\n" +
                " password_hash TEXT NOT NULL,\n" +
                " salt TEXT NOT NULL\n" +
                ");";
        createTable(sql, "Admins");
    }

    public Admin findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, salt FROM admins WHERE username = ?";
        return findOne(sql, this::mapAdmin, username);
    }

    public boolean insert(String username, String passwordHash, String salt) {
        String sql = "INSERT INTO admins (username, password_hash, salt) VALUES (?, ?, ?)";
        return executeInsert(sql, username, passwordHash, salt) > 0;
    }

    private Admin mapAdmin(ResultSet rs) throws SQLException {
        return new Admin(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("salt")
        );
    }
}