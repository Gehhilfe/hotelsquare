package tk.internet.praktikum.foursquare.search;

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
    public SearchResultViewHolder(View itemResult){
        super(itemResult);
        this.itemView=itemResult;
        name= (TextView) itemView.findViewById(R.id.item_name);
        address=(TextView)itemView.findViewById(R.id.item_address);
        rating=(TextView)itemView.findViewById(R.id.item_address);
        image=(ImageView) itemView.findViewById(R.id.item_image);
    }
    public void render(Venue searchResult){
        this.name.setText(searchResult.getName());
       //this.address.setText(searchResult.getPlace_id());
        this.address.setText(searchResult.getReference());
       // this.rating.setText(searchResult.getRating());
        //this.image.setImageBitmap(Utils.decodeResponsedInputStreamImage(searchResult.getImage()));
    }
    @Override
    public void onClick(View v) {
        //TODO
        // views each Venue in details

    }
}
