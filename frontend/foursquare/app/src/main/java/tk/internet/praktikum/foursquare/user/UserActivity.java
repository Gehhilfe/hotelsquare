package tk.internet.praktikum.foursquare.user;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import tk.internet.praktikum.foursquare.MainActivity;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.chat.InboxFragment;
import tk.internet.praktikum.foursquare.friendlist.FriendListFragment;
import tk.internet.praktikum.foursquare.history.dummy.DummyHistoryFragment;
import tk.internet.praktikum.foursquare.home.HomeFragment;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

public class UserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private UserStatePagerAdapter userStatePagerAdapter;
    private ViewPager fragmentContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.me_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setTitle("Me");

        fragmentContainer = (ViewPager) findViewById(R.id.me_fragment_container);
        userStatePagerAdapter = new UserStatePagerAdapter(getSupportFragmentManager(), getApplicationContext());
        initialiseFragmentContainer(fragmentContainer);

        TabLayout tabLayout= (TabLayout) findViewById(R.id.me_tabs);
        tabLayout.setupWithViewPager(fragmentContainer);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_search:
                setResult(0, null);
                finish();
                break;
            case R.id.nav_search_person:
                setResult(1, null);
                finish();
                break;
            case R.id.nav_history:
                setResult(2, null);
                finish();
                break;
            case R.id.nav_me:
                setResult(3, null);
                finish();
                break;
            case R.id.nav_manage:
                setResult(4, null);
                finish();
                break;
            case R.id.nav_logout:
                logout();
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
