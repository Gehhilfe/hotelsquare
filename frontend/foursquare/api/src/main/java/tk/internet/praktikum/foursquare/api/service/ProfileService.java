package tk.internet.praktikum.foursquare.api.service;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
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

    /**
     * Uploads avatar image
     * @param image multipart request body containing image data. Can be created with UploadHelper
     */
    @Multipart
    @POST("profile/avatar")
    Observable<User> uploadAvatar(@Part MultipartBody.Part image);
}
