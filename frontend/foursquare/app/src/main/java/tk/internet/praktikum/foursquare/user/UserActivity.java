package tk.internet.praktikum.foursquare.user;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import tk.internet.praktikum.foursquare.MainActivity;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.chat.InboxFragment;
import tk.internet.praktikum.foursquare.friendlist.FriendListFragment;
import tk.internet.praktikum.foursquare.home.HomeFragment;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

//import tk.internet.praktikum.foursquare.history.dummy.DummyHistoryFragment;

public class UserActivity extends AppCompatActivity  {
    private UserStatePagerAdapter userStatePagerAdapter;
    private ViewPager fragmentContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.me_toolbar);
        setSupportActionBar(toolbar);


        setTitle("Me");

        fragmentContainer = (ViewPager) findViewById(R.id.me_fragment_container);
        userStatePagerAdapter = new UserStatePagerAdapter(getSupportFragmentManager(), getApplicationContext());
        initialiseFragmentContainer(fragmentContainer);

        TabLayout tabLayout= (TabLayout) findViewById(R.id.me_tabs);
        tabLayout.setupWithViewPager(fragmentContainer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void logout() {
        LocalStorage.getLocalStorageInstance(getApplicationContext()).deleteLoggedInInformation();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void initialiseFragmentContainer(ViewPager container) {
        userStatePagerAdapter.addFragment(new HomeFragment(), "Home");
        userStatePagerAdapter.addFragment(new ProfileFragment(), "Profile");
        // userStatePagerAdapter.addFragment(new DummyHistoryFragment(), "History");
        userStatePagerAdapter.addFragment(new FriendListFragment(), "Friend list");
        userStatePagerAdapter.addFragment(new InboxFragment(), "Chat");
        container.setAdapter(userStatePagerAdapter);
    }
}
