package tk.internet.praktikum.foursquare.chat;

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
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.bean.Chat;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.user.ProfileActivity;

class InboxRecyclerViewAdapter extends RecyclerView.Adapter<InboxRecyclerViewAdapter.InboxViewHolder> {

    class InboxViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView sendMsg, avatar;
        private TextView name, preview;

        InboxViewHolder(View itemView) {
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
                sendMsg.setImageDrawable(null);
            }

        }

        /**
         * Load up the profile activity.
         */
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
            intent.putExtra("Parent", "UserActivity");
            activity.startActivity(intent);
        }

        /**
         * Loads up the chat activity.
         */
        private void startChat() {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId", chatList.get(getAdapterPosition()).getChatId());
            intent.putExtra("currentUserName", currentUserName);
            intent.putExtra("Parent", "UserActivity");
            activity.startActivity(intent);
        }
    }

    private static final String LOG = InboxRecyclerViewAdapter.class.getSimpleName();
    private Context context;
    private LayoutInflater inflater;
    private List<Chat> chatList;
    private String currentUserName;
    private Activity activity;

    InboxRecyclerViewAdapter(Context context, Activity activity) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.activity = activity;
        chatList = new ArrayList<>();
        currentUserName = LocalStorage.getSharedPreferences(context).getString(Constants.NAME, "");
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

        // Determine the chat participants.
        for (User user : currentChat.getParticipants()) {
            if (Objects.equals(user.getName(), currentUserName)) {
                currentUser = user;
            } else {
                chatPartner = user;
            }
        }

        // Loads the avatar.
        if (chatPartner.getAvatar() != null) {
            ImageCacheLoader imageCacheLoader = new ImageCacheLoader(context);
            imageCacheLoader.loadBitmap(chatPartner.getAvatar(), ImageSize.LARGE)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> holder.avatar.setImageBitmap(bitmap),
                            throwable -> Log.d(LOG, throwable.getMessage())
                    );
        } else {
            holder.avatar.setImageResource(R.mipmap.user_avatar);
        }

        holder.name.setText(chatPartner.getName());

        // Initialise the preview messages.
        if (currentChat.getMessages().size() > 0)
            holder.preview.setText(currentChat.getMessages().get(currentChat.getMessages().size() - 1).getMessage());

        // Load the date of the last read message and the newest message and determine if there are unread messages
        long lastReadMsgTime = LocalStorage.getSharedPreferences(context).getLong(currentChat.getChatId(), -1);
        Date lastMsgRead = new Date(lastReadMsgTime);
        if (currentChat.getMessages().size() > 0) {
            Date previewDate = currentChat.getMessages().get(0).getDate();

            if (lastMsgRead.compareTo(previewDate) == -1 &&
                    !(currentChat.getMessages().get(currentChat.getMessages().size() - 1).getSender().equals(currentUser))) {
                holder.sendMsg.setImageResource(R.drawable.ic_email_alert_red);
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    /**
     * Updates the current chat list with the newer chats.
     * @param data New chats to be updated.
     */
    void updateChatList(List<Chat> data) {
        for (Chat currentNewChat : data) {
            if (containsChat(currentNewChat)) {
                for (int i = 0; i < chatList.size(); i++) {
                    if (chatList.get(i).getChatId().equals(currentNewChat.getChatId())) {
                        chatList.set(i, currentNewChat);
                        break;
                    }
                }
            } else
                chatList.add(currentNewChat);
        }

        notifyDataSetChanged();
    }

    /**
     * Determine if the given chat is already a part of the chat list.
     * @param chat2 Chat to look up in the chat list.
     * @return True - Chat is already in the chat list. False - this is a brand new chat.
     */
    private boolean containsChat(Chat chat2) {
        for (Chat chat : chatList)
            if (chat.getChatId().equals(chat2.getChatId()))
                return true;
        return false;
    }

    /**
     * Sets the initial data for the chat list.
     * @param data Initial chat data.
     */
    void setChatList(List<Chat> data) {
        chatList = new ArrayList<>(data);
        notifyDataSetChanged();
    }

    /**
     * Getter for the current chat list.
     * @return Current chat list.
     */
    List<Chat> getChatList() {
        return chatList;
    }
}
