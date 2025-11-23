package model;

public class Admin extends User {

    public Admin(int id, String username, String passwordHash, String salt) {
        super(id, username, passwordHash, salt);
    }

    public void showAdminInfo() {
        System.out.println("Admin username: " + getUsername());
    }
}