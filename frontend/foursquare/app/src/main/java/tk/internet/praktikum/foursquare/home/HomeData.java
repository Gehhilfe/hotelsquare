package tk.internet.praktikum.foursquare.home;

import tk.internet.praktikum.foursquare.api.bean.FriendRequest;

public class HomeData {
    private FriendRequest friendRequest;
    private HomeType type;

    public HomeData(FriendRequest friendRequests, HomeType type) {
        this.friendRequest = friendRequests;
        this.type = type;
    }

    public FriendRequest getFriendRequest() {
        return friendRequest;
    }

    public HomeType getType() {
        return type;
    }
}
