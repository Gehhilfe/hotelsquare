package tk.internet.praktikum.foursquare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;

/**
 * tracks the Location
 */

public class LocationTracker implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // TAG
    private final static String TAG = LocationTracker.class.getSimpleName();

    // INTERVALS TODO: SET
    private static final long INTERVAL = 10 * 45;
    private static final long FASTEST_INTERVAL = 10 * 30;

    //Variables
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;

    // LocationEvent class
    public static class LocationEvent {
        public Location location;

        public LocationEvent(Location location) {
            this.location = location;
        }
    }

    public LocationTracker(Context context) {
        this.context = context;
        init();
    }

    // Setup ApiClient
    private void init() {
        if (!isGooglePlayServicesAvailable(context)) return;

        this.mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    // Start and Connect
    public void start() {
        if (mGoogleApiClient != null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            mGoogleApiClient.connect();
        }
    }

    // Stop and Disconnect
    public void stop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            mGoogleApiClient.disconnect();
            Log.d(TAG, "Location::stop");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // check Permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location tracking::Permission is missing");
            Log.i(TAG, "Pls grant location permission: Settings > Apps > <This App> > Location");
            return;
        }

        // Start Location tracking
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location::start");
    }

    // On ChangeListener
    @Override
    public void onLocationChanged(Location location) {
        // set new Location
        mCurrentLocation = location;
        Log.d(TAG, "Location("+location.getLatitude()+","+location.getLongitude()+")");
        // Post to Bus
        EventBus.getDefault().post(new LocationEvent(mCurrentLocation));
    }

    // Nothing to Do
    @Override
    public void onConnectionSuspended(int i) {

    }

    // On Connection Failed
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    // Check if Available
    public static boolean isGooglePlayServicesAvailable(Context context) {
        // deprecated, but no way to fix due to version issues?
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            return false;
        }
    }
}