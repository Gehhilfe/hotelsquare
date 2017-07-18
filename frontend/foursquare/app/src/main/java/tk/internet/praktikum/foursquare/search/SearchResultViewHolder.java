package tk.internet.praktikum.foursquare.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.Venue;

/**
 * Created by truongtud on 02.07.2017.
 */

public class SearchResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private View itemView;
    private TextView name;
    private TextView address;
    private TextView rating;
    private ImageView image;
    private SearchResultAdapterListener searchResultAdapterListener;
    private Context context;
    public SearchResultViewHolder(View itemResult,SearchResultAdapterListener searchResultAdapterListener){
        super(itemResult);
        this.searchResultAdapterListener=searchResultAdapterListener;
        this.itemView=itemResult;
        name= (TextView) itemView.findViewById(R.id.item_name);
        address=(TextView)itemView.findViewById(R.id.item_address);
        rating=(TextView)itemView.findViewById(R.id.item_address);
        image=(ImageView) itemView.findViewById(R.id.item_image);
        itemView.setOnClickListener(this);
    }
    public void render(Venue searchResult){
        this.name.setText(searchResult.getName());
       //this.address.setText(searchResult.getPlace_id());
        this.address.setText(searchResult.getReference());
       /* List<Image> images=searchResult.getImages();
        //System.out.println("all images size: "+ images.size());
        if(images.size()>0) {
            Image image = images.get(0);
            ImageCacheLoader imageCacheLoader = new ImageCacheLoader(this.getContext());
            this.image.setImageBitmap(imageCacheLoader.loadBitmap(image, ImageSize.SMALL).blockingFirst());
        }*/
       // this.rating.setText(searchResult.getRating());
        //this.image.setImageBitmap(Utils.decodeResponsedInputStreamImage(searchResult.getImage()));
    }
    @Override
    public void onClick(View v) {
        System.out.println(" clicked on venue item");
        searchResultAdapterListener.clickOnVenue(String.valueOf(this.getLayoutPosition()));
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
