package tk.internet.praktikum.foursquare.api.service;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import tk.internet.praktikum.foursquare.api.bean.LoginCredentials;
import tk.internet.praktikum.foursquare.api.bean.TokenInformation;

public interface SessionService {

    @POST("/session")
    Call<TokenInformation> postSession(@Body LoginCredentials loginCredentials);

}
