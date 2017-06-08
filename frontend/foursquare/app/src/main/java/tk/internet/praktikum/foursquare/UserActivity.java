package tk.internet.praktikum.foursquare;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class UserActivity extends AppCompatActivity {
    TabLayout tabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        tabLayout= (TabLayout) findViewById(R.id.tabs);


        tabLayout.addTab(tabLayout.newTab().setText("Home").setIcon(R.mipmap.user_home));
        tabLayout.addTab(tabLayout.newTab().setText("Profile").setIcon(R.mipmap.user_profile));
        tabLayout.addTab(tabLayout.newTab().setText("History").setIcon(R.mipmap.user_history));
        tabLayout.addTab(tabLayout.newTab().setText("Friends").setIcon(R.mipmap.user_friends));
        tabLayout.addTab(tabLayout.newTab().setText("Inbox").setIcon(R.mipmap.user_message));



    }
}
