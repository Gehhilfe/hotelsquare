package tk.internet.praktikum.foursquare.friendlist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.ProfileService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

//import android.app.Fragment;

public class FriendListFragment extends Fragment {
    private RecyclerView recyclerView;
    private final String URL = "https://dev.ip.stimi.ovh/";
    private FLRecyclerViewAdapter flRecyclerViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ProfileService service = ServiceFactory
                .createRetrofitService(ProfileService.class, URL, LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

        View view = inflater.inflate(R.layout.fragment_friendlist, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.fl_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        flRecyclerViewAdapter = new FLRecyclerViewAdapter(getContext(), getActivity());
        recyclerView.setAdapter(flRecyclerViewAdapter);


        try {
            service.friends(0)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            friendListResponse -> {
                                List<User> friendList = friendListResponse.getFriends();
 /*                               friendList.sort(new Comparator<User>() {
                                    @Override
                                    public int compare(User o1, User o2) {
                                        return o1.getName().compareTo(o2.getName());
                                    }
                                });*/
                                Collections.sort(friendList,new Comparator<User>() {
                                    @Override
                                    public int compare(User o1, User o2) {
                                        return o1.getName().compareTo(o2.getName());
                                    }
                                });
                                //recyclerView.setAdapter(new FLRecyclerViewAdapter(getContext(), friendList, getActivity()));
                                flRecyclerViewAdapter.setResults(friendList);
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
