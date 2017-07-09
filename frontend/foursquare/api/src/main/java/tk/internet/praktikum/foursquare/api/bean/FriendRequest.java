package tk.internet.praktikum.foursquare.api.bean;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class FriendRequest {

    private String sender;

    @SerializedName("created_at")
    private Date createdAt;

    public String getSenderID() {
        return sender;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
