package domain;


public class Admin extends User {

    @Override
    public String getUsername() {
        return super.getUsername();
    }

    @Override
    public String getPassword() {
        return super.getPassword();
    }


    public Admin(String username, String password) {
        super(username, password);
    }


    public void showAdminInfo() {
        System.out.println("Admin username: " + getUsername());
    }
}
