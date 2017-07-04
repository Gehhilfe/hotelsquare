package tk.internet.praktikum.foursquare.user;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.UserStatePagerAdapter;
import tk.internet.praktikum.foursquare.friendlist.FriendListFragment;

public class UserActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private UserStatePagerAdapter userStatePagerAdapter;
    private ViewPager fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        tabLayout= (TabLayout) findViewById(R.id.tabs);

        //fragmentContainer = (ViewPager) findViewById(R.id.user_fragment_container);

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

        userStatePagerAdapter = new UserStatePagerAdapter(getSupportFragmentManager());
        //initialiseFragmentContainer(fragmentContainer);
    }

    private void initialiseFragmentContainer(ViewPager container) {
        userStatePagerAdapter.addFragment(new ProfileFragment(), "Profile");
        userStatePagerAdapter.addFragment(new FriendListFragment(), "Friend list");
        container.setAdapter(userStatePagerAdapter);
    }

    public void setFragment(int fragmentId) {
        fragmentContainer.setCurrentItem(fragmentId);
    }


}
