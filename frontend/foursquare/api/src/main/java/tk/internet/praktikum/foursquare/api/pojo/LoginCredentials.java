package tk.internet.praktikum.foursquare.api.pojo;

public class LoginCredentials {

    public String name;

    public String password;

    public String email;

    public LoginCredentials(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public LoginCredentials(String name, String email, String password) {
        this.name = name;
        this.password = password;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
