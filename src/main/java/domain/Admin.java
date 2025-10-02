

package domain;

public class Admin {
    private String username;
    private String password;
    private boolean loggedIn = false;

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean login(String inputUsername, String inputPassword) {
        if(this.username.equals(inputUsername) && this.password.equals(inputPassword)) {
            loggedIn = true;
            System.out.println("Login successful! Welcome, " + username + ".");
            return true;
        }
        return false;
    }

    public void logout() {
        if(loggedIn) {
            loggedIn = false;
            System.out.println("You have been logged out.");
        } else {
            System.out.println("You are not logged in.");
        }
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void manageBooks() {
        if(loggedIn) {
            System.out.println("Accessing book management...");
        } else {
            System.out.println("Error: You must login first to perform this action.");
        }
    }

    public String getUsername() {
        return username;
    }
}
