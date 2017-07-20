package tk.internet.praktikum.foursquare.friendlist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.User;

public class FLRecyclerViewAdapter extends RecyclerView.Adapter<FLRecyclerViewAdapter.FriendListViewHolder> {

    class FriendListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView sendMsg, avatar;
        private TextView name;

        public FriendListViewHolder(View itemView) {
            super(itemView);
            sendMsg = (ImageView) itemView.findViewById(R.id.fl_msg);
            avatar = (ImageView) itemView.findViewById(R.id.fl_avatar);
            name = (TextView) itemView.findViewById(R.id.fl_name);

            itemView.setOnClickListener(this);
            sendMsg.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.fl_msg) {
                Toast.makeText(v.getContext(), "Send Msg " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), "Profile " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    private LayoutInflater inflater;
    private List<User> friendList = Collections.emptyList();
    private int[] dummyProfilePictures = {
            R.mipmap.ic_account_circle_black_24dp,
            R.mipmap.ic_history_black_24dp,
            R.mipmap.ic_launcher,
            R.mipmap.ic_location_search,
            R.mipmap.ic_lock_outline_black_24dp,
            R.mipmap.ic_mail_outline_black_24dp,
            R.mipmap.location,
            R.mipmap.ic_account_circle_black_24dp,
            R.mipmap.ic_history_black_24dp,
            R.mipmap.ic_launcher,
            R.mipmap.ic_location_search,
            R.mipmap.ic_lock_outline_black_24dp,
            R.mipmap.ic_mail_outline_black_24dp,
            R.mipmap.location,
            R.mipmap.ic_account_circle_black_24dp,
            R.mipmap.ic_history_black_24dp,
            R.mipmap.ic_launcher,
            R.mipmap.ic_location_search,
            R.mipmap.ic_lock_outline_black_24dp,
            R.mipmap.ic_mail_outline_black_24dp,
            R.mipmap.location};

    public FLRecyclerViewAdapter(Context context, List<User> friendList) {
        inflater = LayoutInflater.from(context);
        this.friendList = friendList;
    }

    @Override
    public FriendListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.friendlist_entry, parent, false);

        return new FriendListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendListViewHolder holder, int position) {
        User currentUser = friendList.get(position);
        if (currentUser.getAvatar() != null) {
            // TODO - Bild vom image loader
        } else {
            holder.avatar.setImageResource(R.mipmap.user_avatar);
        }

        holder.name.setText(currentUser.getName());
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }
}
