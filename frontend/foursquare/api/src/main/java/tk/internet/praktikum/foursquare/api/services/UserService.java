package tk.internet.praktikum.foursquare.api.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;

/**
 * Created by robert on 14.05.2017.
 *
 * @author robert, truongtud
 */

public interface UserService {

    @DELETE("user")
    Call deleteUser(@Body String user);

}
