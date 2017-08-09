package tk.internet.praktikum.foursquare.location;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import tk.internet.praktikum.foursquare.MainActivity;

/**
 * Location Service which handle the LocationTracker
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
        locationTracker.start();

        //Intent resultIntent = new Intent(this, MainActivity.class);
        //android.support.v4.app.TaskStackBuilder stackBuilder = android.support.v4.app.TaskStackBuilder.create(this);
        //stackBuilder.addParentStack(MainActivity.class);
        //stackBuilder.addNextIntent(resultIntent);

        //PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        return START_STICKY;
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        //if(locationTracker != null) locationTracker.stop();
        locationTracker.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {return null;}

}
