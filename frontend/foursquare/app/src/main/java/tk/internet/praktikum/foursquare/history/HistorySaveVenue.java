package tk.internet.praktikum.foursquare.history;

import java.util.Date;

import tk.internet.praktikum.foursquare.api.bean.Venue;

/**
 * Created by truongtud on 04.07.2017.
 */

public class HistorySaveVenue extends HistoryEntryBase {
    private Venue venue;

    public HistorySaveVenue(String uid, String historyName, Date date) {
        super(uid, historyName, date);
    }


    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

}
