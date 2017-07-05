package tk.internet.praktikum.foursquare.api.service;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import tk.internet.praktikum.foursquare.api.bean.FriendRequestResponse;
import tk.internet.praktikum.foursquare.api.bean.User;

public interface ProfileService {

    /**
     * Retrieves profile of current authenticated user
     *
     * @return profile of authenticated user
     */
    @GET("profile")
    Observable<User> profile();

    /**
     * Answers pending friend request
     *
     * @param name     User name of request sender
     * @param response Accept or decline answer
     */
    @PUT("profile/friend_requests/{name}")
    Observable<Object> answerFriendRequest(@Path("name") String name, @Body FriendRequestResponse response);

}
