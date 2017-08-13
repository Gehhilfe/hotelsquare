package tk.internet.praktikum.foursquare.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.search.DeepSearchFragment;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

/**
 * LocationTracker to track Location of the User
 */
public class LocationTracker implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * LocationEvent for greenrobot.EventBus
     */
    public static class LocationEvent {
        public Location location;

        public LocationEvent(Location location) {
            this.location = location;
        }
    }

    private final static String TAG = LocationTracker.class.getSimpleName();

    // Interval to receive updates
    private static final long INTERVAL = 1000;
    // Fastest Interval to receive updates
    private static final long FASTEST_INTERVAL = 1000;

    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;

    /**
     * Constructor
     *
     * @param context
     */
    public LocationTracker(Context context) {
        this.context = context;

        init();
    }

    /**
     * Initialize GoogleApiClient
     */
    private void init() {
        if (!isGooglePlayServicesAvailable(context)) return;

        this.mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Start GoogleApiClient-Tracking with Balanced Power/Accuracy,
     */
    public void start(boolean search) {
        EventBus.getDefault().register(this);
        if (mGoogleApiClient != null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

            if(search){
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            } else {
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            }

            mGoogleApiClient.connect();
        }
    }

    /**
     * Stop Tracking
     */
    public void stop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onConnected(Bundle bundle) {

        // No Permissions
        if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            //TODO: Get Permissions in Runtime isn't that easy due to the need of an activtiy reference
            Toast.makeText(context,R.string.permissongrant, Toast.LENGTH_LONG).show();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        // Post to EventBus
        Log.d("KEYFOUND", "Priority is: " + mLocationRequest.getPriority());
        EventBus.getDefault().post(new LocationEvent(mCurrentLocation));
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Nothing to do
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    /**
     * Checks of GooglePlayService is available
     *
     * @param context
     * @return true, if available otherwise false
     */
    public static boolean isGooglePlayServicesAvailable(Context context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            return false;
        }
    }

    public void onSearch(){
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void noSearch(){
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeepSearchFragment.SearchEvent event) {
        if(event.isSearch == true) {
            stop();
            start(true);
            Log.d("KEYFOUND", "Set On Search");
        }else{
            stop();
            start(false);
            Log.d("KEYFOUND", "Set NO Search");
        }
    }

}

