package tk.internet.praktikum.foursquare.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.Chat;
import tk.internet.praktikum.foursquare.api.bean.ChatMessage;
import tk.internet.praktikum.foursquare.api.bean.Message;
import tk.internet.praktikum.foursquare.api.bean.User;

public class ChatListViewAdapter extends BaseAdapter {

    private class ViewHolder {
        public TextView messageView;
        public TextView time;
        public TextView user;
    }

    private String LOG_TAG = ChatListViewAdapter.class.getSimpleName();
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm");
    private ArrayList<ChatMessage> messages;
    private Context context;
    private User currentUser;

    public ChatListViewAdapter(ArrayList<ChatMessage> messages, User currentUser, Context context) {
        this.messages = messages;
        this.context = context;
        this.currentUser = currentUser;
    }

    public ChatListViewAdapter() {
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ChatMessage message = messages.get(position);
        ViewHolder holder1;
        ViewHolder holder2;

        // MSG = SENT => Participant = current
        if (true) {
            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(R.layout.chat_send_msg, null, false);
                holder1 = new ViewHolder();


                holder1.messageView = (TextView) view.findViewById(R.id.message_text);
                holder1.time = (TextView) view.findViewById(R.id.time_text);

                view.setTag(holder1);
            } else {
                view = convertView;
                holder1 = (ViewHolder) view.getTag();
            }

            //holder1.messageView.setText(message.getMessage()); // TODO ADD SEND MSG
            holder1.time.setText(SIMPLE_DATE_FORMAT.format("Sent time")); // TODO - ADD TIME

            // MSG = REC => PART != current
        } else if (true) {

            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(R.layout.chat_received_msg, null, false);

                holder2 = new ViewHolder();


                holder2.messageView = (TextView) view.findViewById(R.id.message_text);
                holder2.time = (TextView) view.findViewById(R.id.time_text);
                holder2.user = (TextView) view.findViewById(R.id.chat_user);
                view.setTag(holder2);
            } else {
                view = convertView;
                holder2 = (ViewHolder) view.getTag();
            }

            holder2.user.setText("USER"); // TODO - ADD OTHER USERNAME
            //holder2.messageView.setText(message.getMessage()); // TODO ADD RECEIVED MSG
            holder2.time.setText(SIMPLE_DATE_FORMAT.format("SENT TIME")); // TODO - ADD TIME
        }
        return view;
    }

}

