package tk.internet.praktikum.foursquare.search;

import android.media.Image;

/**
 * Created by truongtud on 02.07.2017.
 */

public class SearchResult {
    private String name;
    private String address;
    private int rating;
    private Image image;

    public SearchResult(String name, String address, int rating, Image image) {
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
