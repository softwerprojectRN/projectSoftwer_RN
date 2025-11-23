package model;

public class UserWithOverdueBooks {
    private int userId;
    private String username;
    private int overdueCount;

    public UserWithOverdueBooks(int userId, String username, int overdueCount) {
        this.userId = userId;
        this.username = username;
        this.overdueCount = overdueCount;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public int getOverdueCount() { return overdueCount; }
}