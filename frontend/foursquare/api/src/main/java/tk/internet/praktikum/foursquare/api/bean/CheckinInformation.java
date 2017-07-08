package tk.internet.praktikum.foursquare.api.bean;

import java.util.Date;

/**
 * Created by gehhi on 08.07.2017.
 */

public class CheckinInformation {
    private String user;
    private int count;
    private Date last;

    public String getUserID() {
        return user;
    }

    public int getCount() {
        return count;
    }

    public Date getLastDate() {
        return last;
    }
}
