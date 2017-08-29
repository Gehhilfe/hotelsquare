package tk.internet.praktikum.foursquare.search;


import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import tk.internet.praktikum.foursquare.R;

public class SearchPersonActivity extends AppCompatActivity {
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_person);
        Toolbar toolbar = (Toolbar) findViewById(R.id.search_person_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        setTitle(getApplicationContext().getResources().getString(R.string.action_search_person));

        fragment = new PersonSearchFragment();
        addFragment();
    }

    public void addFragment() {
        getSupportFragmentManager().beginTransaction().add(R.id.search_person_activity_container, fragment).commit();
    }
}
