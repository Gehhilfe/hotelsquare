package tk.internet.praktikum.foursquare.api.bean;

/**
 * Created by Tim Burkert on 09/08/2017.
 */

public class PasswordResetInformation {
    private String name;
    private String email;

    PasswordResetInformation() {}
    PasswordResetInformation(String name, String email) {
        this.name = name;
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
}
