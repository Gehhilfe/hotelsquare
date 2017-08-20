package tk.internet.praktikum.foursquare.history;

/**
 * Created by truongtud on 19.08.2017.
 */

public enum HistoryType {
    CHECKIN(0),
    VISIT(1),
    LIKECOMMENT(2),
    DISLIKE_COMMENT(3),
    TEXT_COMMENT(4),
    IMAGE_COMMENT(5);
    private int value;

    HistoryType(int value){
        this.value=value;
    }

    public  int getValue(){
        return this.value;
    }
}
