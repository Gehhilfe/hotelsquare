package tk.internet.praktikum.foursquare.search;

import android.content.Context;
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

    private Marker myPosition;

    private List<User> friends = new ArrayList<User>();
    private Map<Marker, Venue> markerVenueMap;
    private Map<Marker, User> markerFriendMap;
    private Map<Venue, Bitmap> venueBitmapMap;
    private Map<User, Bitmap> friendBitmapMap;
    private List<Venue> allVenues;
    private MainActivity mainActivity;
    private Fragment parent;

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
        userLocation = new Location(0,0);
        userLocation = mainActivity.getUserLocation();

        this.setRetainInstance(true);

        // register to EventBus
        if(!(EventBus.getDefault().isRegistered(this))){
            EventBus.getDefault().register(this);
        }

        parent=this;
        return view;
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

        // set Usermarker
        setUser();

        //TODO:
        //updateFriendsMarker();

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

                    Venue venue = markerVenueMap.get(marker);

                    // Set InfoWindow Text
                    TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
                    tvTitle.setText(venue.getName());

                    TextView tvRate = ((TextView) myContentsView.findViewById(R.id.rate));
                    tvRate.setText(Float.toString(venue.getRating()));

                    TextView tvOpen = ((TextView) myContentsView.findViewById(R.id.isopen));
                    if (venue.isOpen()) {
                        tvOpen.setText(getString(R.string.open_now));
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

                    User friend = markerFriendMap.get(marker);

                    // Set InfoWindow Text
                    TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
                    tvTitle.setText(friend.getDisplayName());
                    // Set Image
                    CircleImageView venueImage = ((CircleImageView) myContentsView.findViewById(R.id.img));
                    venueImage.setImageBitmap(friendBitmapMap.get(friend));

                // Handle User Marker
                } else {
                    // check if logged-in user
                    if((LocalStorage.getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""))  != ""){
                        //TODO: load and set and stuff
                    }
                    //TODO: dunno what to do here, maybe default stuff?
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
                    VenueInDetailFragment venueInDetailFragment = new VenueInDetailFragment();
                    venueInDetailFragment.setParent(parent);
                    venueInDetailFragment.setVenueId(markerVenueMap.get(marker).getId());
                    FragmentTransaction fragmentTransaction = VenuesOnMapFragment.this.getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, venueInDetailFragment);
                    fragmentTransaction.addToBackStack(venueInDetailFragment.getTag());
                    fragmentTransaction.commit();
                // if Friend, got to Friend-Details
                } else if (markerFriendMap.containsKey(marker)) {
                    //TODO: "Call FriendFragment"
                // if User, go to Me-Fragment
                } else if ((LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""))  != "") {
                    MeFragment meFragment = new MeFragment();
                    FragmentTransaction fragmentTransaction = VenuesOnMapFragment.this.getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, meFragment);
                    fragmentTransaction.addToBackStack(meFragment.getTag());
                    fragmentTransaction.commit();
                }
                // else do nothing
                //TODO: check if true
            }
        });
    }

    /**
     * Updates the Map with the given Venue marker
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
     * @param venues
     */
    public void updateVenuesMarker(List<Venue> venues) {
        // clear Map
        map.clear();

        for (Venue venue : venues) {
            updateVenueLocation(venue);
        }
        // set user and Friends
        setUser();
        updateFriendsMarker();

        //TODO: Shouldn't we move the Camera to the User's Position?
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(venues.get(0).getLocation().getLatitude(), venues.get(0).getLocation().getLongitude()), 14));

    }

    /**
     * Update the marker of a friend on the map
     * @param friend
     */
    public void updateFriendsLocation(User friend) {
        LatLng friendLocation = new LatLng(friend.getLocation().getLatitude(), friend.getLocation().getLongitude());

        //TODO: Fix!
        // get Marker if possible
        Marker friendMarker = null;
        if(markerFriendMap.containsValue(friend)){
            friendMarker = getFriendMarker(friend);
        }
        //if not in Map Already add or location of friend changed
        if(!markerFriendMap.containsValue(friend) || friendLocationChanged(friend, friendMarker)){

            //remove if location Changed
            if(friendLocationChanged(friend, friendMarker)){
                markerFriendMap.remove(friendMarker);
                friendBitmapMap.remove(friend);
            }

            //add
            Marker tmp = map.addMarker(new MarkerOptions()
                    .position(friendLocation)
                    .title(friend.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.friend_position))
            );
            markerFriendMap.put(tmp, friend);

            // load Images for marker
            Log.d("KEYFOUND", "Is not null: " + friendBitmapMap.size() + " " + friend + " " + friendBitmapMap.containsKey(friend));
            if (friend.getAvatar() != null) {
                ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getContext());
                imageCacheLoader.loadBitmap(friend.getAvatar(), ImageSize.SMALL)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bitmap -> {
                            friendBitmapMap.put(friend, bitmap);
                        });
            }
        }

    }

    //TODO:
    private boolean friendLocationChanged(User friend, Marker friendMarker){
        return friend.getLocation() != markerFriendMap.get(friendMarker).getLocation();
    }

    //TODO:
    private Marker getFriendMarker(User user) {
        for(Map.Entry<Marker, User> entry : markerFriendMap.entrySet()){
            if(entry.getValue() == user){
                return entry.getKey();
            }
        }
        return null;
    }

    //TODO:
    public void updateFriendsMarker() {

        for(Map.Entry<Marker,User> entry : markerFriendMap.entrySet()){
            // Remove from GoogleMap
            entry.getKey().remove();
        }
        markerFriendMap = new HashMap<Marker, User>();
        ProfileService profileService = ServiceFactory
                .createRetrofitService(ProfileService.class, URL, LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

        profileService.getNearByFriends(userLocation)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(nearbyFriends -> {
                            friends = nearbyFriends;
                            Log.d("KEYFOUND", "Size of Nearby Friends " + friends.size());

                            for (User f : friends) {
                                updateFriendsLocation(f);
                                Log.d("KEYFOUND", "SetMarker: " + f);
                            }

                        },
                        throwable -> {
                            Log.d(LOG,"Exception: "+throwable.getMessage());
                        });

    }

    /**
     * Set the user on the Map
     */
    public void setUser() {
        if(myPosition != null){
            myPosition.remove();
        }
        mainActivity = (MainActivity) getActivity();
        userLocation = new Location(0,0);
        userLocation = mainActivity.getUserLocation();
        //TODO: delete
        Log.d("KEYFOUND", "UserLocation is: " + userLocation.getLatitude() + " , " + userLocation.getLongitude());
        myPosition = map.addMarker(new MarkerOptions()
                .position(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()))
                .title("That's you!").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_position)));

    }

        // TODO: Delete?
 /*   *//**
     * update new venues list
     *
     * @param venues
     *//*
    protected void updateRecyclerView(List<Venue> venues) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new SearchResultAdapter(this, venues));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.HORIZONTAL));

    }*/


    protected  Location calculateClusteringCenterLocation(List<Venue> venues){
        //Todo
        return null;
    }

    /**
     * Listen for new database entries from background service
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationTracker.LocationEvent event) {
        // Update your own Position
        setUser();
        // Update your Friends' Positions
        if ((LocalStorage.
                getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, "")) != "") {
            updateFriendsMarker();
        }
    }
}
