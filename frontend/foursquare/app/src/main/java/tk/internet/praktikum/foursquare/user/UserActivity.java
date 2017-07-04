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
        fragmentContainer = (ViewPager) findViewById(R.id.user_fragment_container);

        tabLayout.addTab(tabLayout.newTab().setText("Home").setIcon(R.mipmap.user_home));
        tabLayout.addTab(tabLayout.newTab().setText("Profile").setIcon(R.mipmap.user_profile));
        tabLayout.addTab(tabLayout.newTab().setText("History").setIcon(R.mipmap.user_history));
        tabLayout.addTab(tabLayout.newTab().setText("Friends").setIcon(R.mipmap.user_friends));
        tabLayout.addTab(tabLayout.newTab().setText("Inbox").setIcon(R.mipmap.user_message));

        userStatePagerAdapter = new UserStatePagerAdapter(getSupportFragmentManager());
        initialiseFragmentContainer(fragmentContainer);
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
