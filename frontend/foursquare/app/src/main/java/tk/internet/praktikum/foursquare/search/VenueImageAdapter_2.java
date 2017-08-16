package tk.internet.praktikum.foursquare.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.Image;

/**
 * Created by truongtud on 15.08.2017.
 */

public class VenueImageAdapter_2 extends RecyclerView.Adapter<VenueImageViewHolder> {
    List<Image> venueImages;
    Context context;
    public VenueImageAdapter_2(List<Image> venueImages){
        this.venueImages=venueImages;

    }


    @Override
    public VenueImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.venue_image_item,parent,false);
        VenueImageViewHolder venueImageViewHolder= new VenueImageViewHolder(view);
        venueImageViewHolder.setContext(context);
        return  venueImageViewHolder;
    }

    @Override
    public void onBindViewHolder(VenueImageViewHolder holder, int position) {
        Image image=venueImages.get(position);
        holder.renderImage(image);
    }

    @Override
    public int getItemCount() {
        return venueImages!=null?venueImages.size():0;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
