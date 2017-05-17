package tk.internet.praktikum.foursquare.api.beans;

import java.util.List;

/**
 * Created by truongtud on 17.05.2017.
 */
/*
this class is use for displaying profile
 */
public class UserDTO {
    String name;
    String email;
    String address;
    List<UserDTO> friends;

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

    public List<UserDTO> getFriends() {
        return friends;
    }

    public void setFriends(List<UserDTO> friends) {
        this.friends = friends;
    }
}
