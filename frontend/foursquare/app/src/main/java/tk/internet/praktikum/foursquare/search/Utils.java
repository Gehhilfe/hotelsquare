package tk.internet.praktikum.foursquare.search;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

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

    public static Bitmap decodeResourceImage(Resources resources, int id,int width,int height) throws  Exception {
        Drawable drawable = resources.getDrawable(id, null);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
       return  bitmap;
    }
}
