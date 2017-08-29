package tk.internet.praktikum.foursquare.friendlist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.ChatService;
import tk.internet.praktikum.foursquare.chat.ChatActivity;
import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.user.ProfileActivity;

class FLRecyclerViewAdapter extends RecyclerView.Adapter<FLRecyclerViewAdapter.FriendListViewHolder> {

    class FriendListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView sendMsg, avatar;
        private TextView name;

        FriendListViewHolder(View itemView) {
            super(itemView);
            sendMsg = (ImageView) itemView.findViewById(R.id.fl_msg);
            avatar = (ImageView) itemView.findViewById(R.id.fl_avatar);
            name = (TextView) itemView.findViewById(R.id.fl_name);

            itemView.setLongClickable(false);
            itemView.setOnClickListener(this);
            sendMsg.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.fl_msg) {
                loadChat();
            } else {
                loadProfile();
            }

        }

        /**
         * Start or continues a chat with the selected person.
         */
        private void loadChat() {
            ChatService service = ServiceFactory
                    .createRetrofitService(ChatService.class, URL, LocalStorage.
                            getSharedPreferences(context).getString(Constants.TOKEN, ""));

            try {
                service.getOrStartChat(friendList.get(getAdapterPosition()).getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                chatResponse -> {
                                    chatResponse.getChatId();
                                    Intent intent = new Intent(context, ChatActivity.class);
                                    intent.putExtra("chatId", chatResponse.getChatId());
                                    intent.putExtra("Parent", "UserActivity");
                                    activity.startActivity(intent);
                                },
                                throwable -> Log.d(LOG, throwable.getMessage())
                        );
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Loads the profile of the selected user.
         */
        private void loadProfile() {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("userID", friendList.get(getAdapterPosition()).getId());
            intent.putExtra("Parent", "UserActivity");
            activity.startActivity(intent);
        }
    }

    private static final String LOG = FLRecyclerViewAdapter.class.getSimpleName();
    private final String URL = "https://dev.ip.stimi.ovh/";
    private Activity activity;
    private Context context;
    private LayoutInflater inflater;
    private List<User> friendList;

    FLRecyclerViewAdapter(Context context, Activity activity) {
        inflater = LayoutInflater.from(context);
        friendList = new ArrayList<>();
        this.context = context;
        this.activity = activity;
    }

    @Override
    public FriendListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.friendlist_entry, parent, false);
        return new FriendListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendListViewHolder holder, int position) {
        User currentUser = friendList.get(position);
        // Loads the avatar
        if (currentUser.getAvatar() != null) {
            ImageCacheLoader imageCacheLoader = new ImageCacheLoader(context);
            imageCacheLoader.loadBitmap(currentUser.getAvatar(), ImageSize.LARGE)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> holder.avatar.setImageBitmap(bitmap),
                            throwable -> Log.d(LOG, throwable.getMessage())
                    );
        } else {
            holder.avatar.setImageResource(R.mipmap.user_avatar);
        }

        holder.name.setText(currentUser.getName());
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    /**
     * Updates and sorts the friend list.
     * @param data New friends that will be added to the friend list.
     */
    void updateList(List<User> data) {
        friendList.addAll(data);
        Collections.sort(friendList,new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        notifyDataSetChanged();
    }

    /**
     * Returns the current friend list.
     * @return Current list of friends.
     */
    List<User> getFriendList() {
        return friendList;
    }

}
