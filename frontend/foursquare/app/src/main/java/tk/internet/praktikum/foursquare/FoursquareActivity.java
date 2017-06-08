package tk.internet.praktikum.foursquare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class FoursquareActivity extends AppCompatActivity {

    private static final String LOG_TAG = FoursquareActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foursquare_search);
    }
}
