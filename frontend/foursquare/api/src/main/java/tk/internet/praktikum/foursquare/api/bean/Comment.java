package tk.internet.praktikum.foursquare.api.bean;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Comment {

    @SerializedName("_id")
    private String id;
    private User author;
    private String text;
    private int likes;
    private int dislikes;
    private Date date;

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public Date getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Comment(User author, String text){
        this.author = author;
        this.text = text;
    }

}
