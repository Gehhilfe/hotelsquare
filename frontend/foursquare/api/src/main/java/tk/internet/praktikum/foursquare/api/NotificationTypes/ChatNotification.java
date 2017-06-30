package tk.internet.praktikum.foursquare.api.NotificationTypes;

import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.Venue;

public class ChatNotification extends Notification {

    private User user;

    public ChatNotification(String title, String type, String content, User user)
    {
        super(title, type, content);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
