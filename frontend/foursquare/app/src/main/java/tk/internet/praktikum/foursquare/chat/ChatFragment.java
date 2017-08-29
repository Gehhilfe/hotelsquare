package tk.internet.praktikum.foursquare.chat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
    private static final String LOG = ChatFragment.class.getSimpleName();
    private ListView chatView;
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
        ImageView sendBtn = (ImageView) view.findViewById(R.id.chat_send);

        // Create the chat service.
        initialiseChat();

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

    /**
     * Initializes the chat by loading the messages for the current chat id from the server and
     * start the update loop to fetch incoming messages.
     */
    private void initialiseChat() {
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
                                    // Save the date of the last read message from this chat.
                                    LocalStorage.getLocalStorageInstance(getActivity().getApplicationContext()).saveChatDate(chatId, lastMsg.getDate());
                                    Collections.reverse(messages);
                                }
                                chatListViewAdapter = new ChatListViewAdapter(messages, chat.getParticipants(), getActivity().getApplicationContext());
                                chatView.setAdapter(chatListViewAdapter);

                                updateLoop();
                            },
                            throwable -> Log.d(LOG, throwable.getMessage())
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Periodically fetches the new incoming messages.
     */
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

                                    // Check if there is a newer message, update the adapter and the date from the last read message.
                                    if (last.compareTo(now) == -1) {
                                        messages.clear();
                                        messages.addAll(chatResponse.getMessages());
                                        lastMsg = messages.get(0);

                                        if (getActivity() == null)
                                            return;
                                        LocalStorage.getLocalStorageInstance(getActivity().getApplicationContext()).saveChatDate(chatId, lastMsg.getDate());

                                        Collections.reverse(messages);
                                        chatListViewAdapter.notifyDataSetChanged();
                                    }
                                } else {
                                    if (chatResponse.getMessages().size() > 0) {
                                        messages.clear();
                                        messages.addAll(chatResponse.getMessages());
                                        lastMsg = messages.get(0);
                                        if (getActivity() == null)
                                            return;
                                        LocalStorage.getLocalStorageInstance(getActivity().getApplicationContext()).saveChatDate(chatId, lastMsg.getDate());
                                        Collections.reverse(messages);
                                        chatListViewAdapter.notifyDataSetChanged();
                                    }
                                }

                            },
                            throwable -> Log.d(LOG, throwable.getMessage())
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends your last message to the server.
     */
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
                            throwable -> Log.d(LOG, throwable.getMessage())
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }

        inputMsg.setText("");
    }
}
