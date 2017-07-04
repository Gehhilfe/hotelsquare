package tk.internet.praktikum.foursquare.api.notification;

import tk.internet.praktikum.foursquare.api.bean.User;

public class ChatNotification extends NotificationBase {

    private User user;

    public ChatNotification(User user)
    {
        super();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
