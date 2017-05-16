package tk.internet.praktikum.foursquare.api.Interfaces;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;

import tk.internet.praktikum.foursquare.api.Data.LoginCredentials;
import tk.internet.praktikum.foursquare.api.Data.TokenInformation;
import tk.internet.praktikum.foursquare.api.Data.Venue;
import tk.internet.praktikum.foursquare.api.Data.VenueQuery;

/**
 * Created by robert on 14.05.2017.
 */

public interface VenueQueryInterface
{

    @GET("/venue/query")
    Call<List<Venue>> getVenues(@Body VenueQuery venue_query);

}
