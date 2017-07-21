package tk.internet.praktikum.foursquare.api.bean;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public abstract class Comment {

    @SerializedName("_id")
    private String id;

    private User author;
   /* private String text;*/
    private int likes_count;
    private int dislikes_count;
    private Date date;

    public Comment() {}

    public void setAuthor(User author) {
        this.author = author;
    }
    public User getAuthor() {
        return author;
    }

    public int getLikes() {
        return likes_count;
    }

    public int getDislikes() {
        return dislikes_count;
    }

    public Date getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
