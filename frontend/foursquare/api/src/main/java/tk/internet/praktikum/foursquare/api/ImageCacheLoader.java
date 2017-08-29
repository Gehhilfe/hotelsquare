package tk.internet.praktikum.foursquare.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import tk.internet.praktikum.foursquare.api.bean.Image;

public class ImageCacheLoader {

    private static long cacheSize = 10 * 1024 * 1024;
    private static String baseUrl = "https://dev.ip.stimi.ovh/images/";
    private static OkHttpClient client;

    public ImageCacheLoader(Context context) {
        if (client == null) {
            Cache cache = new Cache(context.getCacheDir(), cacheSize);
            client = new OkHttpClient.Builder()
                    .cache(cache)
                    .build();
        }
    }

    /**
     * Loads image asynchronously while using
     *
     * @param image
     * @param size
     * @return
     */
    public Observable<Bitmap> loadBitmap(Image image, ImageSize size) {
        String url = baseUrl + image.getId() + "/" + size.getValue() + "/image.jpeg";
        return Observable.defer(() -> {
            // Build request
            Request.Builder request = new Request.Builder()
                    .url(url);
            okhttp3.Response response = client.newCall(request.build()).execute();
            try {
                InputStream inputStream = response.body().byteStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                response.close();
                if (bitmap == null)
                    return Observable.error(new UnsupportedOperationException("Bitmap could not be decoded"));
                else
                    return Observable.just(bitmap);
            } catch (Exception e) {
                return Observable.error(e);
            } finally {
                response.close();
            }
        });
    }
}
