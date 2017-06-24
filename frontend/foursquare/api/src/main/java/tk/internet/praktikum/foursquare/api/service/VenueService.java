package tk.internet.praktikum.foursquare.api.service;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.bean.VenueSearchQuery;

public interface VenueService {

    /**
     * Retrieves venues for current request
     *
     * @return venues for requested parameters
     */
    @POST("/venues/query")
    Observable<List<Venue>> queryVenue(@Body VenueSearchQuery query);

}
