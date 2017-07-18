package tk.internet.praktikum.foursquare.api.service;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import tk.internet.praktikum.foursquare.api.bean.FriendListResponse;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.UserSearchQuery;

public interface UserService {
    /**
     * Retrives profile of user with name
     *
     * @param name user name
     * @return profile of user
     */
    @GET("users/{name}")
    Observable<User> detailsByName(@Path("name") String name);

    /**
     * Retrives profile of user with id
     *
     * @param id user id
     * @return profile of user
     */
    @GET("users/id/{name}")
    Observable<User> profileByID(@Path("id") String id);

    /**
     * Registers a new user
     *
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
     *
     * @param name User name
     * @return profile of user
     */
    @POST("users/{name}/friend_requests")
    Observable<Object> sendFriendRequest(@Path("name") String name);

    /**
     * Search for users with a given name and gender
     *
     * @param query Search query
     */
    @POST("searches/users")
    Observable<List<User>> search(@Body UserSearchQuery query);

    /**
     * Retrvies friends list for user
     * @param page number page starts with 0
     * @return
     */
    @GET("profile/friends/{page}")
    Observable<FriendListResponse> friends(@Path("page") int page);
}
