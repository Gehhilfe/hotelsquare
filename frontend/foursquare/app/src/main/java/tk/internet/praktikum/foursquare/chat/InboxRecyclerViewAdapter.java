package tk.internet.praktikum.foursquare.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.MainActivity;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.bean.Chat;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.user.ProfileActivity;

class InboxRecylcerViewAdapter extends RecyclerView.Adapter<InboxRecylcerViewAdapter.InboxViewHolder> {

    class InboxViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView sendMsg, avatar;
        private TextView name, preview;

        public InboxViewHolder(View itemView) {
            super(itemView);
            sendMsg = (ImageView) itemView.findViewById(R.id.inbox_msg);
            avatar = (ImageView) itemView.findViewById(R.id.inbox_avatar);
            name = (TextView) itemView.findViewById(R.id.inbox_name);
            preview = (TextView) itemView.findViewById(R.id.inbox_preview);

            itemView.setOnClickListener(this);
            avatar.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.inbox_avatar) {
                loadProfile();
            } else {
                startChat();
            }

        }

        private void loadProfile() {
            Chat currentChat = chatList.get(getAdapterPosition());
            User chatPartner = new User();

            for (User user : currentChat.getParticipants()) {
                if (!Objects.equals(user.getName(), currentUserName)) {
                    chatPartner = user;
                }
            }

            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("userID", chatPartner.getId());
            activity.startActivityForResult(intent, REQUEST_PROFILE);
        }

        private void startChat() {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId", chatList.get(getAdapterPosition()).getChatId());
            intent.putExtra("currentUserName", currentUserName);
            activity.startActivityForResult(intent, REQUEST_CHAT);
        }
    }

    private final int REQUEST_CHAT = 1;
    private final int REQUEST_PROFILE = 2;
    private Context context;
    private LayoutInflater inflater;
    private List<Chat> chatList = Collections.emptyList();
    private String currentUserName;
    private InboxFragment inboxFragment;
    private Activity activity;


    public InboxRecylcerViewAdapter(Context context, List<Chat> inbox, InboxFragment inboxFragment, Activity activity) {
        inflater = LayoutInflater.from(context);
        this.chatList = inbox;
        this.context = context;
        currentUserName = LocalStorage.getSharedPreferences(context).getString(Constants.NAME, "");
        this.inboxFragment = inboxFragment;
        this.activity = activity;
    }

    @Override
    public InboxViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.inbox_entry, parent, false);

        return new InboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InboxViewHolder holder, int position) {
        Chat currentChat = chatList.get(position);
        User currentUser = new User();
        User chatPartner = new User();

        for (User user : currentChat.getParticipants()) {
            if (Objects.equals(user.getName(), currentUserName)) {
                currentUser = user;
            } else {
                chatPartner = user;
            }
        }

        if (chatPartner.getAvatar() != null) {
            ImageCacheLoader imageCacheLoader = new ImageCacheLoader(context);
            imageCacheLoader.loadBitmap(chatPartner.getAvatar(), ImageSize.LARGE)
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

        holder.name.setText(chatPartner.getName());

        if (currentChat.getMessages().size() > 0)
            holder.preview.setText(currentChat.getMessages().get(currentChat.getMessages().size() - 1).getMessage());
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
}
