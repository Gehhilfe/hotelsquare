package tk.internet.praktikum.foursquare.api.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class User implements Serializable {
    private String name;
    private String displayName;
    private String email;
    private String password;
    private Gender gender;
    private String city;
    private int age;
    private Location location;
    private boolean incognito;
    private List<VenueCheckinInformation> top_check_ins;
    private List<VenueCheckinInformation> last_check_ins;

    @SerializedName("update_at")
    private Date updatedAt;

    @SerializedName("friend_requests")
    private List<FriendRequest> friendRequests;
    private String _id;
    private Image avatar;

    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
    public String getId() {
        return _id;
    }

    public List<FriendRequest> getFriendRequests() {
        return friendRequests;
    }

    public Image getAvatar() {
        return avatar;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isIncognito() {
        return incognito;
    }

    public void setIncognito(boolean incognito) {
        this.incognito = incognito;
    }

    public List<VenueCheckinInformation> getTopCheckins() {
        return top_check_ins;
    }

    public List<VenueCheckinInformation> getLastCheckins() {
        return last_check_ins;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof User))return false;
        User otherUser = (User)obj;
        return (_id.equals(((User) obj).getId()));
    }
}
