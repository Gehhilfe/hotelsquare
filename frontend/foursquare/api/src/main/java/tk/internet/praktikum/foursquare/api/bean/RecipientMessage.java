package tk.internet.praktikum.foursquare.api.bean;

import java.util.List;

public class RecipientMessage extends Message {

    private List<String> recipients;

    public RecipientMessage() {};

    public RecipientMessage(List<String> recipients, String message) {
        super(message);
        this.recipients = recipients;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }
}
