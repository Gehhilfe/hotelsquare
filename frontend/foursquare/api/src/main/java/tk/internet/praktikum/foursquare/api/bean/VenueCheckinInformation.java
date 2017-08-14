package tk.internet.praktikum.foursquare.api.bean;

import java.util.Date;

/**
 * Created by gehhi on 14.08.2017.
 */

public class VenueCheckinInformation {
    private String venue;
    private int count;
    private Date last;

    public String getVenueID() {
        return venue;
    }

    public int getCount() {
        return count;
    }

    public Date getLastDate() {
        return last;
    }
}
