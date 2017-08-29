package tk.internet.praktikum.foursquare.abstracts;

import android.support.annotation.LayoutRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import tk.internet.praktikum.foursquare.R;

/**
 * Created by truongtud on 22.06.2017.
 */

public   class DrawerLayoutActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private FrameLayout frameLayout;
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        drawerLayout=(DrawerLayout) getLayoutInflater().inflate(R.layout.activity_main,null);
        frameLayout=(FrameLayout) drawerLayout.findViewById(R.id.fragment_container);
        getLayoutInflater().inflate(layoutResID,frameLayout,true);
        super.setContentView(drawerLayout);
    }
}
