package tk.internet.praktikum.foursquare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Location;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.service.VenueService;

public class NewVenueDetail extends AppCompatActivity implements OnMapReadyCallback {

    private final String URL = "https://dev.ip.stimi.ovh/";
    private final String LOG = NewVenueDetail.class.getSimpleName();

    private ImageView headerImage;
    private Toolbar headerTitle;
    private ProgressDialog progressDialog;
    private GoogleMap map;
    private Toolbar toolbar;
    private TextView infoVicinity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_venue_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        Intent intent = getIntent();
        String venueId = intent.getStringExtra("VENUE_ID");


        headerImage = (ImageView) findViewById(R.id.header_image);
        headerTitle = (Toolbar) findViewById(R.id.toolbar);

        infoVicinity = (TextView) findViewById(R.id.text_vicinty);

        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView));
        mapFragment.getMapAsync(this);

        progressDialog = new ProgressDialog(this, 0);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Spy is looking up details...");
        progressDialog.show();

        VenueService venueService = ServiceFactory.createRetrofitService(VenueService.class, URL);
        venueService.getDetails(venueId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(venue -> {
                    progressDialog.dismiss();
                    toolbar.setTitle(venue.getName());
                    updateVicinty(venue);
                    updateVenueLocation(venue.getLocation());
                    if (venue.getImages().size() > 0) {
                        ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getApplicationContext());
                        imageCacheLoader.loadBitmap(venue.getImages().get(0), ImageSize.MEDIUM)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(bitmap -> {
                                    headerImage.setImageBitmap(bitmap);
                                });
                    }
                });
    }

    private void updateVicinty(Venue venue) {
        StringBuilder sb = new StringBuilder();
        if(venue.getFormattedAddress() != null && !venue.getFormattedAddress().isEmpty())
            sb.append(venue.getFormattedAddress()).append("\n");
        if(venue.getPhoneNumber() != null && !venue.getPhoneNumber().isEmpty())
            sb.append(venue.getPhoneNumber()).append("\n");
        if(venue.getWebsite() != null && !venue.getWebsite().isEmpty())
            sb.append(venue.getWebsite()).append("\n");
        infoVicinity.setText(sb.toString());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setAllGesturesEnabled(false);
    }

    public void updateVenueLocation(Location location) {
        LatLng venueLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Bitmap imageBitmap = BitmapFactory.
                decodeResource(getResources(), getResources().getIdentifier("venue_location_marker", "mipmap", getApplicationContext().getPackageName()));

        map.addMarker(new MarkerOptions()
                .position(venueLocation))
                .setIcon(BitmapDescriptorFactory.fromBitmap(imageBitmap));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(venueLocation, 14));
    }
}
