package tk.internet.praktikum.foursquare.user;

//import android.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import tk.internet.praktikum.foursquare.MainActivity;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.friendlist.FriendListFragment;
import tk.internet.praktikum.foursquare.storage.LocalStorage;


public class MeFragment extends Fragment {

    private TabLayout tabLayout;
    private UserStatePagerAdapter userStatePagerAdapter;
    private ViewPager fragmentContainer;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_me, container, false);
        setHasOptionsMenu(true);
        tabLayout= (TabLayout) view.findViewById(R.id.tabs);

        fragmentContainer = (ViewPager) view.findViewById(R.id.user_fragment_container);

        TabLayout.Tab homeTab=tabLayout.newTab();

        homeTab.setText("Home").setIcon(R.mipmap.user_home);
        TabLayout.Tab profileTab=tabLayout.newTab();
        profileTab.setText("Profile").setIcon(R.mipmap.user_profile);
        TabLayout.Tab historyTab=tabLayout.newTab();
        historyTab.setText("History").setIcon(R.mipmap.user_history);

        TabLayout.Tab friendsTab=tabLayout.newTab();
        friendsTab.setText("Friends").setIcon(R.mipmap.user_friends);

        TabLayout.Tab inbox=tabLayout.newTab();
        inbox.setText("Inbox").setIcon(R.mipmap.user_message);

        tabLayout.addTab(homeTab);
        tabLayout.addTab(profileTab);
        tabLayout.addTab(historyTab);
        tabLayout.addTab(friendsTab);
        tabLayout.addTab(inbox);

        userStatePagerAdapter = new UserStatePagerAdapter(getFragmentManager(), getContext());
        initialiseFragmentContainer(fragmentContainer);
        fragmentContainer.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(fragmentContainer);
        tabLayout.addOnTabSelectedListener(createOnTabSelectedListener());
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

    private void initialiseFragmentContainer(ViewPager container) {
        userStatePagerAdapter.addFragment(new ProfileFragment(), "Profile");
        userStatePagerAdapter.addFragment(new FriendListFragment(), "Friend list");
        container.setAdapter(userStatePagerAdapter);
    }

    public void setFragment(int fragmentId) {
        fragmentContainer.setCurrentItem(fragmentId);
    }

    public  TabLayout.OnTabSelectedListener createOnTabSelectedListener(){
        return new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setFragment(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        };
    }



}
