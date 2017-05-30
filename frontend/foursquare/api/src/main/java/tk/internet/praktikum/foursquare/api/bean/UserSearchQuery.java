package tk.internet.praktikum.foursquare.api.bean;

public class UserSearchQuery {

    private String name;
    private Gender gender;

    public UserSearchQuery(String name) {
        this.name = name;
    }

    public UserSearchQuery(String name, Gender gender) {
        this(name);
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }
}
