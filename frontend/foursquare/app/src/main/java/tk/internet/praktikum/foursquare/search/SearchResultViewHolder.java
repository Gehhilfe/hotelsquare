package tk.internet.praktikum.foursquare.search;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.Image;
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
        List<Image> images=searchResult.getImages();

       // this.rating.setText(searchResult.getRating());
        //this.image.setImageBitmap(Utils.decodeResponsedInputStreamImage(searchResult.getImage()));
    }
    @Override
    public void onClick(View v) {
        System.out.println(" clicked on venue item");
        searchResultAdapterListener.clickOnVenue(String.valueOf(this.getLayoutPosition()));
    }


}
