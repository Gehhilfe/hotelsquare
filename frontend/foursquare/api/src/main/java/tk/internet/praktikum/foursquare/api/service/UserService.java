package tk.internet.praktikum.foursquare.api.service;

import io.reactivex.Observable;
import retrofit2.http.*;
import tk.internet.praktikum.foursquare.api.bean.FriendRequestResponse;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.UserSearchQuery;

import java.util.List;

public interface UserService {
    /**
     * Retrives profile of user with name
     * @param name user name
     * @return profile of user
     */
    @GET("users/{name}")
    Observable<User> profile(@Path("name") String name);

    /**
     * Deletes currently authenticated user
     *
     * @return deleted user
     */
    @DELETE("users")
    Observable<User> deleteUser();

    /**
     * Registers a new user
     * @param user user which will be registered
     * @return registered user
     */
    @POST("users")
    Observable<User> register(@Body User user);

        /**
         * Allows to change user properties
         * Fields that should not changed should be null
         *
         * @param user changed user
         * @return new user model
         */
        @PUT("users")
        Observable<User> update(@Body User user);

    /**
     * Retrives profile of user with name
     * @param name User name
     * @return profile of user
     */
    @POST("users/{name}/friend_requests")
    Observable<Object> sendFriendRequest(@Path("name") String name);

    /**
     * Search for users with a given name and gender
     * @param query Search query
     */
    @POST("searches/users")
    Observable<List<User>> search(@Body UserSearchQuery query);
}
