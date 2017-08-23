package tk.internet.praktikum.foursquare.user;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.akexorcist.localizationactivity.LocalizationActivity;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.utils.AdjustedContextWrapper;

public class SettingsActivity extends LocalizationActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Fragment fragment;

    @Override
     public void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getApplicationContext());
        String language=sharedPreferences.getString("LANGUAGE","de");
        System.out.println("SettingsActivity onCreate Language: "+language);
        AdjustedContextWrapper.wrap(getBaseContext(),language);
        getApplicationContext().getResources().getConfiguration().getLocales();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setTitle(getApplicationContext().getResources().getString(R.string.action_settings));
        fragment=new SettingsFragment();
        addFragment();

    }

     public void addFragment() {
       getSupportFragmentManager().beginTransaction().add(R.id.settings_activity_container, fragment).commit();
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

   @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(newBase);
        String language=sharedPreferences.getString("LANGUAGE","de");
        System.out.println("Language: "+language);
        super.attachBaseContext(AdjustedContextWrapper.wrap(newBase,language));
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getApplicationContext());
        String language=sharedPreferences.getString("LANGUAGE","de");
        System.out.println("###### SettingsActivity onConfigurationChanged Language: "+language);
        AdjustedContextWrapper.wrap(getBaseContext(),language);
        super.onConfigurationChanged(newConfig);


    }


}
