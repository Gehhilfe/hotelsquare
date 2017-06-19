package tk.internet.praktikum.foursquare.user;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import tk.internet.praktikum.foursquare.MainActivity;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.storage.LocalStorage;


public class MeFragment extends Fragment {

    TabLayout tabLayout;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_me, container, false);
        setHasOptionsMenu(true);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Home").setIcon(R.mipmap.user_home));
        tabLayout.addTab(tabLayout.newTab().setText("Profile").setIcon(R.mipmap.user_profile));
        tabLayout.addTab(tabLayout.newTab().setText("History").setIcon(R.mipmap.user_history));
        tabLayout.addTab(tabLayout.newTab().setText("Friends").setIcon(R.mipmap.user_friends));
        tabLayout.addTab(tabLayout.newTab().setText("Inbox").setIcon(R.mipmap.user_message));
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        final MenuItem itemLogout = menu.findItem(R.id.logout);
        itemLogout.setVisible(true);
        itemLogout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Todo
                // call logout service
                LocalStorage.getLocalStorageInstance(getActivity().getApplicationContext()).deleteLoggedInInformation();
                Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                startActivityForResult(intent, 0);
                return true; //
            }
        });

    }


}
