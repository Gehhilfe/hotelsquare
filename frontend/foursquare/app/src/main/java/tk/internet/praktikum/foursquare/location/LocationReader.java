package tk.internet.praktikum.foursquare.location;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

/**
 * Created by truongtud on 03.07.2017.
 */

public class LocationReader extends Service implements LocationListener {
    private static LocationReader locationReader;
    private LocationManager locationManager;
    private static final long MIN_TIME_FOR_UPDATE = 1000 * 60;
    private static final long MIN_DISTANCE_FOR_UPDATE = 10;
    private Context lContext;

    public static LocationReader getLocationReader(Context context) {
        return (locationReader != null) ? locationReader : new LocationReader(context);
    }

    public LocationReader(Context context) {
        lContext = context;
        locationManager = (LocationManager) context
                .getSystemService(LOCATION_SERVICE);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Location getCurrentLocation(String provider) {
        if (checkPermission()&&locationManager.isProviderEnabled(provider)) {
                locationManager.requestLocationUpdates(provider, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_FOR_UPDATE, this);
                return locationManager.getLastKnownLocation(provider);


        } else {
            // TOdo something
            return null;
        }


    }

    public boolean checkPermission() {
        System.out.println(lContext!=null);
        return   (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);

        }
}
