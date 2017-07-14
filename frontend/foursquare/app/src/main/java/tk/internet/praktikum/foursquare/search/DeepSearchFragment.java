package tk.internet.praktikum.foursquare.search;

//import android.app.Fragment;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.bean.VenueSearchQuery;
import tk.internet.praktikum.foursquare.api.service.VenueService;
import tk.internet.praktikum.foursquare.location.LocationReader;

//import tk.internet.praktikum.foursquare.api.bean.Location;


public class DeepSearchFragment extends Fragment implements android.support.v7.widget.SearchView.OnQueryTextListener {
    private final String URL = "https://dev.ip.stimi.ovh/";
    private final String LOG = DeepSearchFragment.class.getSimpleName();
    private SearchView searchView;
    private VenueStatePageAdapter venueStatePageAdapter;
    private EditText filterLocation;
    private ToggleButton mapViewButton;
    private SeekBar filterRadius;
    private TextView seekBarView;
    private List<?> optionalFilters;
    private List<Venue> venues;
    private View view;
    private boolean isNearMe;
    private boolean isMapView;
    private ViewPager venuesViewPager;
    private VenuesListFragment venuesListFragment = null;
    private String keyword;
    private int currentPage;
    public DeepSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_deep_search, container, false);
        venuesViewPager = (ViewPager) view.findViewById(R.id.venues_result);
        filterLocation = (EditText) view.findViewById(R.id.location);

        filterRadius = (SeekBar) view.findViewById(R.id.seekBarRadius);
        filterRadius.setMax(50);
        seekBarView = (TextView) view.findViewById(R.id.seekBarView);

        mapViewButton = (ToggleButton) view.findViewById(R.id.is_map_view);
        isMapView = false;
        mapViewButton.setChecked(true);
        filterLocation.onCommitCompletion(null);
        filterLocation.addTextChangedListener(createTextWatcherLocation());
        filterRadius.setOnSeekBarChangeListener(createOnSeekBarChangeListener());
        mapViewButton.setOnClickListener(toggleMapView());
        initVenueStatePageAdapter();
        setHasOptionsMenu(true);
        keyword=getArguments().getString("keyword");
        return view;
    }

    public void initVenueStatePageAdapter() {
        venueStatePageAdapter = new VenueStatePageAdapter(getFragmentManager());
        venueStatePageAdapter.initVenuesFragment();
        venuesViewPager.setAdapter(venueStatePageAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        inflater.inflate(R.menu.search_view, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        MenuItemCompat.expandActionView(item);
        searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        searchView.onActionViewExpanded();
        searchView.requestFocus();
        searchView.clearFocus();
        searchView.setQuery(keyword,true);
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
                        //TODO
                        // displays the recommendation list
                        searchView.onActionViewExpanded();
                        searchView.setQueryHint(getResources().getString(R.string.searching_question));
                        return true; // Return true to expand action view
                    }
                });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        deepSearch(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        deepSearch(newText);
        return true;
    }

    private void deepSearch(String query) {
        Log.d(LOG, "I am here");
        // Searching for
        currentPage=1;
        VenueSearchQuery venueSearchQuery;
        if (filterLocation.isClickable() && !filterLocation.getText().toString().equals("Near Me")) {
            venueSearchQuery = new VenueSearchQuery(query, filterLocation.getText().toString());
        } else {
            // TODO
            // gets current location based on gps; "Near me"
            Location currentLocation = LocationReader.getLocationReader(getContext()).getCurrentLocation(LocationManager.GPS_PROVIDER);
            //Toast.makeText(getActivity().getApplicationContext(), currentLocation.toString(), Toast.LENGTH_LONG).show();
            Log.d(LOG, "current location: long- " + currentLocation.getLongitude() + "lat- " + currentLocation.getLatitude());
            //venueSearchQuery = new VenueSearchQuery(query, dummyLocation().getLongitude(), dummyLocation().getLatitude());
            venueSearchQuery = new VenueSearchQuery(query, currentLocation.getLongitude(), currentLocation.getLatitude());
        }
        venueSearchQuery.setRadius(filterRadius.getProgress());
        // TODO
        // Add more optional filters later

        VenueService venueService = ServiceFactory.createRetrofitService(VenueService.class, URL);
        venueService.queryVenue(venueSearchQuery,currentPage).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(venueSearchResult -> {
                            venueSearchResult.getResults()
                                    .forEach(e ->
                                            {
                                                Log.d(LOG, e.getName());
                                                Log.d(LOG, "long: " + e.getLocation().getLongitude() + "lat: " + e.getLocation().getLatitude());
                                            }
                                    );
                            venues = venueSearchResult.getResults();
                            if (!isMapView)
                                displayVenuesList();
                            else {
                                //TODO
                                //calls map services to display positions
                                displayVenuesOnMap();
                            }
                        },
                        throwable -> {
                            //TODO
                            //handle exception

                        }
                );
    }

    /**
     * listens the changes of location
     *
     * @return
     */
    public TextWatcher createTextWatcherLocation() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<tk.internet.praktikum.foursquare.api.bean.Location> suggestionLocations= suggestionLocations(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                String location=filterLocation.getText().toString().trim();
                if(!location.isEmpty() && location.length()>=3)
                    deepSearch(searchView.getQuery().toString().trim());
            }
        };

    }

    /**
     * listens the changes of seekbar
     *
     * @return
     */
    public SeekBar.OnSeekBarChangeListener createOnSeekBarChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                deepSearch(searchView.getQuery().toString().trim());
            }
        };
    }


    /**
     * dummy location for testing "near me"
     *
     * @return
     */
    /*public Location dummyLocation() {
        // Luisen Darmstadt
        return new Location(8.6511929, 49.8728253);
    }*/
    public View.OnClickListener toggleMapView() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (venues != null && venues.size() > 0) {
                    if (isMapView) {
                        //TODO
                        // change toggle button to list view
                        isMapView = false;
                        displayVenuesList();
                    } else {
                        //TODO
                        // change toggle button to map view
                        isMapView = true;
                        displayVenuesOnMap();

                    }
                }
            }
        };

    }

    private void displayVenuesList() {
        VenuesListFragment venuesListFragment = (VenuesListFragment) venueStatePageAdapter.getItem(0);
        venuesListFragment.updateRecyclerView(venues);
        venuesViewPager.setCurrentItem(0);


    }

    private void displayVenuesOnMap() {
        VenuesOnMapFragment venuesOnMapFragment = (VenuesOnMapFragment) venueStatePageAdapter.getItem(1);
        venuesOnMapFragment.updateVenuesMarker(venues);
        venuesOnMapFragment.updateRecyclerView(venues);
        venuesViewPager.setCurrentItem(1);
    }


    public List<tk.internet.praktikum.foursquare.api.bean.Location> suggestionLocations(String currentTextLocation){
        // TODO
        // use google location services to obtain the appropriate location list upon tipped current location

        return null;
    }
}
