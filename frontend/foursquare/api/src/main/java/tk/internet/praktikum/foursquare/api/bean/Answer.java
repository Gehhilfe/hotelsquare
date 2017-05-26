package tk.internet.praktikum.foursquare.api.bean;

public class Answer {

    private boolean accept;

    public Answer() {
        this(false);
    };

    public Answer(boolean accept) {
        this.accept = false;
    };

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }
}
