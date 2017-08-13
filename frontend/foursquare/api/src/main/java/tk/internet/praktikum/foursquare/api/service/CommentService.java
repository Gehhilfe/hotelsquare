package tk.internet.praktikum.foursquare.api.service;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import tk.internet.praktikum.foursquare.api.bean.Comment;
import tk.internet.praktikum.foursquare.api.bean.ImageComment;
import tk.internet.praktikum.foursquare.api.bean.TextComment;

/**
 * Created by gehhi on 10.07.2017.
 */

public interface CommentService {
    /**
     * Likes a comment for the authenticated user
     * @param id comment id
     */
    @PUT("comments/{id}/like")
    Observable<Object> like(@Path("id") String id);

    /**
     * Get 10 comments to comment identified by id
     * @param id venue id
     * @param page page number
     * @return list of 10 comments
     */
    @GET("venues/{id}/comments/{page}")
    Observable<List<Comment>> getComments(@Path("id") String id, @Path("page") int page);

    /**
     * Dislikes a comment for the authenticated user
     * @param id comment id
     */
    @PUT("comments/{id}/dislike")
    Observable<Object> dislike(@Path("id") String id);

    /**
     * Adds a text comment to the comment
     * @param id comment id
     * @return comment
     */
    @POST("comments/{id}/comments/text")
    Observable<TextComment> addTextComment(@Body TextComment comment, @Path("id") String id);

    /**
     * Adds a image comment to the comment
     * @param id comment id
     * @return comment
     */
    @Multipart
    @POST("comments/{id}/comments/image")
    Observable<ImageComment> uploadAvatar(@Part MultipartBody.Part image, @Path("id") String id);
}
