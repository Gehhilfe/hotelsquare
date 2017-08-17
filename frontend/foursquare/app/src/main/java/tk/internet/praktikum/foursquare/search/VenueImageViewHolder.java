package tk.internet.praktikum.foursquare.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.bean.Image;


public class VenueImageViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
    private View view;
    private ImageView venue_image;
    private Activity activity;

    Context context;
    public VenueImageViewHolder(View itemView) {
        super(itemView);
        view=itemView;
        venue_image= (ImageView) view.findViewById(R.id.venue_image_item);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public  void renderImage(Image image){
        ImageCacheLoader imageCacheLoader = new ImageCacheLoader(context);
        imageCacheLoader.loadBitmap(image, ImageSize.MEDIUM)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                   venue_image.setImageBitmap(bitmap);

                });
    }

    @Override
    public void onClick(View v) {
        int position=this.getLayoutPosition();
        Intent intent = new Intent(activity, FullScreenVenueImage.class);
        intent.putExtra("position", position);
        activity.startActivity(intent);
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }


}
