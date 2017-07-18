package tk.internet.praktikum.foursquare.utils;

import tk.internet.praktikum.foursquare.api.bean.Venue;

/**
 * Created by truongtud on 12.07.2017.
 */

public interface FilterVenue {
    boolean applyFilter(Venue venue);
}
