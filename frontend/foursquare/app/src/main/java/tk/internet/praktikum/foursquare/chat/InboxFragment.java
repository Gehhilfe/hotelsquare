package tk.internet.praktikum.foursquare.chat;

import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
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
import tk.internet.praktikum.foursquare.api.service.ProfileService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

public class InboxFragment extends Fragment {
    private RecyclerView recyclerView;
    private final String URL = "https://dev.ip.stimi.ovh/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ChatService service = ServiceFactory
                .createRetrofitService(ChatService.class, URL, LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.inbox_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        try {
            service.getConversations()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            inboxResponse -> {
                                recyclerView.setAdapter(new InboxRecylcerViewAdapter(getContext(), inboxResponse, this, getActivity()));
                            },
                            throwable -> {
                                Toast.makeText(getActivity().getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }
}
