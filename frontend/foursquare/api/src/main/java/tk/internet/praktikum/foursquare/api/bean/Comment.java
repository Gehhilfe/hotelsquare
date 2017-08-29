package tk.internet.praktikum.foursquare.api.bean;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public abstract class Comment {

    @SerializedName("_id")
    private String id;

    private User author;
   /* private String text;*/
    private int rating;
    private Date created_at;

    public Comment() {}

    public void setAuthor(User author) {
        this.author = author;
    }


    public void setDate(Date date) {
        this.created_at = date;
    }

    public User getAuthor() {
        return author;
    }

    public Date getDate() {
        return created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }
}
