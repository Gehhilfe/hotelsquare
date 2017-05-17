package tk.internet.praktikum.foursquare.api.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import tk.internet.praktikum.foursquare.api.beans.LoginCredentials;
import tk.internet.praktikum.foursquare.api.beans.User;
import tk.internet.praktikum.foursquare.api.beans.UserDTO;

/**
 * Created by robert on 14.05.2017.
 * @author robert, truongtud
 */

public interface UserServices
{

    @GET("/user/friends")
    Call<List<UserDTO>> getFriends(@Body User user);

    @GET("/user/find")
    Call<UserDTO> findPerson(@Body String name);

    @POST("/user/add_friend")
    Call<UserDTO> addFriend(@Body UserDTO friend);

    @GET("/user/profile")
    Call<UserDTO> getProfile(@Body UserDTO user);

    @POST("/user/new_user")
    Call<UserDTO> newUser(@Body User user);

    @DELETE("/user/delete_account")
    Call<User> deleteUser(@Body String user);

    @POST("/user/reset_password")
    Call<LoginCredentials> resetPassword(@Body LoginCredentials loginCredentials);


}
