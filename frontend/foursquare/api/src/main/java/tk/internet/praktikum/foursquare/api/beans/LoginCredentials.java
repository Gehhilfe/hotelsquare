package tk.internet.praktikum.foursquare.api.beans;

/**
 * Created by robert on 14.05.2017.
 */

public class LoginCredentials {

    public String name;

    public String password;

    public LoginCredentials(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
