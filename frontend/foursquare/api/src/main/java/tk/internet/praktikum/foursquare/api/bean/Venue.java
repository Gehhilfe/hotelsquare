package tk.internet.praktikum.foursquare.api.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Venue {

    @SerializedName("_id")
    private String id;
    private String name;
    private String place_id;
    private String reference;
    private String formatted_address;
    private boolean is_open;
    private String phone_number;
    private String website;
    private String vicinity;
    private List<String> types;
    private Location location;
    private int rating;
    private int utc_offset;
    private int check_ins_count;
    private List<Image> images;
    private List<CheckinInformation> top_check_ins;
    private List<CheckinInformation> last_check_ins;

    public Venue() {
    }

    public String getFormattedAddress() {
        return formatted_address;
    }

    public boolean isOpen() {
        return is_open;
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    public String getWebsite() {
        return website;
    }

    public String getVicinity() {
        return vicinity;
    }

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

    public int getCheckInCount() {
        return check_ins_count;
    }

    public List<CheckinInformation> getTopCheckins() {
        return top_check_ins;
    }

    public List<CheckinInformation> getLastCheckins() {
        return last_check_ins;
    }

    public List<Image> getImages() {
        return images;
    }
}
