package tk.internet.praktikum.foursquare.chat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Chat;
import tk.internet.praktikum.foursquare.api.bean.ChatMessage;
import tk.internet.praktikum.foursquare.api.bean.Message;
import tk.internet.praktikum.foursquare.api.service.ChatService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

public class ChatFragment extends Fragment {
    private ListView chatView;
    private ImageView sendBtn;
    private EditText inputMsg;
    private ChatListViewAdapter chatListViewAdapter;
    private String chatId;
    private Chat chat;
    private final String URL = "https://dev.ip.stimi.ovh/";
    private List<ChatMessage> messages = Collections.emptyList();
    private ChatMessage lastMsg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        chatId = args.getString("chatId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        chatView = (ListView) view.findViewById(R.id.chat_list_view);
        inputMsg = (EditText) view.findViewById(R.id.chat_input);
        sendBtn = (ImageView) view.findViewById(R.id.chat_send);

        ChatService service = ServiceFactory
                .createRetrofitService(ChatService.class, URL, LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.getConversation(chatId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            chatResponse -> {
                                chat = chatResponse;
                                messages = chat.getMessages();
                                if (messages.size() > 0) {
                                    lastMsg = messages.get(0);
                                    LocalStorage.getLocalStorageInstance(getActivity().getApplicationContext()).saveChatDate(chatId, lastMsg.getDate());
                                    Collections.reverse(messages);
                                }
                                chatListViewAdapter = new ChatListViewAdapter(messages, chat.getParticipants(), getActivity().getApplicationContext());
                                chatView.setAdapter(chatListViewAdapter);

                                updateLoop();
                            },
                            throwable -> Toast.makeText(getActivity().getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }

         sendBtn.setOnClickListener(v -> send());

        inputMsg.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND)
                    send();
                return true;
            }
        });

        return view;
    }

    private void updateLoop() {
        ChatService service = ServiceFactory
                .createRetrofitService(ChatService.class, URL, LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));


        try {
            service.getConversation(chatId, 0)
                    .repeatWhen(done -> done.delay(10, TimeUnit.SECONDS))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            chatResponse -> {
                                if (messages.size() > 0) {
                                    Date last = lastMsg.getDate();
                                    Date now = chatResponse.getMessages().get(0).getDate();

                                    if (last.compareTo(now) == -1) {
                                        messages.clear();
                                        messages.addAll(chatResponse.getMessages());
                                        lastMsg = messages.get(0);
                                        LocalStorage.getLocalStorageInstance(getActivity().getApplicationContext()).saveChatDate(chatId, lastMsg.getDate());
                                        Collections.reverse(messages);
                                        chatListViewAdapter.notifyDataSetChanged();
                                    }
                                }else {
                                    if (chatResponse.getMessages().size() > 0) {
                                        messages.clear();
                                        messages.addAll(chatResponse.getMessages());
                                        lastMsg = messages.get(0);
                                        LocalStorage.getLocalStorageInstance(getActivity().getApplicationContext()).saveChatDate(chatId, lastMsg.getDate());
                                        Collections.reverse(messages);
                                        chatListViewAdapter.notifyDataSetChanged();
                                    }
                                }

                            },
                            throwable -> Toast.makeText(getActivity().getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
        /*
        try {
            service.getConversation(chatId, messages.get(0).getId())
                    .repeatWhen(done -> done.delay(10, TimeUnit.SECONDS))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            chatResponse -> {
                                messages.addAll(chatResponse.getMessages());
                                chatListViewAdapter.notifyDataSetChanged();
                            },
                            throwable -> Toast.makeText(getActivity().getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private void send() {
        ChatService service = ServiceFactory
                .createRetrofitService(ChatService.class, URL, LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            Message msg = new Message(inputMsg.getText().toString());
            service.replyMessage(chatId, msg)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            chatResponse -> {
                                messages.add(chatResponse);
                                lastMsg = chatResponse;
                                chatListViewAdapter.notifyDataSetChanged();
                            },
                            throwable -> Toast.makeText(getActivity().getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }

        inputMsg.setText("");
    }
}
