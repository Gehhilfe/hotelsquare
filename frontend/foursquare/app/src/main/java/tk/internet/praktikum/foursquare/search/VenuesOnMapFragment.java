package tk.internet.praktikum.foursquare.search;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.MainActivity;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.VenueInDetailsNestedScrollView;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Location;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.service.ProfileService;
import tk.internet.praktikum.foursquare.location.LocationTracker;
import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.user.MeFragment;
import tk.internet.praktikum.foursquare.user.ProfileActivity;
import tk.internet.praktikum.foursquare.user.UserActivity;

//import com.google.maps.android.clustering.ClusterManager;

/**
 * Fragment for the Venues on the Map
 */
public class VenuesOnMapFragment extends Fragment implements OnMapReadyCallback {
    private final String LOG = VenuesOnMapFragment.class.getSimpleName();
    private View view;
    private GoogleMap map;
    private RecyclerView recyclerView;
    private String URL = "https://dev.ip.stimi.ovh/";
    private Venue tmp;
    private Bitmap bmap;
    private Location userLocation;
    private User user = new User();
    private Bitmap userImage;


    private static ProfileService profileService;
    private static int overAllID = 0;
    private int thisID = 0;

    private Marker myPosition;

    private List<User> friends = new ArrayList<User>();
    private Map<Marker, Venue> markerVenueMap;
    private Map<Marker, User> markerFriendMap;
    private Map<Venue, Bitmap> venueBitmapMap;
    private Map<User, Bitmap> friendBitmapMap;
    private List<Venue> allVenues;
    private MainActivity mainActivity;
    private Fragment parent;
    int i = 0;
    // private ClusterManager<Location> locationClusterManager;
    public VenuesOnMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_venues_on_map, container, false);


        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.venues_mapView));
        mapFragment.getMapAsync(this);

        // init HashMaps
        markerVenueMap = new HashMap<Marker, Venue>();
        markerFriendMap = new HashMap<Marker, User>();
        venueBitmapMap = new HashMap<Venue, Bitmap>();
        friendBitmapMap = new HashMap<User, Bitmap>();

        mainActivity = (MainActivity) getActivity();
        userLocation = new Location(0, 0);
        userLocation = mainActivity.getUserLocation();

        this.setRetainInstance(true);

        //work-around
        overAllID++;
        thisID = overAllID;

        parent = this;
        return view;
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    public void onDestroy() {
        // unregister to EventBus
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // set Map
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        Log.d("MAPFIX", "OMR: The Map is ready and created");

        // set Usermarker
        //Log.d("MAPFIX", "OMR: User get set now");
        //setUser();
        //Log.d("MAPFIX", "OMR: User was set");

        // custom InfoWindow for all Markers
        class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

            private final View myContentsView;

            MyInfoWindowAdapter(Context context) {
                LayoutInflater inflater = LayoutInflater.from(context);
                myContentsView = inflater.inflate(R.layout.info_window, null);
            }

            @Override
            public View getInfoContents(Marker marker) {

                // Handle Venue Marker
                if (markerVenueMap.containsKey(marker)) {
                    Log.d("KEYFOUND", "Marker is VENUE");
                    Venue venue = markerVenueMap.get(marker);

                    // Set InfoWindow Text
                    TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
                    tvTitle.setText(venue.getName());

                    TextView tvRate = ((TextView) myContentsView.findViewById(R.id.rate));
                    tvRate.setText(Float.toString(venue.getRating()));

                    TextView tvOpen = ((TextView) myContentsView.findViewById(R.id.isopen));
                    if (venue.isOpen()) {
                        tvOpen.setText(getString(R.string.open_now));
                    } else {
                        tvOpen.setText("");
                    }

                    CircleImageView venueImage = ((CircleImageView) myContentsView.findViewById(R.id.img));
                    // load Image if possible, else default
                    if (venueBitmapMap.containsKey(venue)) {
                        venueImage.setImageBitmap(venueBitmapMap.get(venue));
                    } else {
                        venueImage.setImageResource(R.mipmap.ic_location_city_black_24dp);
                    }

                    // Handle Friend Marker
                } else if (markerFriendMap.containsKey(marker)) {
                    Log.d("KEYFOUND", "Marker is FRIEND");
                    User friend = markerFriendMap.get(marker);

                    // Set InfoWindow Text
                    TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
                    tvTitle.setText(friend.getDisplayName());

                    TextView tvRate = ((TextView) myContentsView.findViewById(R.id.rate));
                    if (friend.getCity() != null) {
                        tvRate.setText(friend.getCity());
                    } else {
                        tvRate.setText("");
                    }
                    TextView tvOpen = ((TextView) myContentsView.findViewById(R.id.isopen));
                    if (friend.getAge() != 0) {
                        tvOpen.setText(Integer.toString(friend.getAge()));
                    } else {
                        tvOpen.setText("");
                    }

                    // Set Image
                    CircleImageView venueImage = ((CircleImageView) myContentsView.findViewById(R.id.img));
                    if (friendBitmapMap.get(friend) != null) {
                        venueImage.setImageBitmap(friendBitmapMap.get(friend));
                    } else {
                        venueImage.setImageResource(R.mipmap.user_avatar);
                    }

                    // Handle logged-in User Marker
                } else if (!(LocalStorage.getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, "")).equals("") && !markerFriendMap.containsKey(marker)) {
                    Log.d("KEYFOUND", "Marker is USER");
                    TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
                    tvTitle.setText(user.getDisplayName());
                    CircleImageView venueImage = ((CircleImageView) myContentsView.findViewById(R.id.img));
                    if (userImage != null) {
                        venueImage.setImageBitmap(userImage);
                    } else {
                        venueImage.setImageResource(R.mipmap.user_avatar);
                    }

                    TextView tvRate = ((TextView) myContentsView.findViewById(R.id.rate));
                    if (user.getCity() != null) {
                        tvRate.setText(user.getCity());
                    } else {
                        tvRate.setText("");
                    }
                    TextView tvOpen = ((TextView) myContentsView.findViewById(R.id.isopen));
                    if (user.getAge() != 0) {
                        tvOpen.setText(Integer.toString(user.getAge()));
                    } else {
                        tvOpen.setText("");
                    }

                    // Handle User Marker
                } else {
                    Log.d("KEYFOUND", "Marker is unlogged User");
                    TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
                    tvTitle.setText(R.string.thatsme);

                    CircleImageView venueImage = ((CircleImageView) myContentsView.findViewById(R.id.img));
                    venueImage.setImageResource(R.mipmap.user_avatar);

                    TextView tvRate = ((TextView) myContentsView.findViewById(R.id.rate));
                    tvRate.setText("");

                    TextView tvOpen = ((TextView) myContentsView.findViewById(R.id.isopen));
                    tvOpen.setText("");
                }
                return myContentsView;
            }

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

        }

        map.setInfoWindowAdapter(new MyInfoWindowAdapter(this.getActivity()));

        // Handles Click on InfoWindow
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                // if Venue, got to Venue-Details
                if (markerVenueMap.containsKey(marker)) {

                    //TODO:
                   /* VenueInDetailFragment venueInDetailFragment = new VenueInDetailFragment();
                    venueInDetailFragment.setParent(parent);
                    venueInDetailFragment.setVenueId(markerVenueMap.get(marker).getId());
                    FragmentTransaction fragmentTransaction = VenuesOnMapFragment.this.getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, venueInDetailFragment);
                    fragmentTransaction.addToBackStack(venueInDetailFragment.getTag());
                    fragmentTransaction.commit();*/
                    Intent intent = new Intent(parent.getActivity(), VenueInDetailsNestedScrollView.class);
                    intent.putExtra("VENUE_ID", markerVenueMap.get(marker).getId());
                    parent.getActivity().startActivity(intent);
                    // if Friend, got to Friend-Details
                } else if (markerFriendMap.containsKey(marker)) {
                    //TODO: "Call FriendFragment"
                    Intent intent = new Intent(parent.getActivity(), ProfileActivity.class);
                    intent.putExtra("userID", markerFriendMap.get(marker).getId());
                    parent.getActivity().startActivity(intent);
                    // if User, go to Me-Fragment
                } else if (!(LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, "")).equals("") && !markerFriendMap.containsKey(marker)) {
                    Intent intent = new Intent(parent.getActivity(), UserActivity.class);
                    startActivity(intent);
                    /*
                    MeFragment meFragment = new MeFragment();
                    FragmentTransaction fragmentTransaction = VenuesOnMapFragment.this.getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, meFragment);
                    fragmentTransaction.addToBackStack(meFragment.getTag());
                    fragmentTransaction.commit();
                    */
                }
                // else do nothing (case: user not  logged-in)
            }
        });
    } // end: onMapReady

    /**
     * Updates the Map with the given Venue marker
     *
     * @param venue, which should appear on the map
     */
    public void updateVenueLocation(Venue venue) {
        LatLng venueLocation = new LatLng(venue.getLocation().getLatitude(), venue.getLocation().getLongitude());

        //setup Marker
        Marker tmp = map.addMarker(new MarkerOptions()
                .position(venueLocation)
                .title(venue.getName()));

        //set specific marker Icon
        // TODO: New Markers
        float rating = venue.getRating();
        if (rating == 0) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_grey_24dp));
        } else if (rating > 0 && rating <= 1) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_red_24dp));
        } else if (rating > 1 && rating <= 2) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_orange_24dp));
        } else if (rating > 2 && rating <= 3) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_yellow_24dp));
        } else if (rating > 3 && rating <= 4) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_lime_24dp));
        } else if (rating > 4) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_green_24dp));
        }

        markerVenueMap.put(tmp, venue);

        // load Images for Venue and InfoWindow
        if (venue.getImages().size() > 0) {
            ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getContext());
            imageCacheLoader.loadBitmap(venue.getImages().get(0), ImageSize.SMALL)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        venueBitmapMap.put(venue, bitmap);
                    });
        }


    }

    /**
     * Update a list of Venues on the Map
     *
     * @param venues
     */
    public void updateVenuesMarker(List<Venue> venues) {
        // clear Map
        //map.clear();
        Log.d("MAPFIX", "UVM: Map is cleared");

        for (Venue venue : venues) {
            updateVenueLocation(venue);
        }
        // set user
        Log.d("MAPFIX", "UVM: User get set now");
        setUser();
        Log.d("MAPFIX", "UVM: User was set");
        // and friends if logged-in
        if (!(LocalStorage.
                getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, "")).equals("")) {
            updateFriendsMarker();
        }
        Location centerLocation=calculateClusteringCenterLocation(venues);
        //Location centerLocation=userLocation;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(centerLocation.getLatitude(), centerLocation.getLongitude()), 12));

    }

    /**
     * Update the marker of a friend on the map
     *
     * @param friend
     */
    public void updateFriendsLocation(User friend) {
        LatLng friendLocation = new LatLng(friend.getLocation().getLatitude(), friend.getLocation().getLongitude());

        // get matching friend
        User tmpFriend = matchedFriend(friend);
        // if one was found
        if (tmpFriend != null) {
            // get belonging marker
            Marker tmpMarker = getFriendMarker(tmpFriend);
            // remove it
            markerFriendMap.remove(tmpMarker);
            tmpMarker.remove();
            friendBitmapMap.remove(tmpFriend);
            Log.d("KEYFOUND", "Removed!");
        }
        Marker tmp = map.addMarker(new MarkerOptions()
                .position(friendLocation)
                .title(friend.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.friend_position))
        );
        // load Images for marker
        if (friend.getAvatar() != null) {
            ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getContext());
            imageCacheLoader.loadBitmap(friend.getAvatar(), ImageSize.SMALL)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        friendBitmapMap.put(friend, bitmap);
                    });
        }
        Log.d("KEYFOUND", "ADDED!");
        markerFriendMap.put(tmp, friend);
        return;
    }

    /**
     * Return matching stored friend by DisplayName
     *
     * @param u, user to be found in Hashmap
     * @return found user or null
     */
    private User matchedFriend(User u) {
        for (Map.Entry<Marker, User> entry : markerFriendMap.entrySet()) {
            if (entry.getValue().getDisplayName().equals(u.getDisplayName())) {
                return entry.getValue();
            }
        }
        return null;
    }

    //TODO:
    private boolean friendLocationChanged(User friend, Marker friendMarker) {
        Location location1 = friend.getLocation();
        Location location2 = markerFriendMap.get(friendMarker).getLocation();
        return (location1.getLatitude().equals(location2.getLatitude())) && (location1.getLongitude().equals(location2.getLongitude()));
    }

    //TODO:
    private Marker getFriendMarker(User user) {
        for (Map.Entry<Marker, User> entry : markerFriendMap.entrySet()) {
            if (entry.getValue() == user) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Updates the friends marker on the map
     */
    public void updateFriendsMarker() {

        //check again if logged-in
        if (!(LocalStorage.
                getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, "")).equals("")) {

            if (profileService == null) {

                profileService = ServiceFactory
                        .createRetrofitService(ProfileService.class, URL, LocalStorage.
                                getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));
                Log.d("KGK", "New PS created " + profileService);
            }
            Log.d("KGK", "PS search for friends " + profileService);
            profileService.getNearByFriends(userLocation)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(nearbyFriends -> {
                                friends = nearbyFriends;
                                Log.d("KEYFOUND", "Size of Nearby Friends " + friends.size());

                                for (User f : friends) {
                                    // for every user
                                    updateFriendsLocation(f);
                                }
                            },
                            throwable -> {
                                Log.d(LOG, "Exception: " + throwable.getMessage());
                            });
        }
    }

    /**
     * Set the user on the Map
     */
    public void setUser() {

        Log.d("MAPFIX", "SU: User gets his Location");
        // set user position
        userLocation = mainActivity.getUserLocation();

        // get user Data
        try {
            if (!(LocalStorage.
                    getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, "")).equals("")) {

                ProfileService profileService = ServiceFactory
                        .createRetrofitService(ProfileService.class, URL, LocalStorage.
                                getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

                profileService.profile()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                user -> {
                                    this.user = user;
                                    // get user Avatar
                                    if(user.getAvatar() == null)
                                        return;
                                    ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getContext());
                                    imageCacheLoader.loadBitmap(user.getAvatar(), ImageSize.SMALL)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(bitmap -> {
                                                userImage = bitmap;
                                            });
                                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("MAPFIX", "SU: Star checks");

        //If position has not changed, no new Marker
        if ((myPosition != null) && (myPosition.getPosition().latitude == userLocation.getLatitude()) && (myPosition.getPosition().longitude == userLocation.getLongitude())) {
            Log.d("MAPFIX", "SU: No new Marker");

            return;
        }

        // If no user Marker yet
        if (myPosition == null && map != null) {
            Log.d("MAPFIX", "SU: SetPosition with: " + userLocation.getLatitude() + " : " + userLocation.getLongitude());
            LatLng tmp = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            Log.d("MAPFIX", "SU: SetPosition with2: " + tmp.latitude + " : " + tmp.longitude);
            myPosition = map.addMarker(new MarkerOptions()
                    .position(tmp)
                    .visible(true)
                    .title("That's you!").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_position)));
            Log.d("MAPFIX", "SU: New Marker for User");
            return;
        }

        //if position changed and marker already set
        if ((myPosition != null) && (myPosition.getPosition().longitude != userLocation.getLongitude()) && (map != null)) {
            //myPosition.remove();
            Log.d("MAPFIX", "SU: SetPosition with: " + userLocation.getLatitude() + " : " + userLocation.getLongitude());
            LatLng tmp = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            Log.d("MAPFIX", "SU: SetPosition with2: " + tmp.latitude + " : " + tmp.longitude);
            myPosition.setPosition(tmp);
            Log.d("MAPFIX", "SU: Remove and add!");
            /*myPosition = map.addMarker(new MarkerOptions()
                    .position(new LatLng(49.877153, 8.654542))
                    .title("That's you!")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_position))); */
            return;
        }
    }

    // TODO: Delete?
 /*   */

    /**
     * update new venues list
     *
     * @param venues
     *//*
    protected void updateRecyclerView(List<Venue> venues) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new SearchResultAdapter(this, venues));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.HORIZONTAL));

    }*/
    protected Location calculateClusteringCenterLocation(List<Venue> venues) {
        return new Location(venues.stream().mapToDouble(location -> location.getLocation().getLongitude()).average().getAsDouble(),
                venues.stream().mapToDouble(location -> location.getLocation().getLatitude()).average().getAsDouble());

    }

    /**
     * Listen for new database entries from background service
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationTracker.LocationEvent event) {
        if (thisID < overAllID) {
            EventBus.getDefault().unregister(this);
        }
        // Update your own Position
        Log.d("MAPFIX", "OME: onEvent set Marker");
        setUser();
        // Update your Friends' Positions
        if ((LocalStorage.
                getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, "")) != "") {
            updateFriendsMarker();
            Log.d("KGK", "THIS IS FRAGMENT: " + this.thisID + "  " + overAllID);
            Log.d("MAPFIX", "Location: " + event.location);
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}

