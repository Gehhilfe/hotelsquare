package tk.internet.praktikum.storage;

import android.content.Context;
import android.content.SharedPreferences;

import tk.internet.praktikum.foursquare.R;

/**
 * Created by truongtud on 16.05.2017.
 */

public class LocalStorage {


    private static LocalStorage localStorage;

    public static LocalStorage getLocalStorageInstance(){
        return localStorage!=null? localStorage: new LocalStorage();
    }

    private SharedPreferences sharedPreferences;


    private void createSharedPreferences(Context context){
        sharedPreferences=context.getSharedPreferences(context.getString(R.string.user_preferences),context.MODE_PRIVATE);
    }


}
