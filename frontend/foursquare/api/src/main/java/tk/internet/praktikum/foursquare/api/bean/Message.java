package tk.internet.praktikum.foursquare.api.bean;

public class Message {

    String message;

    public Message(String message)
    {
        this.message = message;
    }

    void setMessage(String message)
    {
        this.message = message;
    }

    String getMessage()
    {
        return message;
    }
}
