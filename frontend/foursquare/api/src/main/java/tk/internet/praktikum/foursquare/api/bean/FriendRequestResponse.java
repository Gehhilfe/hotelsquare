package tk.internet.praktikum.foursquare.api.bean;

public class FriendRequestResponse {

    private boolean accept;

    public FriendRequestResponse() {
        this(false);
    };

    public FriendRequestResponse(boolean accept) {
        this.accept = accept;
    };

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }
}
