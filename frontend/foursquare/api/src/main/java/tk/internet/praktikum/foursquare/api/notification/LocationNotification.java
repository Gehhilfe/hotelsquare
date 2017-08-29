package tk.internet.praktikum.foursquare.api.notification;

import android.location.Location;

import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.Venue;

public class LocationNotification extends NotificationBase {

    private User user;
    private Location location;

    public LocationNotification(User user, Venue venue)
    {
        super();
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
