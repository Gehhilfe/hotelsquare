package tk.internet.praktikum.foursquare.home;

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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.bean.FriendRequest;
import tk.internet.praktikum.foursquare.api.bean.User;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.HomeViewHolder> {

    class HomeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView entry;

        public HomeViewHolder(View itemView) {
            super(itemView);
            entry = (TextView) itemView.findViewById(R.id.home_entry);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.home_entry) {
                Toast.makeText(v.getContext(), "Entry " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    private Context context;
    private LayoutInflater inflater;
    private List<HomeData> homeEntries = Collections.emptyList();
    private List<FriendRequest> friendRequests = Collections.emptyList();

    public HomeRecyclerViewAdapter(Context context, List<HomeData> data, List<FriendRequest> friendRequest) {
        inflater = LayoutInflater.from(context);
        this.homeEntries = data;
        this.context = context;
        this.friendRequests = friendRequest;
    }

    @Override
    public HomeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.home_entry, parent, false);
        return new HomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeViewHolder holder, int position) {
        HomeData currentEntry = homeEntries.get(position);
        /*
        if (currentEntry.getType() == HomeType.FRIEND_REQUEST) {
            FriendRequest fr = currentEntry.getFriendRequest();
            holder.entry.setText("New friend request " + fr.getSenderID()
                    + " " + fr.getCreatedAt());
        }*/

        FriendRequest fr = friendRequests.get(position);
        holder.entry.setText("New friend request " + fr.getSenderID()
                + " " + fr.getCreatedAt());
    }

    @Override
    public int getItemCount() {
        return homeEntries.size();
    }
}
