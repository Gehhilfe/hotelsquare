package tk.internet.praktikum.foursquare;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.utils.AdjustedContextWrapper;

/**
 * Created by truongtud on 22.08.2017.
 */

public class HotelSquareApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getApplicationContext());
        String language=sharedPreferences.getString("LANGUAGE","de");
        Locale locale=new Locale(language);
        System.out.println("HotelSquareApp onCreate Language: "+language);
        AdjustedContextWrapper.wrap(getBaseContext(),language);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getApplicationContext());
        String language=sharedPreferences.getString("LANGUAGE","de");
        Locale locale=new Locale(language);
        System.out.println("HotelSquareApp onConfigurationChanged Language: "+language);
        AdjustedContextWrapper.wrap(getBaseContext(),language);
    }
}
