package tk.internet.praktikum.foursquare.api.service;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.bean.VenueSearchQuery;
import tk.internet.praktikum.foursquare.api.bean.VenueSearchResult;

public interface VenueService {

    /**
     * Retrieves venues fitting a search query.
     *
     * @return venues for requested parameters
     */
    @POST("searches/venues/{page}")
    Observable<VenueSearchResult> queryVenue(@Body VenueSearchQuery query, @Path("page") int page);

    /**
     * Retrieves venues fitting a search query.
     *
     * @return Search result fitting the query
     */
    @POST("searches/venues")
    Observable<VenueSearchResult> queryVenue(@Body VenueSearchQuery query);
}
