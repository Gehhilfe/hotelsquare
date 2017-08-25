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

public class SearchPersonActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setTitle(getApplicationContext().getResources().getString(R.string.action_search_person));

        fragment = new PersonSearchFragment();
        // TODO - initialise fragment
        addFragment();
    }

    public void addFragment() {
        getSupportFragmentManager().beginTransaction().add(R.id.search_person_activity_container, fragment).commit();
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
            case R.id.nav_login_logout:
                setResult(5, null);
                finish();
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
/*    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(newBase);
        String language=sharedPreferences.getString("LANGUAGE","de");
        super.attachBaseContext(AdjustedContextWrapper.wrap(newBase,language));
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getBaseContext());
        String language=sharedPreferences.getString("LANGUAGE","de");
        Locale locale=new Locale(language);
        AdjustedContextWrapper.wrap(getBaseContext(),language);

    }*/
}
