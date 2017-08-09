package tk.internet.praktikum.foursquare.api.service;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import tk.internet.praktikum.foursquare.api.bean.PlaceAutoComplete;

/**
 * Created by truongtud on 18.07.2017.
 */

public interface PlaceService {

    @POST("/autocomplete")
    Observable<PlaceAutoComplete> getSuggestedPlaces(@Body String typedPlace);
}
