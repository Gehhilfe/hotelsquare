package tk.internet.praktikum.foursquare.search;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Image;
import tk.internet.praktikum.foursquare.api.service.VenueService;


public class VenueInDetailFragment extends Fragment {

    private final String URL = "https://dev.ip.stimi.ovh/";
    private final String LOG = VenueInDetailFragment.class.getSimpleName();
    private String venueId;
    private View view;

    private ImageView imageVenueOne;
    private ImageView imageVenueTwo;
    private ImageView imageVenueThree;
    private List<tk.internet.praktikum.foursquare.api.bean.Image> images;

    public static VenueInDetailFragment newInstance(String param1, String param2) {
        VenueInDetailFragment fragment = new VenueInDetailFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_venue_in_detail, container, false);
        imageVenueOne = (ImageView) view.findViewById(R.id.image_venue_one);
        imageVenueTwo = (ImageView) view.findViewById(R.id.image_venue_two);
        imageVenueThree = (ImageView) view.findViewById(R.id.image_venue_three);
        getVenueImages();
        return view;
    }

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public VenueInDetailFragment() {
        // Required empty public constructor
    }


    private void getVenueImages() {
        Log.d(LOG,"##### Venue Id: "+venueId);
        VenueService venueService = ServiceFactory.createRetrofitService(VenueService.class, URL);
        venueService.getDetails(venueId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(venue -> {
                            images = venue.getImages();
                            Log.d(LOG,"all images size: " + images.size());
                            if (images.size() > 0) {
                                Log.d(LOG,"++++ get images");
                                Image image = images.get(0);
                                ImageCacheLoader imageCacheLoader = new ImageCacheLoader(this.getContext());

                                Observable<Bitmap> bitmapObservable= imageCacheLoader.loadBitmap(image, ImageSize.MEDIUM);
                                Bitmap bitmap=imageCacheLoader.loadBitmap(image, ImageSize.MEDIUM).toFuture().get();
                                imageVenueOne.setImageBitmap(bitmap);
                                imageVenueTwo.setImageBitmap(bitmap);
                                imageVenueThree.setImageBitmap(bitmap);

                            }


                        },
                        throwable -> {
                            //TODO
                            //handle exception
                           // Log.d(LOG,"#### exception"+ throwable.getStackTrace());

                        }
                );
    }


}
