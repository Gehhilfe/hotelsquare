package tk.internet.praktikum.foursquare.api.services;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import tk.internet.praktikum.foursquare.api.pojo.LoginCredentials;

/**
 * Created by robert on 14.05.2017.
 *
 * @author robert, truongtud
 */

public interface UserService {

    @DELETE("user")
    Call deleteUser(@Body String user);

    @POST("user")
    Observable<LoginCredentials> postRegistration(@Body LoginCredentials loginCredentials);

}
