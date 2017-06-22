package tk.internet.praktikum.foursquare.friendlist;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.User;

public class FriendListFragment extends Fragment {
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        List<User> users = new ArrayList<>();
        users.add(new User("Alex", ""));
        users.add(new User("Bob", ""));
        users.add(new User("Chris", ""));
        users.add(new User("Dennis", ""));
        users.add(new User("Erik", ""));
        users.add(new User("Frank", ""));
        users.add(new User("Guido", ""));
        users.add(new User("Hans", ""));
        users.add(new User("Ingo", ""));
        users.add(new User("Jan", ""));
        users.add(new User("Klaus", ""));
        users.add(new User("Lennard", ""));
        users.add(new User("Max", ""));
        users.add(new User("Nik", ""));
        users.add(new User("Otto", ""));
        users.add(new User("Peter", ""));
        users.add(new User("Quin", ""));
        users.add(new User("Rolf", ""));
        users.add(new User("Stefan", ""));
        users.add(new User("Tom", ""));
        users.add(new User("Uwe", ""));


        //View view = inflater.inflate(R.layout.fragment_friendlist, container, false);
        View view = inflater.inflate(R.layout.content_user, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.fl_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new FLRecyclerViewAdapter(getContext(), users));
        return view;
    }
}
