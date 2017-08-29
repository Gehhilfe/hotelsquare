package tk.internet.praktikum.foursquare.search;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.io.InputStream;



public class Utils {

    public  static Bitmap decodeResponsedInputStreamImage(InputStream inputStream){
        return BitmapFactory.decodeStream(inputStream);
    }
    public static Bitmap decodeResponsedInputArrayImage(byte[] imageBytes){
        return  BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length);
    }

    public static Bitmap decodeResourceImage(Context context,String drawableName, int width, int height) throws  Exception {
        Drawable drawable =context.getDrawable(context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName()));
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
       return  bitmap;
    }
}
