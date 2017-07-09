package tk.internet.praktikum.foursquare.api.bean;

public class ImageComment extends Comment {

    private String imageID;

    public ImageComment(User author, String text, String imageID){
        super(author, text);
        this.imageID = imageID;
    }

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

}
