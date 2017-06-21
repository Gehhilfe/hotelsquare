package tk.internet.praktikum.foursquare;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class old_LocationService extends Service {

    private final IBinder binder = new LocalBinder();
    private LocationManager locationManager = null;
    private static int LOCATION_INTERVAL = 0;
    private static float LOCATION_DISTANCE = 0;
    private Location lastLocation = null;

    public class LocalBinder extends Binder {
        // return this Service
        old_LocationService getService(){
            return old_LocationService.this;
        }
    }
    // Location Listener
    private class LocationListener implements android.location.LocationListener{
        Location lastLocation;

        public LocationListener(String provider){

            lastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {

            lastLocation.set(location);
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

        public Location getLastLocation(){
            return lastLocation;
        }
    }

    LocationListener[] locationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    public old_LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    // get Location
    public Location getLastLocation(){
        if(locationListeners[1].getLastLocation() != null){
            lastLocation = locationListeners[1].getLastLocation();
        } else if(locationListeners[0].getLastLocation() != null){
            lastLocation = locationListeners[0].getLastLocation();
        }
        return lastLocation;
    }

    @Override
    public void onCreate()
    {
        initializeLocationManager();
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListeners[1]);
        } catch (java.lang.SecurityException ex) {
           //TODO
        } catch (IllegalArgumentException ex) {
            //TODO
        }
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListeners[0]);
        } catch (java.lang.SecurityException ex) {
           //TODO
        } catch (IllegalArgumentException ex) {
            //TODO
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (locationManager != null) {
            for (int i = 0; i < locationListeners.length; i++) {
                try {
                    locationManager.removeUpdates(locationListeners[i]);
                } catch (Exception ex) {
                  //TODO
                }
            }
        }
    }

    private void initializeLocationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
