package tk.internet.praktikum.foursquare.api.bean;

public class VenueComment extends Comment {

    private String venueID;

    public VenueComment(User author, String text, String venueID){
        super(author, text);
        this.venueID = venueID;
    }

    public String getVenueID() {
        return venueID;
    }

    public void setVenueID(String venueID) {
        this.venueID = venueID;
    }
}
