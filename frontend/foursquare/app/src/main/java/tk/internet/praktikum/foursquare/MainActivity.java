package tk.internet.praktikum.foursquare;

//import android.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import tk.internet.praktikum.foursquare.friendlist.DummyActivity;
import tk.internet.praktikum.foursquare.login.LoginActivity;
import tk.internet.praktikum.foursquare.search.FastSearchFragment;
import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.user.DummyProfile;
import tk.internet.praktikum.foursquare.user.MeFragment;
import tk.internet.praktikum.foursquare.user.UserActivity;

//import android.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        FastSearchFragment searchFragment = new FastSearchFragment();
        //getFragmentManager().beginTransaction().add(R.id.fragment_container, searchFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, searchFragment).commit();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.nav_search) {
            // call Search fast activity
            try {
                fragment = FastSearchFragment.class.newInstance();
                redirectToFragment(fragment);
                setTitle(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.nav_history) {
            // call history activity
            Intent intent = new Intent(getApplicationContext(), UserActivity.class);
            startActivityForResult(intent, 0);
        } else if (id == R.id.nav_me) {
            // call login activity if didn't login util now
            if (!LocalStorage.getLocalStorageInstance(getApplicationContext()).isLoggedIn()) {
               Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(intent, 0);
               /*try {
                    fragment = LoginGeneralFragment.class.newInstance();
                    redirectToFragment(fragment);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }*/
            } else {
                try {
                fragment = MeFragment.class.newInstance();
                redirectToFragment(fragment);
                    setTitle(item);
                }
                catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }


            }


            // Insert the fragment by replacing any existing fragment

    }else if (id == R.id.nav_manage) {
            // call history activity
            Intent intent = new Intent(getApplicationContext(), DummyProfile.class);
            startActivityForResult(intent, 0);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void redirectToFragment(Fragment fragment){
            //FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

    }
    private  void setTitle(MenuItem item){
        item.setChecked(true);
        // Set action bar title
        setTitle(item.getTitle());

    }




}
