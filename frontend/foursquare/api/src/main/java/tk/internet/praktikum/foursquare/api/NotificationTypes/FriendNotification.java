package tk.internet.praktikum.foursquare.api.NotificationTypes;

import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.Venue;

public class FriendNotification extends Notification {

    private User user;
    private Venue venue;

    public FriendNotification(String title, String type, String content, User user, Venue venue)
    {
        super(title, type, content);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }
}
