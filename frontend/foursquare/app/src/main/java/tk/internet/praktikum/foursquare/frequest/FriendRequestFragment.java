package tk.internet.praktikum.foursquare.frequest;

import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import tk.internet.praktikum.foursquare.api.bean.FriendRequest;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.ProfileService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

public class FriendRequestFragment extends Fragment {
    private static final String LOG = FriendRequestFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private TextView emptyRequests;
    private final String URL = "https://dev.ip.stimi.ovh/";
    private FriendRequestRecyclerViewAdapter friendRequestRecyclerViewAdapter;
    private LinearLayoutManager linearLayoutManager;
    private int page, visibleItemCount, itemCount, lastVisibleItemPosition;
    private int maxLastVisibleItemPosition = 0;
    private boolean done, refresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        emptyRequests = (TextView) view.findViewById(R.id.home_empty_view);
        recyclerView = (RecyclerView) view.findViewById(R.id.home_recyclerview);

        linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        friendRequestRecyclerViewAdapter = new FriendRequestRecyclerViewAdapter(getContext());
        recyclerView.setAdapter(friendRequestRecyclerViewAdapter);

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
                        loadFriendRequests();
                }
            }
        });

        refresh = false;
        done = false;
        page = 0;

        loadFriendRequests();

        return view;
    }

    /**
     * Checks for new friend requests and fetches data from the given page.
     */
    private void checkForUpdates() {
        ProfileService service = ServiceFactory
                .createRetrofitService(ProfileService.class, URL, LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.getFriendRequests(page)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            userList -> {
                                try {
                                    service.profile()
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(
                                                    user -> {
                                                        List<FriendRequest> tmpRequestList = user.getFriendRequests();
                                                        List<User> adapterUserList = friendRequestRecyclerViewAdapter.getUserList();

                                                        // determine the page state of the friend requests.
                                                        if (userList.size() > 0) {
                                                            recyclerView.setVisibility(View.VISIBLE);
                                                            emptyRequests.setVisibility(View.GONE);
                                                            increasePage();
                                                        } else if (userList.size() == 0 && page > 0){
                                                            setDone(true);
                                                            setRefresh(true);
                                                        } else {
                                                            recyclerView.setVisibility(View.GONE);
                                                            emptyRequests.setVisibility(View.VISIBLE);
                                                            setDone(true);
                                                            setRefresh(true);
                                                        }

                                                        // removes invalid friend requests
                                                        for (Iterator<User> iterator = userList.listIterator(); iterator.hasNext(); ) {
                                                            User tmpUser = iterator.next();
                                                            if(tmpUser.getId() == null)
                                                                iterator.remove();
                                                        }

                                                        if (userList.size() < tmpRequestList.size())
                                                            removeInvalidRequests(userList, tmpRequestList);

                                                        // removes already existing friend requests.
                                                        for (Iterator<User> iterator = userList.listIterator(); iterator.hasNext(); ) {
                                                            User tmpUser = iterator.next();
                                                            if (adapterUserList.contains(tmpUser))
                                                                iterator.remove();
                                                        }

                                                        if (userList.size() < tmpRequestList.size())
                                                            removeInvalidRequests(userList, tmpRequestList);

                                                        // updates the current list of friend requests.
                                                        if (userList.size() > 0)
                                                            friendRequestRecyclerViewAdapter.updateData(tmpRequestList, userList);
                                                        setDone(true);
                                                    },
                                                    throwable -> Log.d(LOG, throwable.getMessage())
                                            );
                                }catch (Exception e) {
                                    e.printStackTrace();
                                }
                            },
                            throwable -> Log.d(LOG, throwable.getMessage())
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes invalid/old friend request and their senders.
     * @param userList Sender of the friend requests.
     * @param tmpRequestList Friend requests to be removed
     */
    private void removeInvalidRequests(List<User> userList, List<FriendRequest> tmpRequestList) {
        for (Iterator<FriendRequest> iterator = tmpRequestList.listIterator(); iterator.hasNext(); ) {
            boolean valid = false;
            FriendRequest friendRequest = iterator.next();
            for (User tmpUser : userList) {
                if (friendRequest.getSenderID().equals(tmpUser.getId())) {
                    valid = true;
                    break;
                }
            }
            if (!valid)
                iterator.remove();
        }
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
     * Loads the initial page of friend requests.
     */
    private void loadFriendRequests() {
        ProfileService service = ServiceFactory
                .createRetrofitService(ProfileService.class, URL, LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.getFriendRequests(page)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            userList -> {
                                try {
                                    service.profile()
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(
                                                    user -> {
                                                        List<FriendRequest> tmpRequestList = user.getFriendRequests();

                                                        for (Iterator<User> iterator = userList.listIterator(); iterator.hasNext(); ) {
                                                            User tmpUser = iterator.next();
                                                            if(tmpUser.getId() == null)
                                                                iterator.remove();
                                                        }


                                                        if (userList.size() < tmpRequestList.size()) {
                                                            if (userList.size() < tmpRequestList.size())
                                                                removeInvalidRequests(userList, tmpRequestList);
                                                        }

                                                        if (userList.size() > 0) {
                                                            recyclerView.setVisibility(View.VISIBLE);
                                                            emptyRequests.setVisibility(View.GONE);
                                                            increasePage();
                                                        } else if (userList.size() == 0 && page > 0){
                                                            setDone(true);
                                                        } else {
                                                            recyclerView.setVisibility(View.GONE);
                                                            emptyRequests.setVisibility(View.VISIBLE);
                                                            setDone(true);
                                                        }

                                                        friendRequestRecyclerViewAdapter.updateData(user.getFriendRequests(), userList);
                                                        setDone(true);
                                                    },
                                                    throwable -> Log.d(LOG, throwable.getMessage())
                                            );
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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
