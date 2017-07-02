package tk.internet.praktikum.foursquare.api.notification;

public abstract class NotificationBase {

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
