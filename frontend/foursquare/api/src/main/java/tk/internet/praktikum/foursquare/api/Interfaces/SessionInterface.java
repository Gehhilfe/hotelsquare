package tk.internet.praktikum.foursquare.api.Interfaces;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import tk.internet.praktikum.foursquare.api.Data.LoginCredentials;
import tk.internet.praktikum.foursquare.api.Data.TokenInformation;

/**
 * Created by robert on 14.05.2017.
 */

public interface SessionInterface {

    @POST("/session")
    Call<TokenInformation> postSession(@Body LoginCredentials loginCredentials);

}
