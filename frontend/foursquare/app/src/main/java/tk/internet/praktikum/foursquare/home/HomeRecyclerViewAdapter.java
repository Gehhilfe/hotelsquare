package tk.internet.praktikum.foursquare.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.FriendRequest;
import tk.internet.praktikum.foursquare.api.bean.FriendRequestResponse;
import tk.internet.praktikum.foursquare.api.bean.Gender;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.ProfileService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.HomeViewHolder> {

    class HomeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView requst;
        private ImageView accept, decline;

        public HomeViewHolder(View itemView) {
            super(itemView);
            requst = (TextView) itemView.findViewById(R.id.home_entry_text);
            accept = (ImageView) itemView.findViewById(R.id.home_friend_request_accept);
            decline = (ImageView) itemView.findViewById(R.id.home_friend_request_decline);

            accept.setOnClickListener(this);
            decline.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.home_friend_request_accept) {
                acceptFriendRequest();
            } else if (v.getId() == R.id.home_friend_request_decline) {
                declineFriendRequest();
            }
        }

        private void acceptFriendRequest() {
            ProfileService service = ServiceFactory
                    .createRetrofitService(ProfileService.class, URL, LocalStorage.
                            getSharedPreferences(context).getString(Constants.TOKEN, ""));

            //String name = friendRequestList.get(getAdapterPosition()).getSenderID();
            String name = "peter";

            try {
                service.answerFriendRequest(name, new FriendRequestResponse(true))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                user -> {
                                    Toast.makeText(context, "Friend request accepted!", Toast.LENGTH_SHORT).show();
                                    friendRequestList.remove(getAdapterPosition());
                                    notifyDataSetChanged();
                                },
                                throwable -> {
                                    Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        );
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void declineFriendRequest() {
            ProfileService service = ServiceFactory
                    .createRetrofitService(ProfileService.class, URL, LocalStorage.
                            getSharedPreferences(context).getString(Constants.TOKEN, ""));

            //String name = friendRequestList.get(getAdapterPosition()).getSenderID();
            String name = "janus";

            try {
                service.answerFriendRequest(name, new FriendRequestResponse(false))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                user -> {
                                    Toast.makeText(context, "Friend request declined!", Toast.LENGTH_SHORT).show();
                                    friendRequestList.remove(getAdapterPosition());
                                    notifyDataSetChanged();
                                },
                                throwable -> {
                                    Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        );
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Context context;
    private LayoutInflater inflater;
    private List<FriendRequest> friendRequestList = Collections.emptyList();
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("d.M.Y HH:mm");
    private final String URL = "https://dev.ip.stimi.ovh/";


    public HomeRecyclerViewAdapter(Context context, List<FriendRequest> friendRequestList) {
        inflater = LayoutInflater.from(context);
        this.friendRequestList = friendRequestList;
        this.context = context;
    }

    @Override
    public HomeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.home_entry, parent, false);

        return new HomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeViewHolder holder, int position) {
        FriendRequest currentFriendRequest = friendRequestList.get(position);

        holder.requst.setText(SIMPLE_DATE_FORMAT.format(currentFriendRequest.getCreatedAt()) + "\n" + currentFriendRequest.getSenderID());
    }

    @Override
    public int getItemCount() {
        return friendRequestList.size();
    }
}
