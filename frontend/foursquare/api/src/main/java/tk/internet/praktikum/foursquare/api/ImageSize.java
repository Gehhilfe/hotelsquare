package tk.internet.praktikum.foursquare.api;


public enum ImageSize {
    SMALL(0),
    MEDIUM(1),
    LARGE(2);

    private int value;
    ImageSize(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
