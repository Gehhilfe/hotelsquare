package tk.internet.praktikum.foursquare.api.service;

import io.reactivex.Observable;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import tk.internet.praktikum.foursquare.api.bean.Comment;

/**
 * Created by gehhi on 10.07.2017.
 */

public interface CommentService {
    /**
     * Likes a comment for the authenticated user
     * @param id comment id
     */
    @PUT("comments/{id}/like")
    Observable<Comment> like(@Path("id") String id);

    /**
     * Dislikes a comment for the authenticated user
     * @param id comment id
     */
    @PUT("comments/{id}/dislike")
    Observable<Comment> dislike(@Path("id") String id);
}
