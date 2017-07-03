package tk.internet.praktikum.foursquare.search;

import android.app.Fragment;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.bean.VenueSearchQuery;
import tk.internet.praktikum.foursquare.location.LocationReader;


public class DeepSearchFragment extends Fragment implements android.support.v7.widget.SearchView.OnQueryTextListener {
    private SearchView searchView;
    private RecyclerView recyclerView;
    private EditText filterLocation;
    private SeekBar filterRadius;
    private List<?>optionalFilters;
    private List<Venue> venues;
    private SearchResultAdapter searchResultAdapter;
    private View view;

    public DeepSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_deep_search, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.searching_results);

        filterLocation=(EditText) view.findViewById(R.id.location);
        filterRadius=(SeekBar) view.findViewById(R.id.seekBarRadius);
        filterLocation.setOnClickListener(null);
        filterLocation.onCommitCompletion(null);

        filterRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        inflater.inflate(R.menu.search_view, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(item);
        //searchView.setOnClickListener(v -> deepSearch(searchView.getQuery().toString()));
        searchView.setOnSearchClickListener(v -> deepSearch(searchView.getQuery().toString()));
        MenuItemCompat.setOnActionExpandListener(item,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Do something when collapsed

                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        // Do something when expanded
                        searchView.onActionViewExpanded();
                       // searchView.setOnClickListener(v -> deepSearch(searchView.getQuery().toString()));

                       // System.out.println("I am here");
                        searchView.setQueryHint(getResources().getString(R.string.searching_question));
                        return true; // Return true to expand action view
                    }
                });
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchResultAdapter= new SearchResultAdapter(venues);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
       // System.out.println("I am here");
        // Searching for
        Location currentLocation= LocationReader.getLocationReader(getContext()).getCurrentLocation(LocationManager.GPS_PROVIDER);
        Toast.makeText(getActivity().getApplicationContext(), currentLocation.toString(), Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
    private void deepSearch(String query) {
        System.out.println("I am here");
        // Searching for
        //Location currentLocation= LocationReader.getLocationReader(getContext()).getCurrentLocation(LocationManager.GPS_PROVIDER);
       // Toast.makeText(getActivity().getApplicationContext(), currentLocation.toString(), Toast.LENGTH_LONG).show();

        VenueSearchQuery venueSearchQuery=new VenueSearchQuery(query,0.0,0.0);
        venueSearchQuery.setRadius(filterRadius.getProgress()) ;

    }



}
