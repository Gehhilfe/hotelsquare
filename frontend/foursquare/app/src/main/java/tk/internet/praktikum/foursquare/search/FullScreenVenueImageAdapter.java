package tk.internet.praktikum.foursquare.search;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.bean.Image;

/**
 * Created by truongtud on 17.08.2017.
 */

public class FullScreenVenueImageAdapter extends PagerAdapter {
    private List<Image> venueImages;
    private Activity activity;
    private Context context;
    private View view;
    private LayoutInflater layoutInflater;
    ImageView venueImage;
    //public  FullScreenVenueImage
    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        layoutInflater = activity.getLayoutInflater();
        view = layoutInflater.inflate(R.layout.content_full_screen_venue_image, container, false);
        venueImage=(ImageView) view.findViewById(R.id.fullscreen_venue_image);
        renderImage(venueImages.get(position));
        container.addView(view);
        return  view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
    @Override
    public int getCount() {
        return venueImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
    public  void renderImage(Image image){
        ImageCacheLoader imageCacheLoader = new ImageCacheLoader(context);
        imageCacheLoader.loadBitmap(image, ImageSize.LARGE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    venueImage.setImageBitmap(bitmap);

                });
    }
}
