package tk.internet.praktikum.foursquare.user;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.abstracts.DrawerLayoutActivity;

public class UserActivity extends DrawerLayoutActivity {
    TabLayout tabLayout;
    //TabLayout.Tab homeTab;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);


        tabLayout= (TabLayout) findViewById(R.id.tabs);

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




    }
}
