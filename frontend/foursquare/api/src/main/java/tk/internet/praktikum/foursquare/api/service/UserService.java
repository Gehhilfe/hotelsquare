package tk.internet.praktikum.foursquare.api.service;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import tk.internet.praktikum.foursquare.api.bean.User;

public interface UserService {

    /**
     * deletes currently authenticated user
     *
     * @return deleted user
     */
    @DELETE("user")
    Observable<User> deleteUser();

    /**
     * registers new user
     * @param user user which will be registered
     * @return registered user
     */
    @POST("user")
    Observable<User> register(@Body User user);

}
