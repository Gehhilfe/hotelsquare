package tk.internet.praktikum.foursquare.api.NotificationTypes;

import android.location.Location;

import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.Venue;

public class LocationNotification extends Notification {

    private User user;
    private Location location;

    public LocationNotification(String title, String type, String content, User user, Venue venue)
    {
        super(title, type, content);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location lcoation) {
        this.location = location;
    }

}
