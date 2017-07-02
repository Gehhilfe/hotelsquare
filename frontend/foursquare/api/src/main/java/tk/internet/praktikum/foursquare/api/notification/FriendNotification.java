package tk.internet.praktikum.foursquare.api.notification;

import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.Venue;

public class FriendNotification extends NotificationBase {

    private User user;
    private Venue venue;

    public FriendNotification(User user, Venue venue)
    {
        super();
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
