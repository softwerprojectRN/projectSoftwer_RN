package domain;

public class User {

    private String username;
    private String password;

    private boolean loggedIn;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.loggedIn = false;

    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean login(String username, String password) {
        if(this.username.equals(username) && this.password.equals(password)){
            this.loggedIn = true;
            System.out.println(" Login successful, welcome " + username + "!");
            return  true;
        }
        else{
            System.out.println("Invalid username or password.");

            return false;
        }
    }
    public void logout() {
        if(this.loggedIn){
            this.loggedIn = false;
            System.out.println("Logged out successfully.");
        }
        else{
            System.out.println("You are not logged in yet.");

        }
    }
}
