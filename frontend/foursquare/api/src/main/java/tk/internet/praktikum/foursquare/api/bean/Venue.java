package tk.internet.praktikum.foursquare.api.bean;

import com.google.gson.annotations.SerializedName;

import java.io.InputStream;
import java.util.List;

public class Venue {

    @SerializedName("_id")
    private String id;
    private String name;
    private String place_id;
    private String reference;
    private List<String> types;
    private Location location;
    private int rating;
    private int utc_offset;

    public Venue() {}

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
