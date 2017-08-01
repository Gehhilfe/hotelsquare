package tk.internet.praktikum.foursquare.api.bean;

import java.util.Date;

/**
 * Created by Tim Burkert on 01/08/2017.
 */
public class ChatMessage {

    private User sender;
    private String message;
    private Date date;

    public User getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }
}
