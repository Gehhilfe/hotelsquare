package tk.internet.praktikum.foursquare.api.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import tk.internet.praktikum.foursquare.api.beans.LoginCredentials;
import tk.internet.praktikum.foursquare.api.beans.TokenInformation;

/**
 * Created by robert on 14.05.2017.
 */

public interface SessionServices {

    @POST("/session")
    Call<TokenInformation> postSession(@Body LoginCredentials loginCredentials);

}
