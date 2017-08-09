package tk.internet.praktikum.foursquare.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.Image;

public class VenueImagesFragment extends Fragment {
    private View view;
    private RecyclerView recyclerView;
    private List<Image> images;
    private VenueImageAdapter venueImageAdapter;


    private Fragment parent;
    public VenueImagesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.venue_images, container, false);
        recyclerView=(RecyclerView) view.findViewById(R.id.all_venue_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(),2));
        renderImages();
        return  view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public Fragment getParent() {
        return parent;
    }

    public void setParent(Fragment parent) {
        this.parent = parent;
    }

    private void renderImages() {
        venueImageAdapter = new VenueImageAdapter(images);
        venueImageAdapter.setParentFragment(this.getParent());
        recyclerView.setAdapter(venueImageAdapter);

    }
    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

}
