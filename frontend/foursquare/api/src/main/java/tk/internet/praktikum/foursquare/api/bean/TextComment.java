package tk.internet.praktikum.foursquare.api.bean;

public class TextComment extends Comment {

    private String text;

    public TextComment(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
