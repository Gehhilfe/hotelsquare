package tk.internet.praktikum.foursquare.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.Venue;

//import tk.internet.praktikum.foursquare.api.bean.Location;


public class VenuesOnMapFragment extends Fragment implements OnMapReadyCallback {

    private View view;
    private GoogleMap map;
    private RecyclerView recyclerView;

    private Marker myPosition;

    private Map <Marker, Venue> markerVenueMap;
    // TODO private Map <Marker, Friend> markerFriendMap;

    public VenuesOnMapFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_venues_on_map, container, false);
       recyclerView =(RecyclerView) view.findViewById(R.id.searching_results_on_map);
        SupportMapFragment mapFragment =((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.venues_mapView));
        mapFragment.getMapAsync(this);
        markerVenueMap=new HashMap<Marker,Venue>();

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // set Map
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){

            @Override
            public boolean onMarkerClick(Marker marker) {
                if(markerVenueMap.containsKey(marker)){
                    Venue v = markerVenueMap.get(marker);
                    //TODO open new Fragment/Activity
                    return true;
                }
                //else if(markerFriendMap.containsKey(marker)){
                //Friend f = markerFriendMap.get(marker);
                //TODO: open new Fragment/Activity
                // return true;
                //}
            return false;
            }

        });

    }


    public void updateVenueLocation(Venue venue,int ranking){
        LatLng venueLocation = new LatLng(venue.getLocation().getLatitude(), venue.getLocation().getLongitude());
        Marker tmp = map.addMarker(new MarkerOptions()
                    .position(venueLocation)
                    .title(venue.getName() + String.valueOf(ranking)));
        markerVenueMap.put(tmp, venue);

    }


    public void updateVenuesMarker(List<Venue> venues){
        map.clear();
        int ranking = 1;
        for(Venue venue:venues){
            updateVenueLocation(venue, ranking);
            ranking++;
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
