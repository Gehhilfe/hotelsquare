package tk.internet.praktikum.foursquare.search;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.Image;


public class VenueImageAdapter extends RecyclerView.Adapter<VenueImageViewHolder> {
    List<Image> venueImages;
    Fragment parentFragment;


    public VenueImageAdapter(List<Image> venueImages){
        this.venueImages=venueImages;

    }

    public Fragment getParentFragment() {
        return parentFragment;
    }

    public void setParentFragment(Fragment parentFragment) {
        this.parentFragment = parentFragment;
    }

    @Override
    public VenueImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.venue_image_item,parent,false);
        VenueImageViewHolder venueImageViewHolder= new VenueImageViewHolder(view);
        venueImageViewHolder.setContext(parentFragment.getContext());
        return  venueImageViewHolder;
    }

    @Override
    public void onBindViewHolder(VenueImageViewHolder holder, int position) {
        Image image=venueImages.get(position);
        holder.renderImage(image);
    }

    @Override
    public int getItemCount() {
        return venueImages.size();
    }

}
