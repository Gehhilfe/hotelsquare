package tk.internet.praktikum.foursquare.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.bean.Comment;
import tk.internet.praktikum.foursquare.api.bean.Image;
import tk.internet.praktikum.foursquare.api.bean.ImageComment;
import tk.internet.praktikum.foursquare.api.bean.TextComment;

import static android.view.View.GONE;

/**
 * Created by truongtud on 20.07.2017.
 */

public class CommentVenueViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final String  LOG=CommentVenueViewHolder.class.getName();
    private View commentItemView;
    private TextView comment_content;


    private TextView comment_date;
    private ImageView user_avatar;
    private ImageView comment_image_content;
    private Context context;
    public CommentVenueViewHolder(View itemView) {
        super(itemView);
        this.commentItemView=itemView;
        user_avatar= (ImageView) commentItemView.findViewById(R.id.user_image);
        comment_content=(TextView) commentItemView.findViewById(R.id.comment_content);
        comment_date=(TextView)commentItemView.findViewById(R.id.comment_date);
        comment_image_content=(ImageView) commentItemView.findViewById(R.id.comment_image_content);
    }



    public void renderComment(Comment comment){
        ImageCacheLoader imageCacheLoader = new ImageCacheLoader(this.context);
        if(comment instanceof TextComment){
            TextComment textComment= (TextComment) comment;
            comment_image_content.setVisibility(View.GONE);
            comment_content.setText(textComment.getText());
            comment_content.setVisibility(View.VISIBLE);

        }
        else{
            ImageComment imageComment=(ImageComment)comment;
            Log.d(LOG, "imageComment: "+imageComment.getImage());
            comment_content.setVisibility(View.GONE);
            Image image=imageComment.getImage();
            if(image!=null) {
                imageCacheLoader.loadBitmap(image, ImageSize.SMALL)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bitmap -> {
                            Log.d(LOG,"*** bitmap: "+bitmap);
                            comment_image_content.setImageBitmap(bitmap);
                            comment_image_content.setVisibility(View.VISIBLE);
                        });
            }
            else
                comment_image_content.setVisibility(GONE);


        }

        Image avatar=comment.getAuthor().getAvatar();
        if(avatar!=null) {
            imageCacheLoader.loadBitmap(avatar, ImageSize.SMALL)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        user_avatar.setImageBitmap(bitmap);
                        user_avatar.setVisibility(View.VISIBLE);
                    });
        }
        else
            user_avatar.setVisibility(GONE);
        Date date= comment.getDate();
        if (date!=null) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            String dateToString = formatter.format(comment.getDate());
            comment_date.setText(dateToString);
            comment_date.setVisibility(View.VISIBLE);
        }
        commentItemView.setVisibility(View.VISIBLE);


    }
    @Override
    public void onClick(View v) {

    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
