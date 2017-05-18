package tk.internet.praktikum.foursquare.api.services;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import tk.internet.praktikum.foursquare.api.pojo.LoginCredentials;
import tk.internet.praktikum.foursquare.api.pojo.TokenInformation;

/**test
 * Created by robert on 14.05.2017.
 */

public interface SessionService {

    @POST("session")
    Observable<TokenInformation> postSession(@Body LoginCredentials loginCredentials);

}
