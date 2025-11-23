package model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String salt;
    private boolean loggedIn;

    public User(int id, String username, String passwordHash, String salt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.loggedIn = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public boolean isLoggedIn() { return loggedIn; }
    public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }

    public void logout() {
        if (this.loggedIn) {
            this.loggedIn = false;
            System.out.println("Logged out successfully.");
        } else {
            System.out.println("You are not logged in yet.");
        }
    }
}