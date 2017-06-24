package tk.internet.praktikum.foursquare.api.bean;


public class VenueSearchQuery {

    private String keyword;
    private long longitude;
    private long latitude;

    public VenueSearchQuery(String keyword, long latitude, long longitude) {
        this.keyword = keyword;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }
}
