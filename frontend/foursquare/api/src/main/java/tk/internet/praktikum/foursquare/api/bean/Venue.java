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
    private String phone_number;
    private String vicinity;
    private List<String> types;
    private Location location;
    private float rating;
    private int utc_offset;
    private int check_ins_count;
    private int price;
    private List<Image> images;
    private List<UserCheckinInformation> top_check_ins;
    private List<UserCheckinInformation> last_check_ins;
    private String website;
    private boolean is_open;


    public Venue() {
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public boolean is_open() {
        return is_open;
    }

    public void setIs_open(boolean is_open) {
        this.is_open = is_open;
    }


    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
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

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
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

    public List<UserCheckinInformation> getTopCheckins() {
        return top_check_ins;
    }

    public List<UserCheckinInformation> getLastCheckins() {
        return last_check_ins;
    }

    public List<Image> getImages() {
        return images;
    }

    public int getPrice() {
        return price;
    }
}
