package tk.internet.praktikum.foursquare.api.bean;

public class TextComment extends Comment {

    private String textID;

    public TextComment(User author, String text, String textID){
        super(author, text);
        this.textID = textID;
    }

    public String getTexteID() {
        return textID;
    }

    public void setTextID(String textID) {
        this.textID = textID;
    }

}
