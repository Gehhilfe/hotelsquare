package tk.internet.praktikum.foursquare.api.service;

import io.reactivex.Observable;
import retrofit2.http.*;
import tk.internet.praktikum.foursquare.api.bean.Answer;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.UserSearchQuery;

import java.util.List;

public interface UserService {
    /**
     * Retrieves profile of current authenticated user
     *
     * @return profile of authenticated user
     */
    @GET("profile")
    Observable<User> profile();

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

    /**
     * Allows to change user properties
     * Fields that should not changed should be null
     *
     * @param user changed user
     * @return new user model
     */
    @PUT("user")
    Observable<User> update(@Body User user);

    /**
     * Retrives profile of user with name
     * @param name User name
     * @return profile of user
     */
    @POST("user/{name}/friend_requests")
    Observable<Object> sendFriendRequest(@Path("name") String name);

    /**
     * Answers pending friend request
     * @param name User name of request sender
     * @param answer Accept or decline answer
     */
    @PUT("profile/friend_requests/{name}")
    Observable<Object> answerFriendRequest(@Path("name") String name, @Body Answer answer);

    /**
     * Search for users with a given name and gender
     * @param query Search query
     */
    @POST("users")
    Observable<List<User>> search(@Body UserSearchQuery query);
}
