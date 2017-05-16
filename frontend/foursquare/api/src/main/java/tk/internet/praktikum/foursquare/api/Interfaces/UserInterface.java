package tk.internet.praktikum.foursquare.api.Interfaces;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import tk.internet.praktikum.foursquare.api.Data.LoginCredentials;
import tk.internet.praktikum.foursquare.api.Data.User;
import tk.internet.praktikum.foursquare.api.Data.Venue;
import tk.internet.praktikum.foursquare.api.Data.VenueQuery;

/**
 * Created by robert on 14.05.2017.
 */

public interface UserInterface
{

    @GET("/user/friends")
    Call<List<User>> getFriends(@Body User user);

    @GET("/user/find")
    Call<User> findPerson(@Body String name);

    @GET("/user/profile")
    Call<User> getProfile(@Body User user);

    @POST("/user/new_user")
    Call<User> newUser(@Body User user);

    @DELETE("/user/delete_account")
    Call<User> deleteUser(@Body String user);

    @POST("/user/reset_password")
    Call<LoginCredentials> resetPassword(@Body LoginCredentials loginCredentials);

}
