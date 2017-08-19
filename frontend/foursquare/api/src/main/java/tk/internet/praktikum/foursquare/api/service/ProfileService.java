package tk.internet.praktikum.foursquare.api.service;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import tk.internet.praktikum.foursquare.api.bean.FriendListResponse;
import tk.internet.praktikum.foursquare.api.bean.FriendRequestResponse;
import tk.internet.praktikum.foursquare.api.bean.Location;
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
     * Retrieves a bunch of friendrequest by users
     *
     * @param page result page
     * @return List<User> list of open friendrequests by user in list
     */
    @GET("profile/friend_requests/{page}")
    Observable<List<User>> getFriendRequests(@Query("page") int page);

    /**
     * Answers pending friend request
     *
     * @param name     User name of request sender
     * @param response Accept or decline answer
     */
    @PUT("profile/friend_requests/{name}")
    Observable<Object> answerFriendRequest(@Path("name") String name, @Body FriendRequestResponse response);

    /**
     * Uploads avatar image
     * @param image multipart request body containing image data. Can be created with UploadHelper
     */
    @Multipart
    @POST("profile/avatar")
    Observable<User> uploadAvatar(@Part MultipartBody.Part image);

    /**
     * Retrvies friends list for user
     * @param page number page starts with 0
     * @return FriendListResponse
     */
    @GET("profile/friends/{page}")
    Observable<FriendListResponse> friends(@Path("page") int page);

    /**
     * Retrvies profile if he is friend with the authenticated user
     * @param other_id id of possible friend
     * @return FriendListReponse
     */
    @GET("profile/friends")
    Observable<FriendListResponse> profileIfFriends(@Query("only") String other_id);

    /**
     * Retrieves all friends near by
     *
     * @param location Location of the user
     * @return List<User> list of nearby friends
     */
    @POST("searches/nearbyfriends")
    Observable<List<User>> getNearByFriends(@Body Location location);

    /**
     * Deletes authenticated user =(
     * @return User that was deleted bye bye
     */
    @DELETE("users")
    Observable<User> delete();
}
