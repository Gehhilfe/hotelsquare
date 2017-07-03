package tk.internet.praktikum.foursquare.api.bean;

import java.util.List;

public class VenueSearchResult {

    private Location location;
    private String locationName;
    private List<Venue> results;

    public VenueSearchResult() {};

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public List<Venue> getResults() {
        return results;
    }

    public void setResults(List<Venue> results) {
        this.results = results;
    }
}
