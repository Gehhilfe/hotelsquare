package tk.internet.praktikum.foursquare.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import tk.internet.praktikum.foursquare.R;

public class FullScreenVenueImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_venue_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewPager viewPager=(ViewPager) findViewById(R.id.pager);
        Intent intent=getIntent();
        int position= intent.getIntExtra("position",0);

    }

}
