package tk.internet.praktikum.foursquare.api.service;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import tk.internet.praktikum.foursquare.api.bean.CheckinInformation;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.bean.VenueComment;
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

    /**
     * Retrieves detailed information
     *
     * @param id venue id
     * @return detail information
     */
    @GET("venues/{id}")
    Observable<Venue> getDetails(@Path("id") String id);

    /**
     * Checks user authenticated with token into venue with id
     * @param id venue id
     * @return checkin information
     */
    @PUT("venues/{id}/checkin")
    Observable<CheckinInformation> checkin(@Path("id") String id);

    /**
     * Gets all comments of venue
     * @param id venue id
     * @return öost of all venue comments
     */
    @GET("venues/{id}/comments")
    Observable<List<VenueComment>> getComments(@Path("id") String id);

    /**
     * Adds a comment to the venue
     * @param id venue id
     * @return comment
     */
    @POST("venues/{id}")
    Observable<VenueComment> addVenueComment(@Body VenueComment venueComment, @Path("id") String id);

}
