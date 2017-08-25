package tk.internet.praktikum.foursquare.friendlist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class FLRecyclerViewAdapter extends RecyclerView.Adapter<FLRecyclerViewAdapter.FriendListViewHolder> {

    class FriendListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView sendMsg, avatar;
        private TextView name;

        public FriendListViewHolder(View itemView) {
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
                                throwable -> {
                                    Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        );
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void loadProfile() {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("userID", friendList.get(getAdapterPosition()).getId());
            intent.putExtra("Parent", "UserActivity");
            activity.startActivity(intent);
        }
    }
    
    private final String URL = "https://dev.ip.stimi.ovh/";
    private Activity activity;
    private Context context;
    private LayoutInflater inflater;
    private List<User> friendList = Collections.emptyList();

    public FLRecyclerViewAdapter(Context context, List<User> friendList, Activity activity) {
        inflater = LayoutInflater.from(context);
        this.friendList = friendList;
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
        if (currentUser.getAvatar() != null) {
            ImageCacheLoader imageCacheLoader = new ImageCacheLoader(context);
            imageCacheLoader.loadBitmap(currentUser.getAvatar(), ImageSize.LARGE)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                                holder.avatar.setImageBitmap(bitmap);
                            },
                            throwable -> {
                                Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
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
}
