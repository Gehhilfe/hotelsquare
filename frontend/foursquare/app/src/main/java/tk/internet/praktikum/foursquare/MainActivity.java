package tk.internet.praktikum.foursquare;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Location;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.ProfileService;
import tk.internet.praktikum.foursquare.api.service.UserService;
import tk.internet.praktikum.foursquare.history.DaoSession;
import tk.internet.praktikum.foursquare.history.HistoryFragment;
import tk.internet.praktikum.foursquare.location.LocationService;
import tk.internet.praktikum.foursquare.location.LocationTracker;
import tk.internet.praktikum.foursquare.login.LoginActivity;
import tk.internet.praktikum.foursquare.search.FastSearchFragment;
import tk.internet.praktikum.foursquare.search.PersonSearchFragment;
import tk.internet.praktikum.foursquare.search.SuggestionKeyWord;
import tk.internet.praktikum.foursquare.storage.LocalDataBaseManager;
import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.user.SettingsFragment;
import tk.internet.praktikum.foursquare.user.UserActivity;
import tk.internet.praktikum.foursquare.utils.LanguageHelper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final int REQUEST_LOGIN = 0;
    private final int REQUEST_ME = 6;
    private final int RESULT_USER_ACTIVITY = 3;
    private final int RESULT_LOGIN = 2;


    private TextView userName;
    private ImageView avatar;

    private Location userLocation = new Location(0, 0);
    private Handler handler = new Handler();
    private final String URL = "https://dev.ip.stimi.ovh/";
    private User locationUser = new User();
    private int PARAM_INTERVAL = 10000;
    private NavigationView navigationView;
    private MenuItem loginMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getApplicationContext());
        String language = sharedPreferences.getString("LANGUAGE", "de");
        LanguageHelper.updateResources(this, language);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View parentView = navigationView.getHeaderView(0);
        userName = (TextView) parentView.findViewById(R.id.nav_header_name);
        avatar = (ImageView) parentView.findViewById(R.id.nav_header_avatar);


        Menu tmpMenu = navigationView.getMenu();
        loginMenu = null;
        for (int i = 0; i < tmpMenu.size(); i++) {
            tmpMenu.getItem(i);
            if (tmpMenu.getItem(i).getItemId() == R.id.nav_login_logout) {
                loginMenu = tmpMenu.getItem(i);
            }
        }
        readStaticKeyWords();
        if (LocalStorage.getLocalStorageInstance(getApplicationContext()).isLoggedIn()) {
            initialiseNavigationHeader();
            loginMenu.setTitle(getApplicationContext().getResources().getString(R.string.action_logout));
        }

        FastSearchFragment searchFragment = new FastSearchFragment();
        redirectToFragment(searchFragment, getApplicationContext().getResources().getString(R.string.action_search));

        handler.postDelayed(sendLocation, PARAM_INTERVAL);
    }

    private void initialiseNavigationHeader() {
        ProfileService service = ServiceFactory
                .createRetrofitService(ProfileService.class, URL, LocalStorage.
                        getSharedPreferences(getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.profile()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            user -> {
                                userName.setText(user.getName());
                                if (user.getAvatar() != null) {
                                    ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getApplicationContext());
                                    imageCacheLoader.loadBitmap(user.getAvatar(), ImageSize.SMALL)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(bitmap -> {
                                                        avatar.setImageBitmap(bitmap);
                                                    },
                                                    throwable -> {
                                                        Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                            );
                                } else {
                                    avatar.setImageResource(R.mipmap.user_avatar);
                                }
                            },
                            throwable -> {
                                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            int lastBackStackEntryPosition = getSupportFragmentManager().getBackStackEntryCount() - 2;
            if (lastBackStackEntryPosition >= 0) {
                FragmentManager.BackStackEntry lastBackStackEntry =
                        getSupportFragmentManager().getBackStackEntryAt(lastBackStackEntryPosition);
                setTitle(lastBackStackEntry.getName());
                getSupportFragmentManager().popBackStack();
            }
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
            redirectToFragment(fragment, getApplicationContext().getResources().getString(R.string.action_search));
            setTitle(item);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

  /*  private void searchPersonNavigation() {
        Intent intent = new Intent(getApplicationContext(), SearchPersonActivity.class);
        startActivityForResult(intent, REQUEST_SEARCH_PERSON);
    }*/

    private void searchPersonNavigation(MenuItem item) {
        PersonSearchFragment fragment = new PersonSearchFragment();
        redirectToFragment(fragment, getApplicationContext().getResources().getString(R.string.action_search_person));
        setTitle(item);
    }
/*
    private void historyNavigation() {
        Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
        startActivityForResult(intent, REQUEST_HISTORY);
    }*/

    private void historyNavigation(MenuItem item) {
        HistoryFragment fragment = new HistoryFragment();
        redirectToFragment(fragment, getApplicationContext().getResources().getString(R.string.action_history));
        setTitle(item);
    }

    private void meNavigation() {
        if (!LocalStorage.getLocalStorageInstance(getApplicationContext()).isLoggedIn()) {
            login(true);
        } else {
            Intent intent = new Intent(getApplicationContext(), UserActivity.class);
            startActivityForResult(intent, REQUEST_ME);
        }
    }


    private void settingsNavigation(MenuItem item) {
        SettingsFragment fragment = new SettingsFragment();
        redirectToFragment(fragment, getApplicationContext().getResources().getString(R.string.action_settings));
        setTitle(item);
    }

    private void login(boolean destination) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.putExtra("UserActivity", destination);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    private void logout() {
        LocalStorage.getLocalStorageInstance(getApplicationContext()).deleteLoggedInInformation();
        this.recreate();
        /*
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(intent, 0);
        */
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_search:
                searchNavigation(item);
                break;
            case R.id.nav_search_person:
                //searchPersonNavigation();
                searchPersonNavigation(item);
                break;
            case R.id.nav_history:
                //historyNavigation();
                historyNavigation(item);
                break;
            case R.id.nav_me:
                meNavigation();
                break;
            case R.id.nav_manage:
                //settingsNavigation();
                settingsNavigation(item);
                break;
            case R.id.nav_login_logout:
                if (!LocalStorage.getLocalStorageInstance(getApplicationContext()).isLoggedIn()) {
                    login(false);
                    return false;
                } else {
                    logout();
                    //item.setTitle(getApplicationContext().getResources().getString(R.string.action_login));
                }
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void redirectToFragment(Fragment fragment, String backStackName) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(backStackName);
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
        switch (requestCode) {
            case REQUEST_LOGIN:
                if (resultCode == RESULT_LOGIN) {
                    initialiseNavigationHeader();
                    loginMenu.setTitle(getApplicationContext().getResources().getString(R.string.action_logout));
                    break;
                } else if (resultCode == RESULT_USER_ACTIVITY) {
                    initialiseNavigationHeader();
                    loginMenu.setTitle(getApplicationContext().getResources().getString(R.string.action_logout));
                    meNavigation();
                }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("LOCATION", "StartService");
        if (!isMyServiceRunning(LocationService.class))
            startService(new Intent(this, LocationService.class)); // start tracking service

        // off-topic -> ignore this
        if (!(EventBus.getDefault().isRegistered(this))) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LocalStorage.getLocalStorageInstance(getApplicationContext()).isLoggedIn())
            initialiseNavigationHeader();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
            if (token == "")
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

    public void setTitleOnBackStack() {
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            }
        });
    }

    public synchronized void readStaticKeyWords() {
         LocalStorage ls = LocalStorage.getLocalStorageInstance(this);
        if (!ls.alreadyReadStaticKeyWords()) {
            System.out.println(("#### readStaticKeyWords"));
            DaoSession daoSession = LocalDataBaseManager.getLocalDatabaseManager(getApplicationContext()).getDaoSession();
            LocalStorage.getLocalStorageInstance(getApplicationContext()).saveBooleanValue(Constants.ALREADY_READ_STATIC_KEYWORDS, true);
            String[] suggestionList = getApplicationContext().getResources().getStringArray(R.array.suggestion_list);
            for (int i = 0; i < suggestionList.length; i++) {
                SuggestionKeyWord suggestionKeyWord = new SuggestionKeyWord();
                suggestionKeyWord.setUid(UUID.randomUUID().toString());
                suggestionKeyWord.setSuggestionName(suggestionList[i]);
                daoSession.getSuggestionKeyWordDao().insert(suggestionKeyWord);

            }
        }
    }

}
