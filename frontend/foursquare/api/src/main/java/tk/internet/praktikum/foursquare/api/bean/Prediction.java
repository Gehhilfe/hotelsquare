package tk.internet.praktikum.foursquare.api.bean;

/**
 * Created by truongtud on 18.07.2017.
 */

public class Prediction {
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    String description;
}
