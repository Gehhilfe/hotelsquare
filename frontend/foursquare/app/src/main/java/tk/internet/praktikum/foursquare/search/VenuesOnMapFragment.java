package tk.internet.praktikum.foursquare.search;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Location;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.service.ProfileService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.user.MeFragment;

//import android.location.Location;

public class VenuesOnMapFragment extends Fragment implements OnMapReadyCallback {

    private View view;
    private GoogleMap map;
    private RecyclerView recyclerView;
    private String URL = "https://dev.ip.stimi.ovh/";
    private Venue tmp;
    private Bitmap bmap;
    private Location userLocation;

    private Marker myPosition;
    private List<User> friends  = new ArrayList<User>();

    private Map <Marker, Venue> markerVenueMap;
    private Map <Marker, User> markerFriendMap;
    private Map <Venue, Bitmap> venueBitmapMap;
    private Map <User, Bitmap> friendBitmapMap;

    public VenuesOnMapFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_venues_on_map, container, false);

        //recyclerView =(RecyclerView) view.findViewById(R.id.searching_results_on_map);
        SupportMapFragment mapFragment =((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.venues_mapView));
        mapFragment.getMapAsync(this);

        markerVenueMap = new HashMap<Marker, Venue>();
        markerFriendMap = new HashMap<Marker, User>();
        venueBitmapMap = new HashMap<Venue, Bitmap>();
        friendBitmapMap = new HashMap<User, Bitmap>();

        userLocation = new Location(8.656868, 49.876171);
        this.setRetainInstance(true);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // set Map
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        //setUser();
        //TODO:
        //updateFriendsMarker();

        class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

            private final View myContentsView;

            MyInfoWindowAdapter(Context context) {
                LayoutInflater inflater = LayoutInflater.from(context);
                myContentsView = inflater.inflate(R.layout.info_window, null);
            }
            @Override
            public View getInfoContents(Marker marker) {

                // Get Info from Venue
                if (markerVenueMap.containsKey(marker)) {
                    Log.d("KEYFOUND", "Marker was Venue");

                    Venue venue = markerVenueMap.get(marker);

                    // Set InfoWindow Text
                    TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
                    tvTitle.setText(venue.getName());
                    TextView tvRate = ((TextView) myContentsView.findViewById(R.id.rate));
                    Log.d("KEYFOUND", "Rate is: " + venue.getRating());
                    tvRate.setText(Float.toString(venue.getRating()));
                    TextView tvOpen = ((TextView) myContentsView.findViewById(R.id.isopen));
                    if (venue.isOpen()) {
                        tvOpen.setText(getString(R.string.open_now));
                    }
                    ImageView venueImage = ((ImageView) myContentsView.findViewById(R.id.img));
                    if (venueBitmapMap.containsKey(venue)) {
                        Log.d("KEYFOUND", "Image for Venue was found");
                        venueImage.setImageBitmap(venueBitmapMap.get(venue));
                    } else {
                        //TODO: Other Pic?
                        venueImage.setImageResource(R.mipmap.ic_location_city_black_24dp);
                    }

                } else if(markerFriendMap.containsKey(marker)){
                    Log.d("KEYFOUND", "Marker was Friend");

                    User friend = markerFriendMap.get(marker);

                    // Set InfoWindow Text
                    TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
                    tvTitle.setText(friend.getDisplayName());

                    ImageView venueImage = ((ImageView) myContentsView.findViewById(R.id.img));
                    venueImage.setImageBitmap(friendBitmapMap.get(friend));

                } else {
                    Log.d("KEYFOUND", "Marker was User");

                }
                return myContentsView;
            }

            @Override
            public View getInfoWindow(Marker marker) {
                // TODO Auto-generated method stub
                return null;
            }

        }

        map.setInfoWindowAdapter(new MyInfoWindowAdapter(this.getActivity()));

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener(){
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(markerVenueMap.containsKey(marker)){
                    VenueInDetailFragment venueInDetailFragment=new VenueInDetailFragment();
                    venueInDetailFragment.setVenueId(markerVenueMap.get(marker).getId());
                    FragmentTransaction fragmentTransaction = VenuesOnMapFragment.this.getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, venueInDetailFragment);
                    fragmentTransaction.addToBackStack(venueInDetailFragment.getTag());
                    fragmentTransaction.commit();
                    //redirectToFragment(venueInDetailFragment);

                } else if(markerFriendMap.containsKey(marker)){
                    //TODO: "Call FriendFragment"
                } else {
                    MeFragment meFragment = new MeFragment();
                    FragmentTransaction fragmentTransaction = VenuesOnMapFragment.this.getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, meFragment);
                    fragmentTransaction.addToBackStack(meFragment.getTag());
                    fragmentTransaction.commit();
                }
            }
        });

    }

    public void updateVenueLocation(Venue venue){
        LatLng venueLocation = new LatLng(venue.getLocation().getLatitude(), venue.getLocation().getLongitude());

        //setup Marker
        Marker tmp = map.addMarker(new MarkerOptions()
                    .position(venueLocation)
                    .title(venue.getName()));

        //set specific marker Icon
        float rating = venue.getRating();
        if(rating == 0){
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_grey_24dp));
        } else if(rating > 0 && rating <= 1){
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_red_24dp));
        } else if(rating > 1 && rating <= 2){
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_orange_24dp));
        } else if(rating > 2 && rating <= 3) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_yellow_24dp));
        } else if(rating > 3 && rating <= 4) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_lime_24dp));
        }else if(rating > 4) {
            tmp.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_green_24dp));
        }

        markerVenueMap.put(tmp, venue);

        // load Images for marker
        if(venue.getImages().size() > 0){
            ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getContext());
            imageCacheLoader.loadBitmap(venue.getImages().get(0), ImageSize.SMALL)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    venueBitmapMap.put(venue, bitmap);
                });
        }



}


    public void updateVenuesMarker(List<Venue> venues){
        map.clear();
        for(Venue venue:venues){
            updateVenueLocation(venue);
        }
        setUser();
        updateFriendsMarker();
        //Shouldn't we move the Camera to the User's Position?
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(venues.get(0).getLocation().getLatitude(), venues.get(0).getLocation().getLongitude()),14));

    }

    public void updateFriendsLocation(User friend) {
        LatLng friendLocation = new LatLng(friend.getLocation().getLatitude(), friend.getLocation().getLongitude());
        Marker tmp = map.addMarker(new MarkerOptions()
                .position(friendLocation)
                .title(friend.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.friend_position))
        );
        markerFriendMap.put(tmp, friend);

        // load Images for marker
        Log.d("KEYFOUND", "Is not null: " + friendBitmapMap.size() + " " + friend + " " + friendBitmapMap.containsKey(friend));
        if(friend.getAvatar() != null){
        ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getContext());
            imageCacheLoader.loadBitmap(friend.getAvatar(), ImageSize.SMALL)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        friendBitmapMap.put(friend, bitmap);
                    });
         }
    }

    public void updateFriendsMarker(){

            //TODO: Doesn't work? Timeout
            ProfileService profileService = ServiceFactory
                    .createRetrofitService(ProfileService.class, URL, LocalStorage.
                            getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

            //ProfileService profileService = ServiceFactory.createRetrofitService(ProfileService.class, URL);

            profileService.getNearByFriends(userLocation)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(nearbyFriends -> {
                        friends = nearbyFriends;
                        Log.d("KEYFOUND", "Size of Nearby Friends " + friends.size());

                        for(User f : friends){
                            updateFriendsLocation(f);
                            Log.d("KEYFOUND", "SetMarker: " + f);
                        }

                    });

    }

    public void setUser(){
        //TODO get LocationData...
        myPosition = map.addMarker(new MarkerOptions()
        .position(new LatLng(userLocation.getLatitude(),userLocation.getLongitude()))
        .title("That's you!").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_position)));

    }

    /**
     * update new venues list
     * @param venues
     */
    protected   void updateRecyclerView(List<Venue> venues){
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(new SearchResultAdapter(this,venues));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),LinearLayoutManager.HORIZONTAL));

    }

}
