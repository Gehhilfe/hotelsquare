package tk.internet.praktikum.foursquare.location;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import tk.internet.praktikum.foursquare.MainActivity;

/**
 * Handles the Location Tracker
 */

public class LocationService extends Service{

    private LocationTracker locationTracker;
    private final static int SERVICE_NOTIFICATION_ID = 123456;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){

        // start tracker
        locationTracker = new LocationTracker(this);
        locationTracker.start(false);
        return START_STICKY;
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        if(locationTracker != null){locationTracker.stop();};
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {return null;}

}
