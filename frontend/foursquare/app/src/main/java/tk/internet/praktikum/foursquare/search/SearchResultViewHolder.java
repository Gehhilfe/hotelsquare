package tk.internet.praktikum.foursquare.search;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.bean.Image;
import tk.internet.praktikum.foursquare.api.bean.Venue;


public class SearchResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private View itemView;
    private TextView name;
    private TextView address;
    private TextView rating;
    private ImageView image;
    private TextView shortNameOverImage;
    private SearchResultAdapterListener searchResultAdapterListener;
    private Context context;

    public SearchResultViewHolder(View itemResult,SearchResultAdapterListener searchResultAdapterListener){
        super(itemResult);
        this.searchResultAdapterListener=searchResultAdapterListener;
        this.itemView=itemResult;
        name= (TextView) itemView.findViewById(R.id.item_name);
        address=(TextView)itemView.findViewById(R.id.item_address);
        rating=(TextView)itemView.findViewById(R.id.rating);
        image=(ImageView) itemView.findViewById(R.id.item_image);
        shortNameOverImage=(TextView)itemView.findViewById(R.id.item_short_name);
        itemView.setOnClickListener(this);
    }
    public void render(Venue searchResult){
        this.name.setText(searchResult.getName());
        this.address.setText(searchResult.getFormattedAddress());
        List<Image> images=searchResult.getImages();
        if(images.size()>0) {
            Image image = images.get(0);
            ImageCacheLoader imageCacheLoader = new ImageCacheLoader(this.getContext());
            imageCacheLoader.loadBitmap(image, ImageSize.SMALL)
                .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(bitmap -> {
                      this.image.setImageBitmap(bitmap);
                      this.image.setVisibility(View.VISIBLE);
                      this.shortNameOverImage.setVisibility(View.GONE);
                  });
        }
        else {

            this.image.setDrawingCacheEnabled(true);
            Bitmap bitmap= null;
            try {
                bitmap = Utils.decodeResourceImage(getContext(),"default_image",50,50);
                this.image.setImageBitmap(bitmap);
                this.shortNameOverImage.setText(searchResult.getName().substring(0,1).toUpperCase());
                this.shortNameOverImage.setVisibility(View.VISIBLE);
                this.image.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
            }

            //this.image.setVisibility(View.GONE);
        }
        this.rating.setText(String.valueOf(searchResult.getRating()));

    }
    @Override
    public void onClick(View v) {
        searchResultAdapterListener.clickOnVenue(String.valueOf(this.getLayoutPosition()));
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
