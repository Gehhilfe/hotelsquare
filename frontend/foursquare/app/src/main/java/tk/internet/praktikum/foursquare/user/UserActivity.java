package tk.internet.praktikum.foursquare.user;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import tk.internet.praktikum.foursquare.MainActivity;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.VenueInDetailsNestedScrollView;
import tk.internet.praktikum.foursquare.chat.InboxFragment;
import tk.internet.praktikum.foursquare.friendlist.FriendListFragment;
import tk.internet.praktikum.foursquare.frequest.FriendRequestFragment;
import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.utils.LanguageHelper;



public class UserActivity extends AppCompatActivity  {
    private UserStatePagerAdapter userStatePagerAdapter;
    private ViewPager fragmentContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getApplicationContext());
        String language=sharedPreferences.getString("LANGUAGE","de");
        LanguageHelper.updateResources(this,language);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.me_toolbar);
        setSupportActionBar(toolbar);
        setTitle(getApplicationContext().getResources().getString(R.string.action_me));
        fragmentContainer = (ViewPager) findViewById(R.id.me_fragment_container);
        userStatePagerAdapter = new UserStatePagerAdapter(getSupportFragmentManager(), getApplicationContext());
        initialiseFragmentContainer(fragmentContainer);

        TabLayout tabLayout= (TabLayout) findViewById(R.id.me_tabs);
        tabLayout.setupWithViewPager(fragmentContainer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initialiseFragmentContainer(ViewPager container) {
        userStatePagerAdapter.addFragment(new ProfileFragment(), "Profile");
        userStatePagerAdapter.addFragment(new FriendRequestFragment(), "Friend Request");
        userStatePagerAdapter.addFragment(new FriendListFragment(), "Friend list");
        userStatePagerAdapter.addFragment(new InboxFragment(), "Chat");
        container.setAdapter(userStatePagerAdapter);
    }

    public ViewPager getViewPager() {
        return fragmentContainer;
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    @Override
    public Intent getParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    private Intent getParentActivityIntentImpl() {
        Intent i = null;
        Bundle bundle = getIntent().getExtras();
        String parentActivity = bundle.getString("Parent");

        if(parentActivity.equals("VenueInDetailsNestedScrollView")){
            i = new Intent(this, VenueInDetailsNestedScrollView.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        return i;
    }
}
