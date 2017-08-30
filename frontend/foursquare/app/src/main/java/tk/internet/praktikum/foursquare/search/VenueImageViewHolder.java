package tk.internet.praktikum.foursquare.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.bean.Image;


public class VenueImageViewHolder extends RecyclerView.ViewHolder  implements  View.OnClickListener{
    private View view;
    private ImageView venue_image;
    private Context context;
    private VenueImageAdapter.OnClickVenueImageListener onClickVenueImageListener;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public VenueImageViewHolder(View itemView, VenueImageAdapter.OnClickVenueImageListener onClickVenueImageListener) {
        super(itemView);
        view=itemView;
        venue_image= (ImageView) view.findViewById(R.id.venue_image_item);
        this.onClickVenueImageListener=onClickVenueImageListener;
        view.setOnClickListener(this);
    }

    public  void renderImage(Image image , int position, VenueImageAdapter.OnClickVenueImageListener onClickVenueImageListener){
        ImageCacheLoader imageCacheLoader = new ImageCacheLoader(context);
        imageCacheLoader.loadBitmap(image, ImageSize.MEDIUM)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    venue_image.setImageBitmap(bitmap);

                }, throwable -> Log.d(VenueImageViewHolder.class.getSimpleName(), throwable.getMessage()));
    }

    @Override
    public void onClick(View v) {
        onClickVenueImageListener.onClick( this.getLayoutPosition());
    }
}
