package tk.internet.praktikum.foursquare.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.Venue;

//import tk.internet.praktikum.foursquare.api.bean.Location;


public class VenuesOnMapFragment extends Fragment implements OnMapReadyCallback {
    private View view;
    private GoogleMap map;


    public VenuesOnMapFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_venues_on_map, container, false);
        SupportMapFragment mapFragment =((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.venues_mapView));
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // set Map
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);

    }


    public void updateVenueLocation(Venue venue,int ranking){
        LatLng venueLocation = new LatLng(venue.getLocation().getLatitude(), venue.getLocation().getLongitude());
        map.addMarker(new MarkerOptions()
                    .position(venueLocation).title(String.valueOf(ranking)));

    }


    public void updateVenuesMarker(List<Venue> venues){
        map.clear();
        int ranking=1;
        for(Venue venue:venues){
            updateVenueLocation(venue,ranking);
            ranking++;
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(venues.get(0).getLocation().getLatitude(), venues.get(0).getLocation().getLongitude()),14));

    }




}
