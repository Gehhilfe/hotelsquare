package tk.internet.praktikum.foursquare.api.service;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import tk.internet.praktikum.foursquare.api.bean.PlaceAutoComplete;

/**
 * Created by truongtud on 18.07.2017.
 */

public interface PlaceService {

    @GET("/maps/api/place/autocomplete/json")
    Observable<PlaceAutoComplete> getSuggestedPlaces(@Query("input")String typedPlace,@Query("types") String types,  @Query("key") String api_key);
}
