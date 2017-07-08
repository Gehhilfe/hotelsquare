package tk.internet.praktikum.foursquare.history;

import java.util.Date;

import tk.internet.praktikum.foursquare.api.bean.VenueSearchQuery;

/**
 * Created by truongtud on 04.07.2017.
 */

public class HistorySearch extends HistoryEntryBase {
    private VenueSearchQuery venueSearchQuery;

    public HistorySearch(String uid, String historyName, Date date) {
        super(uid, historyName, date);
    }


    public VenueSearchQuery getVenueSearchQuery() {
        return venueSearchQuery;
    }

    public void setVenueSearchQuery(VenueSearchQuery venueSearchQuery) {
        this.venueSearchQuery = venueSearchQuery;
    }
}
