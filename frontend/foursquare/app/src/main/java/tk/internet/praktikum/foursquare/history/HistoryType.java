package tk.internet.praktikum.foursquare.history;

public enum HistoryType {
    CHECKIN(0),
    //VISIT(1),
    LIKECOMMENT(1),
    DISLIKE_COMMENT(2),
    TEXT_COMMENT(3),
    IMAGE_COMMENT(4);
    private int value;

    HistoryType(int value){
        this.value=value;
    }

    public  int getValue(){
        return this.value;
    }
}
