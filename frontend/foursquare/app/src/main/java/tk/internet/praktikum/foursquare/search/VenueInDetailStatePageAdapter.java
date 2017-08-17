package tk.internet.praktikum.foursquare.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by truongtud on 12.08.2017.
 */

public class VenueInDetailStatePageAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> venueInDetailFragments = new ArrayList<>();

    public VenueInDetailStatePageAdapter(FragmentManager fm) {
        super(fm);
    }
    public void addVenuesFragment(Fragment fragment){
        venueInDetailFragments.add(fragment);

    }
    public void initVenuesFragment(){
        VenuesListFragment venuesListFragment=new VenuesListFragment();
        VenuesOnMapFragment venuesOnMapFragment=new VenuesOnMapFragment();
        addVenuesFragment(venuesListFragment);
        addVenuesFragment(venuesOnMapFragment);
    }
    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }
}
