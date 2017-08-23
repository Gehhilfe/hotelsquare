package tk.internet.praktikum;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.DateFormat;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.VenueInDetailsNestedScrollView;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Comment;
import tk.internet.praktikum.foursquare.api.bean.ImageComment;
import tk.internet.praktikum.foursquare.api.bean.TextComment;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.CommentService;
import tk.internet.praktikum.foursquare.api.service.VenueService;
import tk.internet.praktikum.foursquare.history.HistoryEntry;
import tk.internet.praktikum.foursquare.history.HistoryType;
import tk.internet.praktikum.foursquare.storage.LocalDataBaseManager;
import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.user.ProfileActivity;
import tk.internet.praktikum.foursquare.user.UserActivity;

/**
 * Created by gehhi on 12.08.2017.
 */

public class CommentAdapter extends android.support.v7.widget.RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
    private static final String LOG = CommentAdapter.class.getName();
    VenueService service;
    String venueId;
    List<Comment> comments;
    Context context;
    int lastPage = 0;
    private boolean lastEmpty = false;
    private String venueName;
    public CommentAdapter(String venueId, Context context) {
        this.context = context;
        this.venueId = venueId;
        this.service = ServiceFactory.createRetrofitService(VenueService.class, VenueInDetailsNestedScrollView.URL);
        this.comments = new ArrayList<>();
        this.service.getComments(venueId, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    comments = result;
                    notifyDataSetChanged();
                }, (err) -> Log.d(LOG, err.toString(), err));
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
        holder.name.setText(comment.getAuthor().getDisplayName());
        holder.votes.setText(String.format("%d", comment.getRating()));

        holder.date.setText(DateFormat.getFriendlyTime(context,comment.getDate()));
        if (comment instanceof TextComment) {
            TextComment tcomment = (TextComment) comment;
            holder.text.setText(tcomment.getText());
            holder.text.setVisibility(View.VISIBLE);
            holder.image.setVisibility(View.GONE);
        } else {
            ImageComment icomment = (ImageComment) comment;
            holder.text.setText(" ");
            if (icomment.getImage() != null) {
                ImageCacheLoader loader = new ImageCacheLoader(context);
                loader.loadBitmap(icomment.getImage(), ImageSize.MEDIUM)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bitmap -> {
                            holder.image.setImageBitmap(bitmap);
                            holder.image.setVisibility(View.VISIBLE);
                            holder.text.setVisibility(View.GONE);
                        }, (err) -> Log.d(LOG, err.toString(), err));
            } else {
                Log.d(VenueInDetailsNestedScrollView.LOG, "image is null");
            }
        }

        if (comment.getAuthor().getAvatar() != null) {
            ImageCacheLoader loader = new ImageCacheLoader(context);
            loader.loadBitmap(comment.getAuthor().getAvatar(), ImageSize.SMALL)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> holder.avatar.setImageBitmap(bitmap), err -> Log.d("CommentAdapter", err.toString(), err));
        }

        holder.upvote.setOnClickListener((event) -> {
            LocalStorage ls = LocalStorage.getLocalStorageInstance(context);
            SharedPreferences sp = LocalStorage.getSharedPreferences(context);
            if (ls.isLoggedIn()) {
                CommentService service = ServiceFactory.createRetrofitService(CommentService.class, VenueInDetailsNestedScrollView.URL, sp.getString(Constants.TOKEN, ""));
                service.like(comment.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(cmt -> {
                            Integer d = cmt.getRating();
                            holder.votes.setText(String.format("%d", d));
                            HistoryEntry historyEntry=new HistoryEntry(UUID.randomUUID().toString(), HistoryType.LIKECOMMENT,getVenueName(),venueId,new Date());
                            LocalDataBaseManager.getLocalDatabaseManager(context).getDaoSession().getHistoryEntryDao().insert(historyEntry);
                        }, err -> Log.d("CommentAdapter", err.toString(), err));
            } else {
                Toast.makeText(context, "Login first", Toast.LENGTH_SHORT).show();
            }
        });

        holder.downvote.setOnClickListener((event) -> {
            LocalStorage ls = LocalStorage.getLocalStorageInstance(context);
            SharedPreferences sp = LocalStorage.getSharedPreferences(context);
            if (ls.isLoggedIn()) {
                CommentService service = ServiceFactory.createRetrofitService(CommentService.class, VenueInDetailsNestedScrollView.URL, sp.getString(Constants.TOKEN, ""));
                service.dislike(comment.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(cmt -> {
                            Integer d = cmt.getRating();
                            holder.votes.setText(String.format("%d", d));
                            HistoryEntry historyEntry=new HistoryEntry(UUID.randomUUID().toString(), HistoryType.DISLIKE_COMMENT,getVenueName(),venueId,new Date());
                            LocalDataBaseManager.getLocalDatabaseManager(context).getDaoSession().getHistoryEntryDao().insert(historyEntry);
                        }, err -> Log.d("CommentAdapter", err.toString(), err));
            } else {
                Toast.makeText(context, "Login first", Toast.LENGTH_SHORT).show();
            }
        });
        holder.avatar.setOnClickListener(seeProfile(comment.getAuthor()));
        holder.name.setOnClickListener(seeProfile(comment.getAuthor()));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void loadMore() {
        if (lastEmpty)
            return;

        this.service.getComments(venueId, lastPage + 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    if (result.isEmpty()) {
                        lastEmpty = true;
                        return;
                    }
                    lastPage += 1;
                    comments.addAll(result);
                    notifyDataSetChanged();
                }, (err) -> Log.d(LOG, err.toString(), err));
    }

    public void addComment(Comment comment) {
        comments.add(0, comment);
        notifyDataSetChanged();
    }
    public View.OnClickListener seeProfile(User user) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(context);
                String userName = sharedPreferences.getString(Constants.NAME, "");
                if (user.getName().equals(userName)) {
                    Intent intent = new Intent(context, UserActivity.class);
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra("userID", user.getId());
                    intent.putExtra("Parent", "VenueInDetailsNestedScrollView");
                    context.startActivity(intent);
                }

            }
        };
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, text, votes, date;
        public ImageView avatar, image;
        public ImageButton upvote, downvote;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            date = (TextView) view.findViewById(R.id.date);
            text = (TextView) view.findViewById(R.id.text);
            votes = (TextView) view.findViewById(R.id.votes);
            avatar = (ImageView) view.findViewById(R.id.avatar);
            upvote = (ImageButton) view.findViewById(R.id.upvote);
            downvote = (ImageButton) view.findViewById(R.id.downvote);
            image = (ImageView) view.findViewById(R.id.image);
        }
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
}
