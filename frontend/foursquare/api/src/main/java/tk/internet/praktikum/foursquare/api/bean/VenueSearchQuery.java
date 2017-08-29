package tk.internet.praktikum.foursquare.api.bean;


public class VenueSearchQuery {

    private String keyword;
    private Location location;
    private String locationName;
    private int radius;
    private int price = 0;
    private boolean only_open = false;

    public VenueSearchQuery(String keyword, double longitude, double latitude) {
        this.keyword = keyword;
        this.location = new Location(longitude, latitude);
    }

    public VenueSearchQuery(String keyword, String locationName) {
        this.keyword = keyword;
        this.locationName = locationName;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setOnlyOpen(boolean only_open) {
        this.only_open = only_open;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
