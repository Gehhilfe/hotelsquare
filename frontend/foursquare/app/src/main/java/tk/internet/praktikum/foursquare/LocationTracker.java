package tk.internet.praktikum.foursquare;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by Marco Huber on 14.06.2017.
 */

public class LocationTracker implements LocationListener,
        ConnectionCallbacks,
        OnConnectionFailedListener{

    private static final long INT = 1000 * 15;
    private static final long FASTEST_INT = 1000 *  10;
    private Context context;
    private GoogleApiClient googleApiClient;
    private LocationRequest locRequest;
    private Location currentLocation;

    public LocationTracker(Context context) {
        this.context = context;

        init();
    }

    private void init() {
       // if (!GoogleApiAvailability.isGooglePlayServicesAvailable(context)) return;

        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // Permissions missing
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Nothing to do
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Connection Failed
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        // TODO: pass
    }
}
