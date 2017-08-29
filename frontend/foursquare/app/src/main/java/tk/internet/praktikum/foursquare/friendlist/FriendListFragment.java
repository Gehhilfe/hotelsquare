package tk.internet.praktikum.foursquare.friendlist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.ProfileService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

public class FriendListFragment extends Fragment {
    private static final String LOG = FriendListFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private TextView emptyFriendList;
    private final String URL = "https://dev.ip.stimi.ovh/";
    private FLRecyclerViewAdapter flRecyclerViewAdapter;
    private LinearLayoutManager linearLayoutManager;
    private int page, visibleItemCount, itemCount, lastVisibleItemPosition;
    private int maxLastVisibleItemPosition = 0;
    private boolean done, refresh;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friendlist, container, false);
        emptyFriendList = (TextView) view.findViewById(R.id.friendlist_empty_view);
        recyclerView = (RecyclerView) view.findViewById(R.id.fl_recyclerview);

        linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        flRecyclerViewAdapter = new FLRecyclerViewAdapter(getContext(), getActivity());
        recyclerView.setAdapter(flRecyclerViewAdapter);

        // Starts to load more data pages from the server depending on the scroll position.
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

                if(dy > 0 && (lastVisibleItemPosition + visibleItemCount) >= itemCount && lastVisibleItemPosition % 10 == 9 && !done){
                    maxLastVisibleItemPosition = Math.max(maxLastVisibleItemPosition, lastVisibleItemPosition);
                    if (refresh && !done)
                        checkForUpdates();
                    else if (!done)
                        loadFriendList();
                }
            }
        });

        refresh = false;
        done = false;
        page = 0;
        loadFriendList();

        return view;
    }

    /**
     * Setter for the variable that determines if the refresh function is running.
     * @param refresh Running/ Not running
     */
    private void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    /**
     * Setter for the variable that determines if the refresh function is done.
     * @param done Done/ Not done
     */
    private void setDone(boolean done) {
        this.done = done;
    }

    /**
     * Increases the page parameter.
     */
    private void increasePage() {
        page++;
    }

    /**
     * Resets the page parameter.
     */
    private void resetPage() {
        page = 0;
    }

    /**
     * Loads the initial page of the friend list.
     */
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

                                if (friendList.size() > 0) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    emptyFriendList.setVisibility(View.GONE);
                                    increasePage();
                                } else if (friendList.size() == 0 && page > 0){
                                    setDone(true);
                                } else {
                                    recyclerView.setVisibility(View.GONE);
                                    emptyFriendList.setVisibility(View.VISIBLE);
                                    setDone(true);
                                }

                                flRecyclerViewAdapter.updateList(friendList);
                            },
                            throwable -> Log.d(LOG, throwable.getMessage())
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks for new accepted friends and fetches data from the given page.
     */
    private void checkForUpdates() {
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
                                List<User> fList = flRecyclerViewAdapter.getFriendList();

                                if (friendList.size() > 0) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    emptyFriendList.setVisibility(View.GONE);
                                    increasePage();
                                } else if (friendList.size() == 0 && page > 0){
                                    setDone(true);
                                    setRefresh(true);
                                } else {
                                    recyclerView.setVisibility(View.GONE);
                                    emptyFriendList.setVisibility(View.VISIBLE);
                                    setRefresh(true);
                                    setDone(true);
                                }

                                // remove old friends from the update list
                                for (Iterator<User> iterator = friendList.listIterator(); iterator.hasNext(); ) {
                                    User tmpUser = iterator.next();
                                    if (fList.contains(tmpUser))
                                        iterator.remove();
                                }

                                if (friendList.size() > 0)
                                    flRecyclerViewAdapter.updateList(friendList);
                            },
                            throwable -> Log.d(LOG, throwable.getMessage())
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // Workaround to refresh the friend requests when loading up the fragment from the adjacent fragments.
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null)
            if (isVisibleToUser) {
                refresh = true;
                setDone(false);
                resetPage();
                checkForUpdates();
            }

    }
}
