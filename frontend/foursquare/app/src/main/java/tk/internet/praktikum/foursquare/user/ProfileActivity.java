package tk.internet.praktikum.foursquare.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Gender;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.ProfileService;
import tk.internet.praktikum.foursquare.api.service.UserService;
import tk.internet.praktikum.foursquare.chat.DummyChatActivity;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();
    private final String URL = "https://dev.ip.stimi.ovh/";
    private User currentUser = new User();
    private User otherUser = new User();
    private TextView name, email, city, age;
    private RadioButton male, female, none;
    private ImageView avatarPicture;
    private FloatingActionButton fab;
    private Bitmap avatar;
    private String userID;

    public ProfileActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userID = getIntent().getStringExtra("userID");
        userID = "599071a509ad180015af8b27"; // janus nicht auf flist von peter
        //userID = "599071a509ad180015af8b25"; // Admin auf FList von peter

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        name = (TextView) findViewById(R.id.profile_name);
        email = (TextView) findViewById(R.id.profile_email);
        city = (TextView) findViewById(R.id.profile_city);
        age = (TextView) findViewById(R.id.profile_age);

        male = (RadioButton) findViewById(R.id.radioButton);
        female = (RadioButton) findViewById(R.id.radioButton2);
        none = (RadioButton) findViewById(R.id.radioButton3);

        avatarPicture = (ImageView) findViewById(R.id.profile_activity_avatar);
        fab = (FloatingActionButton) findViewById(R.id.profile_activity_fab);

        initialiseFab();
        initialiseProfile();
    }

    private void initialiseFab() {
        ProfileService service = ServiceFactory
                .createRetrofitService(ProfileService.class, URL, LocalStorage.
                        getSharedPreferences(getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.friends(0)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            friendListResponse -> {
                                List<User> friendList = friendListResponse.getFriends();

                                for (User user : friendList)
                                    if (user.getId().equals(userID)) {
                                        fab.setImageResource(R.mipmap.ic_chat_black_48dp);
                                        fab.setOnClickListener(v -> startChat());
                                    } else {
                                        fab.setOnClickListener(v -> addFriend());
                                    }
                            },
                            throwable -> {
                                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initialiseProfile() {
        UserService service = ServiceFactory
                .createRetrofitService(UserService.class, URL, LocalStorage.
                        getSharedPreferences(getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.detailsByName("janus")
           // service.profileByID(userID)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            user -> {
                                otherUser = user;
                                name.setText(otherUser.getDisplayName());
                                email.setText(otherUser.getEmail());
                                city.setText(otherUser.getCity());
                                age.setText(Integer.toString(otherUser.getAge()));
                                Gender gender = otherUser.getGender();
                                if (gender == Gender.MALE)
                                    male.setChecked(true);
                                else if (gender == Gender.FEMALE)
                                    female.setChecked(true);
                                else
                                    none.setChecked(true);

                                if (otherUser.getAvatar() != null) {
                                    ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getApplicationContext());
                                    imageCacheLoader.loadBitmap(otherUser.getAvatar(), ImageSize.LARGE)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(bitmap -> {
                                                        avatarPicture.setImageBitmap(bitmap);
                                                    },
                                                    throwable -> {
                                                        Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                            );
                                }
                            },
                            throwable -> {
                                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addFriend() {
        UserService service = ServiceFactory
                .createRetrofitService(UserService.class, URL, LocalStorage.
                        getSharedPreferences(getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.sendFriendRequest(otherUser.getName())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            user -> {},
                            throwable -> {
                                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startChat() {
        // TODO - GET CHAT ID, falls es keien gibt neuen chat erstellen => wechseln
        String chatID = "599071a609ad180015af8b2c";
        Intent intent = new Intent(getApplicationContext(), DummyChatActivity.class);
        intent.putExtra("chatId", chatID);
        intent.putExtra("currentUserName", "wahrscinenlich egal");
        startActivityForResult(intent, 0);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        // TODO - Entwerder finish und weitere navigation im onactivityresult (mittels result codes)
        // TODO - oder finish und parameter (item) mitgeben. new itent => set data => item id, item name
        if (id == R.id.nav_search) {
            return true;
        } else if (id == R.id.nav_history) {
            return true;
        } else if (id == R.id.nav_me) {
            return true;
        }else if (id == R.id.nav_manage) {
            return true;
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
}
