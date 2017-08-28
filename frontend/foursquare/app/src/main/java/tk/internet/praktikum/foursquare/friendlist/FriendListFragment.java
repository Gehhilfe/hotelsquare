package tk.internet.praktikum.foursquare.friendlist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BooleanSupplier;
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
    private TextView emptyFriendList;
    private final String URL = "https://dev.ip.stimi.ovh/";
    private FLRecyclerViewAdapter flRecyclerViewAdapter;
    private LinearLayoutManager linearLayoutManager;
    private int page;
    private int visibleItemCount;
    private int itemCount;
    private int lastVisibleItemPosition;
    private int firstVisibleItem;
    private int maxLastVisibleItemPosition=0;
    private boolean done;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friendlist, container, false);
        emptyFriendList = (TextView) view.findViewById(R.id.friendlist_empty_view);
        recyclerView = (RecyclerView) view.findViewById(R.id.fl_recyclerview);

        linearLayoutManager = new LinearLayoutManager(getActivity());
        firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        flRecyclerViewAdapter = new FLRecyclerViewAdapter(getContext(), getActivity());
        recyclerView.setAdapter(flRecyclerViewAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = linearLayoutManager.getChildCount();
                itemCount = linearLayoutManager.getItemCount();
                lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                if(dy > 0 && (lastVisibleItemPosition + visibleItemCount) >= itemCount && lastVisibleItemPosition % 10 ==9 && !done){
                    maxLastVisibleItemPosition = Math.max(maxLastVisibleItemPosition, lastVisibleItemPosition);
                    loadFriendList();
                }
            }
        });

        done = false;
        page = 0;
        loadFriendList();

        return view;
    }

    private void setDone() {
        done = true;
    }

    private void increasePage() {
        page++;
    }

    private void loadFriendList() {
        ProfileService service = ServiceFactory
                .createRetrofitService(ProfileService.class, URL, LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.friends(page)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            friendListResponse -> {
                                List<User> friendList = friendListResponse.getFriends();

                                Collections.sort(friendList,new Comparator<User>() {
                                    @Override
                                    public int compare(User o1, User o2) {
                                        return o1.getName().compareTo(o2.getName());
                                    }
                                });

                                if (friendList.size() > 0) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    emptyFriendList.setVisibility(View.GONE);
                                    increasePage();
                                } else if (friendList.size() == 0 && page > 0){
                                    setDone();
                                } else {
                                    recyclerView.setVisibility(View.GONE);
                                    emptyFriendList.setVisibility(View.VISIBLE);
                                    setDone();
                                }

                                flRecyclerViewAdapter.updateList(friendList);
                            },
                            throwable -> {
                                Toast.makeText(getActivity().getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
