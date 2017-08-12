package tk.internet.praktikum;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.NewVenueDetail;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Comment;
import tk.internet.praktikum.foursquare.api.bean.TextComment;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.service.VenueService;

/**
 * Created by gehhi on 12.08.2017.
 */

public class CommentAdapter extends android.support.v7.widget.RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
    VenueService service;
    Venue venue;
    List<Comment> comments;
    public CommentAdapter(Venue venue) {
        this.venue = venue;
        this.service = ServiceFactory.createRetrofitService(VenueService.class, NewVenueDetail.URL);
        this.comments = new ArrayList<>();
        this.service.getComments(venue.getId(), 0)
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
        if(comment instanceof TextComment) {
            TextComment tcomment = (TextComment)comment;
            holder.text.setText(tcomment.getText());
        } else {
            holder.text.setText("Image");
        }

    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, text;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            text = (TextView) view.findViewById(R.id.text);
        }
    }
}
