package tk.internet.praktikum.foursquare.chat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import tk.internet.praktikum.foursquare.R;

public class ChatFragment extends Fragment {
    private ListView chatView;
    private ImageView sendBtn;
    private EditText inputMsg;

    public ChatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO - INITIALISE CHAT
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        chatView = (ListView) view.findViewById(R.id.chat_list_view);
        inputMsg = (EditText) view.findViewById(R.id.chat_input);
        sendBtn = (ImageView) view.findViewById(R.id.chat_send);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }
}
