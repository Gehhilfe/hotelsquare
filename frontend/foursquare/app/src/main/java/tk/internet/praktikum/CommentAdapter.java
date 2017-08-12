package tk.internet.praktikum;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.NewVenueDetail;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Comment;
import tk.internet.praktikum.foursquare.api.bean.Image;
import tk.internet.praktikum.foursquare.api.bean.ImageComment;
import tk.internet.praktikum.foursquare.api.bean.TextComment;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.service.VenueService;

/**
 * Created by gehhi on 12.08.2017.
 */

public class CommentAdapter extends android.support.v7.widget.RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
    VenueService service;
    String venueId;
    List<Comment> comments;
    Context context;
    int lastPage = 0;

    public CommentAdapter(String venueId, Context context) {
        this.context = context;
        this.venueId = venueId;
        this.service = ServiceFactory.createRetrofitService(VenueService.class, NewVenueDetail.URL);
        this.comments = new ArrayList<>();
        this.service.getComments(venueId, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    comments = result;
        });
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.name.setText(comment.getAuthor().getName());
        Integer delta = comment.getLikes()-comment.getDislikes();

        holder.votes.setText(delta.toString());
        if(comment instanceof TextComment) {
            TextComment tcomment = (TextComment)comment;
            holder.text.setText(tcomment.getText());
        } else {
            ImageComment icomment = (ImageComment)comment;
            holder.text.setText("");
            ImageCacheLoader loader = new ImageCacheLoader(context);
            loader.loadBitmap(icomment.getImage(), ImageSize.MEDIUM)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> holder.image.setImageBitmap(bitmap));
        }

        if(comment.getAuthor().getAvatar() != null) {
            ImageCacheLoader loader = new ImageCacheLoader(context);
            loader.loadBitmap(comment.getAuthor().getAvatar(), ImageSize.SMALL)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> holder.avatar.setImageBitmap(bitmap));
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void loadMore() {
        this.service.getComments(venueId, lastPage+1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    lastPage += 1;
                    comments.addAll(result);
                    notifyDataSetChanged();
                });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name, text, votes;
        public ImageView avatar;
        public ImageButton upvote, downvote;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            text = (TextView) view.findViewById(R.id.text);
            votes = (TextView) view.findViewById(R.id.votes);
            avatar = (ImageView) view.findViewById(R.id.avatar);
            upvote = (ImageButton) view.findViewById(R.id.upvote);
            downvote = (ImageButton) view.findViewById(R.id.downvote);
            image = (ImageView) view.findViewById(R.id.image);
        }
    }
}
