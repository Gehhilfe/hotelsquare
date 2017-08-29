package tk.internet.praktikum.foursquare.api.bean;

import java.util.List;


public class FriendListResponse {
    private int count;
    private List<User> friends;

    public List<User> getFriends() {
        return friends;
    }

    public int getFriendCount() {
        return count;
    }
}
