package tk.internet.praktikum.foursquare.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tk.internet.praktikum.foursquare.R;



public class VenueInDetailFragment extends Fragment {


    public VenueInDetailFragment() {
        // Required empty public constructor
    }


    public static VenueInDetailFragment newInstance(String param1, String param2) {
        VenueInDetailFragment fragment = new VenueInDetailFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_venue_in_detail, container, false);
    }




}
