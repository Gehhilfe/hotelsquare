package tk.internet.praktikum.foursquare.api.beans;

import java.util.List;
import java.io.Serializable;
/**
 * Created by robert on 14.05.2017.
 */

public class User implements Serializable
{
    private String name;
    private String email;
    private String password;
    private String address;


    List<UserDTO> friends;

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

    public List<UserDTO> getFriends() {
        return friends;
    }

    public void setFriends(List<UserDTO> friends) {
        this.friends = friends;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public void addFriend(UserDTO friend){
         friends.add(friend);
    }
}
