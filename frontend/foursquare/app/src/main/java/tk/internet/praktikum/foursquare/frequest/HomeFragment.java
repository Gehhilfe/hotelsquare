package tk.internet.praktikum.foursquare.frequest;

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

import org.w3c.dom.Text;

import java.util.ArrayList;
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

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView emptyRequests;
    private final String URL = "https://dev.ip.stimi.ovh/";
    private HomeRecyclerViewAdapter homeRecyclerViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        emptyRequests = (TextView) view.findViewById(R.id.home_empty_view);
        recyclerView = (RecyclerView) view.findViewById(R.id.home_recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        homeRecyclerViewAdapter = new HomeRecyclerViewAdapter(getContext());
        recyclerView.setAdapter(homeRecyclerViewAdapter);

        loadFriendRequests();


        return view;
    }

    private void loadFriendRequests() {
        ProfileService service = ServiceFactory
                .createRetrofitService(ProfileService.class, URL, LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.getFriendRequests(0)
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

                                                        if (userList.size() > 0 && tmpRequestList.size() > 0) {
                                                            recyclerView.setVisibility(View.VISIBLE);
                                                            emptyRequests.setVisibility(View.GONE);
                                                        } else {
                                                            recyclerView.setVisibility(View.GONE);
                                                            emptyRequests.setVisibility(View.VISIBLE);
                                                        }

                                                        homeRecyclerViewAdapter.setResults(user.getFriendRequests(), userList);
                                                        homeRecyclerViewAdapter.notifyDataSetChanged();
                                                    },
                                                    throwable -> {
                                                        Toast.makeText(getActivity().getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                            );
                                }catch (Exception e) {
                                    e.printStackTrace();
                                }
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
