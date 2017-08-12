package tk.internet.praktikum.foursquare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import tk.internet.praktikum.CommentAdapter;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Location;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.service.VenueService;

public class NewVenueDetail extends AppCompatActivity implements OnMapReadyCallback {

    public static final String URL = "https://dev.ip.stimi.ovh/";
    public final String LOG = NewVenueDetail.class.getSimpleName();

    private ImageView headerImage;
    private ProgressDialog progressDialog;
    private GoogleMap map;
    private Toolbar toolbar;
    private TextView infoVicinity;
    private ImageView image_1;
    private ImageView image_2;
    private ImageView image_3;

    private ImageView[] money;

    private RecyclerView commentRecyclerView;
    private CommentAdapter commentAdapter;

    private NestedScrollView scrollView;

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

        scrollView = (NestedScrollView) findViewById(R.id.scrollView);

        image_1 = (ImageView) findViewById(R.id.image_1);
        image_2 = (ImageView) findViewById(R.id.image_2);
        image_3 = (ImageView) findViewById(R.id.image_3);

        commentRecyclerView = (RecyclerView) findViewById(R.id.comment_recycler_view);

        commentRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));

        commentAdapter = new CommentAdapter(venueId, getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());

        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    Log.i(NewVenueDetail.class.getName(), "BOTTOM SCROLL");
                    commentAdapter.loadMore();
                }
            }
        });

        commentRecyclerView.setNestedScrollingEnabled(false);
        commentRecyclerView.setLayoutManager(mLayoutManager);
        commentRecyclerView.setItemAnimator(new DefaultItemAnimator());
        commentRecyclerView.setAdapter(commentAdapter);

        money = new ImageView[]{
                (ImageView) findViewById(R.id.money_1),
                (ImageView) findViewById(R.id.money_2),
                (ImageView) findViewById(R.id.money_3),
                (ImageView) findViewById(R.id.money_4),
                (ImageView) findViewById(R.id.money_5)
        };

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
                    updatePrice(venue);
                    if (venue.getImages().size() > 0) {
                        ImageCacheLoader imageCacheLoader = new ImageCacheLoader(getApplicationContext());

                        imageCacheLoader.loadBitmap(venue.getImages().get(0), ImageSize.MEDIUM)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(bitmap -> {
                                    headerImage.setImageBitmap(bitmap);
                                });

                        if (venue.getImages().size() > 1)
                            imageCacheLoader.loadBitmap(venue.getImages().get(1), ImageSize.SMALL)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(bitmap -> {
                                        image_1.setImageBitmap(bitmap);
                                    });

                        if (venue.getImages().size() > 2)
                            imageCacheLoader.loadBitmap(venue.getImages().get(2), ImageSize.SMALL)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(bitmap -> {
                                        image_2.setImageBitmap(bitmap);
                                    });

                        if (venue.getImages().size() > 3)
                            imageCacheLoader.loadBitmap(venue.getImages().get(3), ImageSize.SMALL)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(bitmap -> {
                                        image_3.setImageBitmap(bitmap);
                                    });
                    }
                }, err -> {
                    Log.d(NewVenueDetail.class.getName(), err.toString());
                });
    }

    private void updatePrice(Venue venue) {
        switch (venue.getPrice()) {
            default:
            case 0:
                money[0].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                money[1].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                money[2].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                money[3].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                money[4].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                break;

            case 1:
                money[0].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                money[1].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                money[2].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                money[3].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                money[4].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                break;

            case 2:
                money[0].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                money[1].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                money[2].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                money[3].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                money[4].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                break;

            case 3:
                money[0].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                money[1].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                money[2].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                money[3].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                money[4].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                break;

            case 4:
                money[0].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                money[1].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                money[2].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                money[3].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                money[4].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.gray));
                break;

            case 5:
                money[0].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                money[1].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                money[2].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                money[3].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                money[4].setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.black));
                break;
        }
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
