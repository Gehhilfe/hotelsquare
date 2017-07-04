package tk.internet.praktikum.foursquare.api.bean;


public class VenueSearchQuery {

    private String keyword;
    private Location location;
    private String locationName;
    private Integer radius;

    public VenueSearchQuery(String keyword, double longitude, double latitude) {
        this.keyword = keyword;
        this.location = new Location(longitude, latitude);
    }

    public VenueSearchQuery(String keyword, String locationName) {
        this.keyword = keyword;
        this.locationName = locationName;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
