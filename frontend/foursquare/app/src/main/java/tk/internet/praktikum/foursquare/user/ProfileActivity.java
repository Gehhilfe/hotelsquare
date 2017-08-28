package tk.internet.praktikum.foursquare.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.MainActivity;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.VenueInDetailsNestedScrollView;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Gender;
import tk.internet.praktikum.foursquare.api.bean.Image;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.VenueCheckinInformation;
import tk.internet.praktikum.foursquare.api.service.ChatService;
import tk.internet.praktikum.foursquare.api.service.ProfileService;
import tk.internet.praktikum.foursquare.api.service.UserService;
import tk.internet.praktikum.foursquare.api.service.VenueService;
import tk.internet.praktikum.foursquare.chat.ChatActivity;
import tk.internet.praktikum.foursquare.search.SearchPersonActivity;
import tk.internet.praktikum.foursquare.search.Utils;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

public class ProfileActivity extends AppCompatActivity {

    private final String URL = "https://dev.ip.stimi.ovh/";
    private User otherUser = new User();
    private TextView name, city, age, venueName, venueShortName, venueCount;
    private RadioButton male, female, none;
    private ImageView avatarPicture, venueLogo;
    private FloatingActionButton fab;
    private String userID;
    private RecyclerView recyclerView;
    private LinearLayout profileTopContent;
    private VenueCheckinInformation topVenue;
    private Toolbar toolbar;
    private ProfileLatestRecyclerViewAdapter profileLatestRecyclerViewAdapter;

    public ProfileActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userID = getIntent().getStringExtra("userID");

        toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = (TextView) findViewById(R.id.profile_name);
        city = (TextView) findViewById(R.id.profile_city);
        age = (TextView) findViewById(R.id.profile_age);

        venueName = (TextView) findViewById(R.id.profile_top_venue_name);
        venueShortName = (TextView) findViewById(R.id.profile_top_venue_short_name);
        venueCount = (TextView) findViewById(R.id.profile_top_venue_count);

        male = (RadioButton) findViewById(R.id.radio_male);
        female = (RadioButton) findViewById(R.id.radio_female);
        none = (RadioButton) findViewById(R.id.radio_anonymous);

        avatarPicture = (ImageView) findViewById(R.id.profile_activity_avatar);
        venueLogo = (ImageView) findViewById(R.id.profile_top_venue_logo);
        profileTopContent = (LinearLayout) findViewById(R.id.profile_top_content_container);
        fab = (FloatingActionButton) findViewById(R.id.profile_activity_fab);

        recyclerView = (RecyclerView) findViewById(R.id.profile_last_checkin_recylcer_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        profileLatestRecyclerViewAdapter = new ProfileLatestRecyclerViewAdapter(getApplicationContext(), this);
        recyclerView.setAdapter(profileLatestRecyclerViewAdapter);

        profileTopContent.setOnClickListener(v -> loadVenue());
        initialiseFab();
        initialiseProfile();

    }

    private void loadVenue() {
        Intent intent = new Intent(this, VenueInDetailsNestedScrollView.class);
        intent.putExtra("VENUE_ID", topVenue.getVenueID());
        startActivity(intent);
    }

    private void initialiseFab() {
        ProfileService service = ServiceFactory
                .createRetrofitService(ProfileService.class, URL, LocalStorage.
                        getSharedPreferences(getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.profileIfFriends(userID)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            friendListResponse -> {
                                List<User> friendList = friendListResponse.getFriends();
                                boolean isFriend = false;

                                for (User user : friendList)
                                    if (user.getId().equals(userID)) {
                                        fab.setImageResource(R.mipmap.ic_chat_black_48dp);
                                        isFriend = true;
                                    }

                                    if  (isFriend) {
                                        fab.setOnClickListener(v -> startChat());
                                    }else {
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
            service.profileByID(userID)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            user -> {
                                String tmpName = user.getDisplayName().substring(0, 1).toUpperCase() + user.getDisplayName().substring(1);
                                toolbar.setTitle(tmpName);
                                otherUser = user;
                                name.setText(tmpName);
                                city.setText(otherUser.getCity());
                                age.setText(String.format(Locale.ENGLISH, "%1$d", otherUser.getAge()));
                                Gender gender = otherUser.getGender();
                                if (gender == Gender.MALE)
                                    male.setChecked(true);
                                else if (gender == Gender.FEMALE)
                                    female.setChecked(true);
                                else
                                    none.setChecked(true);

                                //recyclerView.setAdapter(new ProfileLatestRecyclerViewAdapter(getApplicationContext(), user.getLastCheckins(), this));
                                profileLatestRecyclerViewAdapter.setResults(user.getLastCheckins());
                                profileLatestRecyclerViewAdapter.notifyDataSetChanged();

                                if (user.getTopCheckins().size() > 0) {
                                    topVenue = user.getTopCheckins().get(0);
                                    initialiseTopVenue(topVenue.getVenueID());
                                    venueCount.setText("# " + String.valueOf(topVenue.getCount()));
                                } else {
                                    venueLogo.setVisibility(View.GONE);
                                    venueShortName.setVisibility(View.GONE);
                                    venueName.setVisibility(View.GONE);
                                    venueCount.setVisibility(View.GONE);
                                }
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
                                } else {
                                    avatarPicture.setImageResource(R.mipmap.user_avatar);
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

    public void initialiseTopVenue(String venueId){
        VenueService service = ServiceFactory
                .createRetrofitService(VenueService.class, URL, LocalStorage.
                        getSharedPreferences(getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.getDetails(venueId)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            venue -> {
                                venueName.setText(venue.getName());

                                List<Image> images=venue.getImages();
                                if(images.size()>0) {
                                    Image image = images.get(0);
                                    ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getApplicationContext());
                                    imageCacheLoader.loadBitmap(image, ImageSize.SMALL)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(bitmap -> {
                                                venueLogo.setImageBitmap(bitmap);
                                                venueLogo.setVisibility(View.VISIBLE);
                                                venueShortName.setVisibility(View.GONE);
                                            });
                                }
                                else {
                                    venueLogo.setDrawingCacheEnabled(true);
                                    Bitmap bitmap= null;
                                    try {
                                        bitmap = Utils.decodeResourceImage(getApplicationContext(),"default_image",50,50);
                                        venueLogo.setImageBitmap(bitmap);
                                        venueShortName.setText(venue.getName().substring(0,1).toUpperCase());
                                        venueShortName.setVisibility(View.VISIBLE);
                                        venueLogo.setVisibility(View.VISIBLE);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
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
                            user -> {
                                Toast.makeText(getApplicationContext(), "Passt...", Toast.LENGTH_SHORT).show();
                            },
                            throwable -> {
                                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startChat() {
        ChatService service = ServiceFactory
                .createRetrofitService(ChatService.class, URL, LocalStorage.
                        getSharedPreferences(getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.getOrStartChat(userID)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            chatResponse -> {
                                chatResponse.getChatId();
                                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                                intent.putExtra("chatId", chatResponse.getChatId());
                                intent.putExtra("Parent", "ProfileActivity");
                                startActivity(intent);
                            },
                            throwable -> {
                                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
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

        if (parentActivity.equals("UserActivity")) {
            i = new Intent(this, UserActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (parentActivity.equals("SearchPerson")) {
            i = new Intent(this, SearchPersonActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        else if(parentActivity.equals("VenueInDetailsNestedScrollView")){
            i = new Intent(this, VenueInDetailsNestedScrollView.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        else {
                i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        return i;
    }

}
