package tk.internet.praktikum.foursquare.chat;

import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.MainActivity;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Chat;
import tk.internet.praktikum.foursquare.api.bean.FriendRequest;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.ChatService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.user.UserActivity;

public class InboxFragment extends Fragment {
    public static final String LOG = InboxFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private TextView emptyChat;
    private final String URL = "https://dev.ip.stimi.ovh/";
    private InboxRecylcerViewAdapter inboxRecylcerViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        emptyChat = (TextView) view.findViewById(R.id.inbox_empty_view);
        recyclerView = (RecyclerView) view.findViewById(R.id.inbox_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        inboxRecylcerViewAdapter = new InboxRecylcerViewAdapter(getContext(), getActivity());
        recyclerView.setAdapter(inboxRecylcerViewAdapter);

        loadInbox();

        return view;
    }

    private void loadInbox() {
        ChatService service = ServiceFactory
                .createRetrofitService(ChatService.class, URL, LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.getConversations()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            inboxResponse -> {
                                if (inboxResponse.size() > 0) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    emptyChat.setVisibility(View.GONE);
                                } else {
                                    recyclerView.setVisibility(View.GONE);
                                    emptyChat.setVisibility(View.VISIBLE);
                                }

                                inboxRecylcerViewAdapter.setChatList(inboxResponse);
                            },
                            throwable -> {
                                Toast.makeText(getActivity().getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkForUpdates() {
        ChatService service = ServiceFactory
                .createRetrofitService(ChatService.class, URL, LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.getConversations()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            inboxResponse -> {
                                if (inboxResponse.size() > 0) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    emptyChat.setVisibility(View.GONE);
                                } else {
                                    recyclerView.setVisibility(View.GONE);
                                    emptyChat.setVisibility(View.VISIBLE);
                                }
                                List<Chat> responseList = inboxResponse;
                                List<Chat> inboxList = inboxRecylcerViewAdapter.getChatList();

                                for (Iterator<Chat> iterator = responseList.listIterator(); iterator.hasNext(); ) {
                                    boolean newMessages = false;
                                    Chat currentChat = iterator.next();
                                    for (Chat tmpChat : inboxList) {
                                        if (currentChat.getMessages().get(0).getId().equals(tmpChat.getMessages().get(0).getId())) {
                                            newMessages = true;
                                            break;
                                        }
                                    }

                                    if (newMessages)
                                        iterator.remove();
                                }

                                if (responseList.size() > 0)
                                    inboxRecylcerViewAdapter.updateChatList(inboxResponse);
                            },
                            throwable -> {
                                Toast.makeText(getActivity().getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        if (getActivity() != null && getActivity() instanceof UserActivity)
            if (((UserActivity) getActivity()).getViewPager().getCurrentItem() == 2) {
                super.onResume();
            } else {
                checkForUpdates();
                super.onResume();
            }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null)
            if (isVisibleToUser) {
                checkForUpdates();
            }

    }


}
