package tk.internet.praktikum.foursquare.search;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Image;
import tk.internet.praktikum.foursquare.api.bean.Location;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.service.VenueService;


public class VenueInDetailFragment extends Fragment implements OnMapReadyCallback {

    private final String URL = "https://dev.ip.stimi.ovh/";
    private final String LOG = VenueInDetailFragment.class.getSimpleName();
    private String venueId;
    private View view;

    private ImageView imageVenueOne;
    private ImageView imageVenueTwo;
    private ImageView imageVenueThree;

    private TextView venueName;
    private TextView venueAddress;
    private TextView venueIsOpened;
    private TextView venueWebsite;
    private TextView venueWebsiteLabel;
    private List<tk.internet.praktikum.foursquare.api.bean.Image> images;
    private GoogleMap map;
    private   ProgressDialog progressDialog;

    public static VenueInDetailFragment newInstance(String param1, String param2) {
        VenueInDetailFragment fragment = new VenueInDetailFragment();
        return fragment;
    }
    public VenueInDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_venue_in_detail, container, false);
        imageVenueOne = (ImageView) view.findViewById(R.id.image_venue_one);
        imageVenueTwo = (ImageView) view.findViewById(R.id.image_venue_two);
        imageVenueThree = (ImageView) view.findViewById(R.id.image_venue_three);

        venueName=(TextView) view.findViewById(R.id.venue_name);
        venueAddress=(TextView) view.findViewById(R.id.venue_address);
        venueIsOpened=(TextView) view.findViewById(R.id.venue_is_opened);
        venueWebsite=(TextView) view.findViewById(R.id.venue_website);
        venueWebsiteLabel=(TextView)view.findViewById(R.id.venue_website_label);
        progressDialog= new ProgressDialog(getActivity(), 1);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Waiting for seeing venue details...");
        progressDialog.show();

        SupportMapFragment mapFragment =((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.venueDetails_mapView));
        mapFragment.getMapAsync(this);
        renderContent();

        return view;
    }

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }




    private void renderContent() {
        Log.d(LOG,"##### Venue Id: "+venueId);
        VenueService venueService = ServiceFactory.createRetrofitService(VenueService.class, URL);
        venueService.getDetails(venueId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(venue -> {
                            renderVenueInformation(venue);
                            Location location= venue.getLocation();
                            updateVenueLocation(location);
                            images = venue.getImages();
                            Log.d(LOG,"all images size: " + images.size());
                            if (images.size() > 0) {
                                Log.d(LOG,"++++ get images");
                                Image image = images.get(0);
                                ImageCacheLoader imageCacheLoader = new ImageCacheLoader(this.getContext());
                                imageCacheLoader.loadBitmap(image, ImageSize.LARGE)
                                           .subscribeOn(Schedulers.io())
                                           .observeOn(AndroidSchedulers.mainThread())
                                           .subscribe(bitmap -> {
                                               imageVenueOne.setImageBitmap(bitmap);
                                               imageVenueTwo.setImageBitmap(bitmap);
                                               imageVenueThree.setImageBitmap(bitmap);
                                           });
                            }
                            progressDialog.dismiss();
                        },
                        throwable -> {
                            //TODO
                            //handle exception
                            progressDialog.dismiss();
                           Log.d(LOG,"#### exception"+ throwable.getCause());

                        }
                );
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        //map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setScrollGesturesEnabled(false);
        //map.getUiSettings().setMapToolbarEnabled(true);
    }

    public void updateVenueLocation(Location location){
        LatLng venueLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("venue_location_marker", "mipmap", getContext().getPackageName()));

        map.addMarker(new MarkerOptions()
                .position(venueLocation))
                .setIcon(BitmapDescriptorFactory.fromBitmap(imageBitmap));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(venueLocation,14));


    }

    public void renderVenueInformation(Venue venue){
        this.venueName.setText(venue.getName());
        this.venueAddress.setText(venue.getVicinity());
        this.venueWebsiteLabel.setText(getString(R.string.websiteLabel));
        this.venueWebsite.setText(venue.getWebsite());

        if(venue.is_open())
            this.venueIsOpened.setText(getString(R.string.isOpened));
        else
            this.venueIsOpened.setText(getString(R.string.closed));
    }
}
