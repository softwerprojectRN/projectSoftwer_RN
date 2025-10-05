package domain;
//مسؤوليته: فقط تخزين البيانات. ما الو دخل بأي عملية في المكتبة
public class Admin {
    private String username;
    private String password;

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}