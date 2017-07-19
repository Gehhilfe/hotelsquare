package tk.internet.praktikum.foursquare.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by truongtud on 09.07.2017.
 */

public class VenueStatePageAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> venuesFragments = new ArrayList<>();

    public VenueStatePageAdapter(FragmentManager fm) {
        super(fm);
    }
    public void addVenuesFragment(Fragment fragment){
        venuesFragments.add(fragment);

    }
    public void initVenuesFragment(){
        VenuesListFragment venuesListFragment=new VenuesListFragment();
        VenuesOnMapFragment venuesOnMapFragment=new VenuesOnMapFragment();
        addVenuesFragment(venuesListFragment);
        addVenuesFragment(venuesOnMapFragment);
    }
    @Override
    public Fragment getItem(int position) {
        return venuesFragments.get(position);
    }

    @Override
    public int getCount() {
        return venuesFragments.size();
    }

}
