package tk.internet.praktikum.foursquare;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Location;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.UserService;
import tk.internet.praktikum.foursquare.location.LocationService;
import tk.internet.praktikum.foursquare.location.LocationTracker;
import tk.internet.praktikum.foursquare.login.LoginActivity;
import tk.internet.praktikum.foursquare.search.FastSearchFragment;
import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.user.MeFragment;
import tk.internet.praktikum.foursquare.user.ProfileActivity;
import tk.internet.praktikum.foursquare.user.UserActivity;

//import android.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final int REQUEST_LOGIN = 0;
    private final int REQUEST_CHAT = 1;
    private final int REQUEST_PROFILE = 2;
    private MenuItem searchMenu, meMenu;

    private Location userLocation = new Location(0, 0);
    private Handler handler = new Handler();
    private final String URL = "https://dev.ip.stimi.ovh/";
    private User locationUser = new User();
    private int PARAM_INTERVAL = 10000;

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

        Menu tmpMenu = navigationView.getMenu();
        for (int i = 0; i < tmpMenu.size(); i++) {
            tmpMenu.getItem(i);
            if (tmpMenu.getItem(i).getItemId() == R.id.nav_search) {
                searchMenu = tmpMenu.getItem(i);
            } else if (tmpMenu.getItem(i).getItemId() == R.id.nav_me) {
                meMenu = tmpMenu.getItem(i);
            }
        }

        FastSearchFragment searchFragment = new FastSearchFragment();
        redirectToFragment(searchFragment);

        handler.postDelayed(sendLocation, PARAM_INTERVAL);
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

    private void searchNavigation(MenuItem item) {
        try {
            Fragment fragment = FastSearchFragment.class.newInstance();
            redirectToFragment(fragment);
            setTitle(item);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void historyNavigation() {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra("userID", "");
        startActivityForResult(intent, 0);
    }

    private void meNavigation(MenuItem item) {
        if (!LocalStorage.getLocalStorageInstance(getApplicationContext()).isLoggedIn()) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivityForResult(intent, REQUEST_LOGIN);
        } else {
            try {
                Fragment fragment = MeFragment.class.newInstance();
                redirectToFragment(fragment);
                setTitle(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void manageNavigation() {
        Intent intent = new Intent(getApplicationContext(), UserActivity.class);
        startActivityForResult(intent, 0);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.nav_search) {
            searchNavigation(item);
            // call Search fast activity
            /*
            try {
                fragment = FastSearchFragment.class.newInstance();
                redirectToFragment(fragment);
                setTitle(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }*/
        } else if (id == R.id.nav_history) {
            // call history activity
            historyNavigation();
            /*
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            intent.putExtra("userID", "");
            startActivityForResult(intent, 0);*/
        } else if (id == R.id.nav_me) {
            // call login activity if didn't login util now
            meNavigation(item);
            /*
            if (!LocalStorage.getLocalStorageInstance(getApplicationContext()).isLoggedIn()) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(intent, REQUEST_LOGIN);
            } else {
                try {
                    fragment = MeFragment.class.newInstance();
                    redirectToFragment(fragment);
                    setTitle(item);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }*/
        } else if (id == R.id.nav_manage) {
            // call history activity
            manageNavigation();
            /*
            Intent intent = new Intent(getApplicationContext(), UserActivity.class);
            startActivityForResult(intent, 0);*/
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void redirectToFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void setTitle(MenuItem item) {
        item.setChecked(true);
        // Set action bar title
        setTitle(item.getTitle());

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = null;
        switch (requestCode) {
            case REQUEST_LOGIN:
                if (resultCode == RESULT_OK) {
                    meNavigation(meMenu);
                }
                break;
            case REQUEST_CHAT:
                switch (resultCode) {
                    case 0:
                        searchNavigation(searchMenu);
                        break;
                    case 1:
                        historyNavigation();
                        break;
                    case 2:
                        meNavigation(meMenu);
                        break;
                    case 4:
                        manageNavigation();
                        break;
                }
                break;
            case REQUEST_PROFILE:
                switch (resultCode) {
                    case 0:
                        searchNavigation(searchMenu);
                        break;
                    case 1:
                        historyNavigation();
                        break;
                    case 2:
                        meNavigation(meMenu);
                        break;
                    case 4:
                        manageNavigation();
                        break;
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("LOCATION", "StartService");
        if(!isMyServiceRunning(LocationService.class))
            startService(new Intent(this, LocationService.class)); // start tracking service

        // off-topic -> ignore this
        if (!(EventBus.getDefault().isRegistered(this))) {
            EventBus.getDefault().register(this);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, LocationService.class)); // stop tracking service
        Log.d("LOCATION", "StopService");
        // off-topic -> ignore this
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    /**
     * Listen for new database entries from background service
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationTracker.LocationEvent event) {
        Log.d("SUBSRIBE", "This is: " + event.location);
        // Update User Location on Map
        userLocation = new Location(event.location.getLongitude(), event.location.getLatitude());
        // Update User Location on Server
        if ((LocalStorage.
                getSharedPreferences(getApplicationContext()).getString(Constants.TOKEN, "")) != "") {
            sendUserLocation(userLocation);
        }
    }

    private void sendUserLocation(Location userLocation) {
        locationUser.setLocation(userLocation);
    }

    public Location getUserLocation() {
        return userLocation;
    }

    private Runnable sendLocation = new Runnable() {
        @Override
        public void run() {
            String token = LocalStorage.getSharedPreferences(getApplicationContext()).getString(Constants.TOKEN, "");
            if(token == "")
                return;

            UserService service = ServiceFactory
                    .createRetrofitService(UserService.class, URL, token);

            service.update(locationUser)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(user -> {
                                locationUser = user;
                                Log.d("SENDET", "This was send to server: " + locationUser.getLocation().getLatitude() + " + " + locationUser.getLocation().getLongitude());
                            },
                            throwable -> {
                                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
            handler.postDelayed(this, PARAM_INTERVAL);
        }

    };

}
