package tk.internet.praktikum.foursquare.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


public class UploadHelper {
    private static final String LOG_TAG = UploadHelper.class.getSimpleName();

    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            Log.i(LOG_TAG, "Image resized");
            return image;
        } else {
            return image;
        }
    }

    private static byte[] persistImage(Bitmap bitmap, boolean resize) {
        Bitmap resized;
        if(resize)
            resized = resize(bitmap, 1920, 1080);
        else
            resized = bitmap;
        try {
            Log.i(LOG_TAG, "Render image as bitmap with 80% quality");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 80, os);
            os.flush();
            os.close();
            return os.toByteArray();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error writing bitmap", e);
            throw new UnsupportedOperationException();
        }
    }

    public static MultipartBody.Part createMultipartBodySync(Bitmap bm, Context context, boolean resize) {
        byte[] payload = persistImage(bm, resize);
        if(payload == null)
            throw new UnsupportedOperationException();

        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("image/jpeg"),
                        payload
                );
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("image", "upload.jpg", requestFile);

        Log.i(LOG_TAG, "Multipart body for image created");
        return body;
    }
}
