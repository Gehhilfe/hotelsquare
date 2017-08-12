package tk.internet.praktikum.foursquare.search;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.Comment;

/**
 * Created by truongtud on 20.07.2017.
 */

public class CommentVenueAdapter extends RecyclerView.Adapter<CommentVenueViewHolder> {

    List<Comment> commentVenueList;
    Fragment parentFragment;



    public CommentVenueAdapter(List<Comment> commentVenueList, Fragment parentFragment) {
        this.commentVenueList = commentVenueList;
        this.parentFragment = parentFragment;
    }

    @Override
    public CommentVenueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment,parent,false);
        CommentVenueViewHolder commentVenueViewHolder= new CommentVenueViewHolder(view);
        commentVenueViewHolder.setContext(parentFragment.getContext());
        return commentVenueViewHolder;
    }
    @Override
    public void onBindViewHolder(CommentVenueViewHolder holder, int position) {
            holder.renderComment(commentVenueList.get(position));
    }

    @Override
    public int getItemCount() {
        return commentVenueList.size();
    }

    public List<Comment> getCommentVenueList() {
        return commentVenueList;
    }

    public void setCommentVenueList(List<Comment> commentVenueList) {
        this.commentVenueList = commentVenueList;
    }

    public  void addMoreCommentVenues(List<Comment> comments){
        if(this.commentVenueList==null)
            this.commentVenueList=new ArrayList<Comment>();
        this.commentVenueList.addAll(comments);
        this.notifyDataSetChanged();
    }
    public void addCommentVenue(Comment comment){
        if(this.commentVenueList==null)
            this.commentVenueList=new ArrayList<Comment>();
        this.commentVenueList.add(0,comment);
        this.notifyDataSetChanged();
    }
}
