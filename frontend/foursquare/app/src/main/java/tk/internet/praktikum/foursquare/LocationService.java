package tk.internet.praktikum.foursquare;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LocationService extends Service {
    public LocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
