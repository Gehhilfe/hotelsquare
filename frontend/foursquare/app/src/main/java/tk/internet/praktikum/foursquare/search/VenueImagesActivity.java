package tk.internet.praktikum.foursquare.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Image;
import tk.internet.praktikum.foursquare.api.service.VenueService;

public class VenueImagesActivity extends AppCompatActivity {
    private final String URL = "https://dev.ip.stimi.ovh/";
    private final String LOG = VenueImagesActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private List<Image> images;
    private VenueImageAdapter_2 venueImageAdapter;
    private String venueId;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_images);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Pictures");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView=(RecyclerView) findViewById(R.id.all_venue_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(),2));
        Intent intent = getIntent();
        venueId = intent.getStringExtra("venueID");
        renderImages();
    }
    private void renderImages() {
        VenueService venueService = ServiceFactory.createRetrofitService(VenueService.class, URL);
        venueService.getDetails(venueId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(venue -> {
                       images=venue.getImages();

                        if(images.size()>0){
                            venueImageAdapter = new VenueImageAdapter_2(images);
                            venueImageAdapter.setContext(getApplicationContext());
                            recyclerView.setAdapter(venueImageAdapter);
                         }
                        },
                        throwable -> {


                        }
                );


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }


}
