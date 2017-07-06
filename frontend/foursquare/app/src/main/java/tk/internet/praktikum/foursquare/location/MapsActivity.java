package tk.internet.praktikum.foursquare;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private Marker yourMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * On Map Ready
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // set Map
        // TODO: set current
        map = googleMap;
        // Just to test
        //map.addMarker(new MarkerOptions().position(new LatLng(49.877050, 8.654878)).title("Office"));
       //map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(49.877050, 8.654878), 17));
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent mIntent = new Intent(this, LocationService.class);
        startService(mIntent);

        // off-topic -> ignore this
        //EventBus.getDefault().register(this);
    }

    public void updateMap(Location location){
        if(yourMarker == null){
            yourMarker = map.addMarker(new MarkerOptions()
            .position(new LatLng(location.getLatitude(),location.getLongitude()))
            .title("Your Position"));
            }else{
                yourMarker.setPosition((new LatLng(location.getLatitude(),location.getLongitude())));
            }
        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));
        }

    //TODO
    public void updateFriendsmarker(Location[] locations){

    }

    //TODO
    public void updateVenuesMarker(Location[] locations){

    }

    @Override
    protected void onStop() {
        super.onStop();

        Intent mIntent = new Intent(this, LocationService.class);
        stopService(mIntent); // stop tracking service

        // off-topic -> ignore this
        //EventBus.getDefault().unregister(this);
    }

    /**
     * Listen for new database entries from background service
     * @param event
     */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationTracker.LocationEvent event) {
        Location location = event.location;
        updateMap(location);
    }


}
