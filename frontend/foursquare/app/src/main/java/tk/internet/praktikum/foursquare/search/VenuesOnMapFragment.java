package tk.internet.praktikum.foursquare.search;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import tk.internet.praktikum.foursquare.user.ProfileActivity;
import tk.internet.praktikum.foursquare.user.UserActivity;

/**
 * Fragment for the Venues on the Map
 */
public class VenuesOnMapFragment extends Fragment implements OnMapReadyCallback {
    private final String LOG = VenuesOnMapFragment.class.getSimpleName();
    private View view;
    private GoogleMap map;
    private RecyclerView recyclerView;
    private String URL = "https://dev.ip.stimi.ovh/";
    private Location userLocation;
    private User user = new User();
    private Bitmap userImage;


    private Button findmeButton;
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
    final int MAX_ZOOM_LEVEL = 14;
    final int MIN_ZOOM_LEVEL = 9;
    final int MAX_RADIUS = 8;
    final int MIN_RADIUS = 5;

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

        findmeButton = (Button) view.findViewById(R.id.focus_on_userlocation);
        findmeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 16));
            }
        });
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
                    Intent intent = new Intent(parent.getActivity(), VenueInDetailsNestedScrollView.class);
                    intent.putExtra("VENUE_ID", markerVenueMap.get(marker).getId());
                    parent.getActivity().startActivity(intent);
                    // if Friend, got to Friend-Details
                } else if (markerFriendMap.containsKey(marker)) {
                    Intent intent = new Intent(parent.getActivity(), ProfileActivity.class);
                    intent.putExtra("userID", markerFriendMap.get(marker).getId());
                    intent.putExtra("Parent", "VenueInDetailsNestedScrollView");
                    parent.getActivity().startActivity(intent);
                    // if User, go to Me-Fragment
                } else if (!(LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, "")).equals("") && !markerFriendMap.containsKey(marker)) {
                    Intent intent = new Intent(parent.getActivity(), UserActivity.class);
                    intent.putExtra("Parent", "VenueInDetailsNestedScrollView");
                    startActivity(intent);
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
        float rating = venue.getRating();
        if (rating == 0) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_grey_marker));
        } else if (rating > 0 && rating <= 1) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_red_marker));
        } else if (rating > 1 && rating <= 2) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_orange_marker));
        } else if (rating > 2 && rating <= 3) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_yellow_marker));
        } else if (rating > 3 && rating <= 4) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_lime_marker));
        } else if (rating > 4) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_green_marker));
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
                    }, throwable ->{
                        Log.d(LOG, "Exception: " + throwable.getMessage());
                    });
        }


    }

    /**
     * Update a list of Venues on the Map
     *
     * @param venues
     */
    public void updateVenuesMarker(List<Venue> venues) {

        for (Venue venue : venues) {
            updateVenueLocation(venue);
        }
        // set user
        setUser();
        // and friends if logged-in
        if (!(LocalStorage.
                getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, "")).equals("")) {
            updateFriendsMarker();
        }
        android.location.Location[] locations = calculateBoundsOfLocations(venues);

        if (venues.size() <= 10) {
            //dynamicZoomLevel(locations, 1);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locations[0].getLatitude(), locations[0].getLongitude()), 13));
        } else if (venues.size() > 100) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locations[0].getLatitude(), locations[0].getLongitude()), 9));

        } else {
            int nbPOI = venues.size() / 5;
            dynamicZoomLevel(locations, nbPOI);
              /*  ProgressDialog progressDialog = new ProgressDialog(getActivity(), 1);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Venues on map...");
                progressDialog.show();
                dynamicZoomLevel(locations, nbPOI);
                Runnable dynamicUpdateZoomLevel = new Runnable() {
                    @Override
                    public void run() {
                        dynamicZoomLevel(locations, nbPOI);
                    }
                };
                Handler handler = new Handler();
                handler.post(dynamicUpdateZoomLevel);
                progressDialog.dismiss();*/
        }


    }


    public void dynamicZoomLevel(android.location.Location[] locations, int numberOfPOI) {
        int currentZoomLevel = MAX_ZOOM_LEVEL;
        int currentFoundPoi = 0;
        LatLngBounds bounds = null;
        List<Marker> foundMarkers = new ArrayList<Marker>();
        boolean keepZoomingOut = true;
        boolean keepSearchingForWithinRadius = true;
        android.location.Location centerLocation = locations[0];
        android.location.Location locationNE = locations[1];
        android.location.Location locationSW = locations[2];

        LatLng latLng = new LatLng(centerLocation.getLatitude(), centerLocation.getLongitude());
        LatLngBounds latLngBounds = createLatLngBoundsForAllVenues(locationNE, locationSW);
        while (keepZoomingOut) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, currentZoomLevel--));
            // bounds = map.getProjection().getVisibleRegion().latLngBounds;
            // android.location.Location centerBound=latlngToLocation( bounds.getCenter());
            //double distanceToNE=Math.round(centerLocation.distanceTo(latlngToLocation(bounds.northeast)) / 1000);
            //double distanceToSW=Math.round(centerLocation.distanceTo(latlngToLocation(bounds.southwest)) / 1000);
            double distanceToNE = Math.round(centerLocation.distanceTo(locationNE) / 1000);
            double distanceToSW = Math.round(centerLocation.distanceTo(locationSW) / 1000);
            keepSearchingForWithinRadius = !((distanceToNE > MAX_RADIUS) || (distanceToSW > MAX_RADIUS));
            Iterator<Marker> markers = markerVenueMap.keySet().iterator();
            while (markers.hasNext()) {
                Marker marker = markers.next();
                if (latLngBounds.contains(marker.getPosition())) {
                    if (!foundMarkers.contains(marker)) {
                        currentFoundPoi++;
                        foundMarkers.add(marker);
                    }
                }
                if (keepSearchingForWithinRadius) {
                    if (currentFoundPoi >= numberOfPOI) {
                        keepZoomingOut = false;
                        break;

                    }
                } else if (currentZoomLevel < MIN_ZOOM_LEVEL) {
                    keepZoomingOut = false;
                    break;
                }
            }
            keepZoomingOut = ((currentZoomLevel > 0) && keepZoomingOut);

        }
        System.out.println("### Zoom level: " + currentZoomLevel);


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
            tmpMarker.setPosition(friendLocation);
        } else {
            Marker tmp = map.addMarker(new MarkerOptions()
                    .position(friendLocation)
                    .title(friend.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.friend_position_1))
            );
            markerFriendMap.put(tmp, friend);
        }
        // load Images for marker
        if (friend.getAvatar() != null) {
            ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getContext());
            imageCacheLoader.loadBitmap(friend.getAvatar(), ImageSize.SMALL)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        friendBitmapMap.put(friend, bitmap);
                    },
                            throwable -> {
                                Log.d(LOG, "Exception: " + throwable.getMessage());
                            });
        }
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

            try {

                if (profileService == null) {

                    profileService = ServiceFactory
                            .createRetrofitService(ProfileService.class, URL, LocalStorage.
                                    getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));
                }
                profileService.getNearByFriends(userLocation)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(nearbyFriends -> {
                                    friends = nearbyFriends;
                                    for (User f : friends) {
                                        // for every user
                                        updateFriendsLocation(f);
                                    }
                                },
                                throwable -> {
                                    Log.d(LOG, "Exception: " + throwable.getMessage());
                                });
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Set the user on the Map
     */
    public void setUser() {

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
                                    if (user.getAvatar() == null)
                                        return;
                                    ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getContext());
                                    imageCacheLoader.loadBitmap(user.getAvatar(), ImageSize.SMALL)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(bitmap -> {
                                                userImage = bitmap;
                                            });
                                }, throwable ->{
                                    Log.d(LOG, "Exception: " + throwable.getMessage());
                                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //If position has not changed, no new Marker
        if ((myPosition != null) && (myPosition.getPosition().latitude == userLocation.getLatitude()) && (myPosition.getPosition().longitude == userLocation.getLongitude())) {
            return;
        }

        // If no user Marker yet
        if (myPosition == null && map != null) {
            LatLng tmp = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            myPosition = map.addMarker(new MarkerOptions()
                    .position(tmp)
                    .visible(true)
                    .title("That's you!").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_position)));
            return;
        }

        //if position changed and marker already set
        if ((myPosition != null) && (myPosition.getPosition().longitude != userLocation.getLongitude()) && (map != null)) {
            LatLng tmp = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            myPosition.setPosition(tmp);
            return;
        }
    }

    protected android.location.Location[] calculateBoundsOfLocations(List<Venue> venues) {
        double averageLat = 0.0;
        double averageLong = 0.0;
        double minLat = Double.MAX_VALUE;
        double maxLat = 0.0;
        double minLong = Double.MAX_VALUE;
        double maxLong = 0.0;
        int numberOfVenues = venues.size();

        for (Venue venue : venues) {
            minLat = Math.min(minLat, venue.getLocation().getLatitude());
            maxLat = Math.max(maxLat, venue.getLocation().getLatitude());
            minLong = Math.min(minLong, venue.getLocation().getLongitude());
            maxLong = Math.max(maxLong, venue.getLocation().getLongitude());
            averageLat += venue.getLocation().getLatitude() / numberOfVenues;
            averageLong += venue.getLocation().getLongitude() / numberOfVenues;
        }
        android.location.Location centerLocation = new android.location.Location("");
        centerLocation.setLongitude(averageLong);
        centerLocation.setLatitude(averageLat);
        android.location.Location locationNE = new android.location.Location("");
        locationNE.setLongitude(maxLong);
        locationNE.setLatitude(minLat);
        android.location.Location locationSW = new android.location.Location("");
        locationSW.setLongitude(minLong);
        locationSW.setLatitude(maxLat);

        return new android.location.Location[]{centerLocation, locationNE, locationSW};
    }

    public LatLngBounds createLatLngBoundsForAllVenues(android.location.Location locationNE, android.location.Location locationSW) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng boundsNE = new LatLng(locationNE.getLatitude(), locationNE.getLongitude());
        LatLng boundsSW = new LatLng(locationSW.getLatitude(), locationSW.getLongitude());
        builder.include(boundsNE);
        builder.include(boundsSW);
        return builder.build();
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
        setUser();
        // Update your Friends' Positions
        if ((LocalStorage.
                getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, "")) != "") {
            updateFriendsMarker();
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


    private android.location.Location latlngToLocation(LatLng dest) {
        android.location.Location loc = new android.location.Location("");
        loc.setLatitude(dest.latitude);
        loc.setLongitude(dest.longitude);
        return loc;
    }

}

