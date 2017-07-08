package tk.internet.praktikum.foursquare.search;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

/**
 * Created by truongtud on 02.07.2017.
 */

public class Utils {

    public  static Bitmap decodeResponsedInputStreamImage(InputStream inputStream){
        return BitmapFactory.decodeStream(inputStream);
    }
    public static Bitmap decodeResponsedInputArrayImage(byte[] imageBytes){
        return  BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length);
    }

}
