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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;

import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.bean.Venue;

public class VenuesOnMapFragment extends Fragment implements OnMapReadyCallback {

    private View view;
    private GoogleMap map;
    private RecyclerView recyclerView;
    private String URL = "https://dev.ip.stimi.ovh/";;
    private Venue tmp;
    private Bitmap bmap;

    private Marker myPosition;

    private Map <Marker, Venue> markerVenueMap;
    private Map <Marker, User> markerFriendMap;
    private Map <Venue, Bitmap> venueBitmapMap;

    public VenuesOnMapFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_venues_on_map, container, false);

        recyclerView =(RecyclerView) view.findViewById(R.id.searching_results_on_map);
        SupportMapFragment mapFragment =((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.venues_mapView));
        mapFragment.getMapAsync(this);

        markerVenueMap = new HashMap<Marker, Venue>();
        markerFriendMap = new HashMap<Marker, User>();
        venueBitmapMap = new HashMap<Venue, Bitmap>();

        this.setRetainInstance(true);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // set Map
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);

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


                    // Retrieve Data from specific Venue
                    //VenueService venueService = ServiceFactory.createRetrofitService(VenueService.class, URL);
                    //venueService.getDetails(tmp.getId()).subscribeOn(Schedulers.newThread())
                    //        .observeOn(AndroidSchedulers.mainThread()).subscribe(venue -> {
                    //        tmp = venue;
                    //    Log.d("GOTDETAILS", "Details for: " + venue.getName());
                    //    Log.d("GOTDETAILS", "Details are: " + venue.getTypes() + venue.getRating());
                    //}, throwable -> {
                    //    //TODO: handle exception
                    //});


                    // Set InfoWindow Text
                    //TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
                    //tvTitle.setText(tmp.getName());
                    //TextView tvAddress = ((TextView) myContentsView.findViewById(R.id.adress));
                    //tvAddress.setText(tmp.getFormattedAddress());
                    //TextView tvRate = ((TextView) myContentsView.findViewById(R.id.rate));
                    //tvRate.setText(tmp.getFormattedAddress());

                    //TODO: Get Info from Venue or Friend
                    //ImageView ivImage = ((ImageView) myContentsView.findViewById(R.id.img));
                    //Drawable picture =
                    //ivImage.setImageDrawable(drawable);
                    //ivImage.setImageRe
                    //TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
                    //tvTitle.setText(marker.getTitle());
                    //TextView tvSnippet = ((TextView) myContentsView.findViewById(R.id.snippet));
                    //tvSnippet.setText(marker.getSnippet());


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

                }
            }
        });

        //TODO
        //map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
         //   @Override
          //  public void onInfoWindowClick(Marker marker) {

         //   }
        //});

        //map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){

          //  @Override
           // public boolean onMarkerClick(Marker marker) {
           //     if(markerVenueMap.containsKey(marker)){
            //        Venue v = markerVenueMap.get(marker);
            //        //TODO open new Fragment/Activity
            //        return true;
              //  }
                //else if(markerFriendMap.containsKey(marker)){
                //Friend f = markerFriendMap.get(marker);
                //TODO: open new Fragment/Activity
                // return true;
                //}
           // return false;
            //}

       // });

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

        //Shouldn't we move the Camera to the User's Position?
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(venues.get(0).getLocation().getLatitude(), venues.get(0).getLocation().getLongitude()),14));

    }

    //public void updateFriendsLocation(Friend friend){
    //LatLng friendLocation = new LatLng(friend.getLocation().getLatitude(), friend.getLocation().getLongitude());
    // Marker tmp = map.addMarker(new MarkerOptions()
    //        .position(friendLocation)
    //       .title(friend.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.friend_position))
    // );
    // markerFriendMap.put(tmp, friend);



    public void setUser(){
        //TODO get LocationData...
        myPosition = map.addMarker(new MarkerOptions()
        .position(new LatLng(0,0))
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
