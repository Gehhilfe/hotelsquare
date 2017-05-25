package tk.internet.praktikum.foursquare.api.service;

import io.reactivex.Observable;
import retrofit2.http.*;
import tk.internet.praktikum.foursquare.api.bean.User;

public interface UserService {
    /**
     * Retrieves profile of current authenticated user
     *
     * @return profile of authenticated user
     */
    @GET("profile")
    Observable<User> proile();

    /**
     * Retrives profile of user with name
     * @param name user name
     * @return profile of user
     */
    @GET("user/{name}")
    Observable<User> profile(@Path("name") String name);

    /**
     * Deletes currently authenticated user
     *
     * @return deleted user
     */
    @DELETE("user")
    Observable<User> deleteUser();

    /**
     * Registers a new user
     * @param user user which will be registered
     * @return registered user
     */
    @POST("user")
    Observable<User> register(@Body User user);
}
