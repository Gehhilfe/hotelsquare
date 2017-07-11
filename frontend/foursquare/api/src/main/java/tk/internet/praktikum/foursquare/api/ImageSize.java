package tk.internet.praktikum.foursquare.api;

/**
 * Created by gehhi on 09.07.2017.
 */

enum ImageSize {
    SMALL(0),
    MEDIUM(1),
    LARGE(2);

    private int value;
    private ImageSize(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
