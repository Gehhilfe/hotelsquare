package tk.internet.praktikum.foursquare.search;

//import android.app.Fragment;

import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Prediction;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.bean.VenueSearchQuery;
import tk.internet.praktikum.foursquare.api.service.PlaceService;
import tk.internet.praktikum.foursquare.api.service.VenueService;
import tk.internet.praktikum.foursquare.location.LocationReader;

//import tk.internet.praktikum.foursquare.api.bean.Location;


public class DeepSearchFragment extends Fragment implements android.support.v7.widget.SearchView.OnQueryTextListener, PlaceSelectionListener {
    private final String URL = "https://dev.ip.stimi.ovh/";
    private final String GOOGLE_PLACE_URL = "https://maps.googleapis.com";
    private final String LOG = DeepSearchFragment.class.getSimpleName();
    private SearchView searchView;
    private VenueStatePageAdapter venueStatePageAdapter;
    private AutoCompleteTextView filterLocation;
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
    private PlaceAdapter placeAdapter;
    private String lastFilterLocation;
    private ProgressDialog progressDialog;
    private String lastQuery;
    private boolean isChangedSearchText = false;
    private boolean submitNewQuery;
    private boolean reachedMaxVenues;

    public DeepSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_deep_search, container, false);
        venuesViewPager = (ViewPager) view.findViewById(R.id.venues_result);
        filterLocation = (AutoCompleteTextView) view.findViewById(R.id.location);

        filterRadius = (SeekBar) view.findViewById(R.id.seekBarRadius);
        filterRadius.setMax(50);
        seekBarView = (TextView) view.findViewById(R.id.seekBarView);

        mapViewButton = (ToggleButton) view.findViewById(R.id.is_map_view);
        mapViewButton.setText(null);
        mapViewButton.setTextOff(null);
        mapViewButton.setTextOn(null);
        isMapView = false;
        mapViewButton.setChecked(true);
        filterLocation.onCommitCompletion(null);

        filterLocation.addTextChangedListener(createTextWatcherLocation());
        filterLocation.setOnItemClickListener(createOnItemClick());
        filterRadius.setOnSeekBarChangeListener(createOnSeekBarChangeListener());
        mapViewButton.setOnClickListener(toggleMapView());
        initVenueStatePageAdapter();
        setHasOptionsMenu(true);
        keyword = getArguments().getString("keyword");
        if (keyword != null && !keyword.trim().isEmpty() && !isChangedSearchText)
            lastQuery = keyword;
        this.setRetainInstance(true);
        currentPage = 0;
        return view;
    }

    public void initVenueStatePageAdapter() {
        venueStatePageAdapter = new VenueStatePageAdapter(getFragmentManager());
        venueStatePageAdapter.initVenuesFragment();
        /*savedView=new ArrayList<View>();
        savedView.add(venueStatePageAdapter.getItem(0).getView());
        savedView.add(venueStatePageAdapter.getItem(1).getView());*/
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
        searchView.setQuery(lastQuery, true);

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
                        //searchView.setQuery(lastQuery,false);
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
        Log.d(LOG, "Action: onQueryTextSubmit");
        venues=new ArrayList<Venue>();
        submitNewQuery=true;
        currentPage=0;
        reachedMaxVenues=false;
        deepSearch();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //deepSearch(newText);
        return true;
    }

    public boolean isReachedMaxVenues() {
        return reachedMaxVenues;
    }

    public void setReachedMaxVenues(boolean reachedMaxVenues) {
        this.reachedMaxVenues = reachedMaxVenues;
    }

    protected void deepSearch() {
        Log.d(LOG, "**** seachView: " + searchView);
        Log.d(LOG, "#### lastQuery: " + lastQuery);

        String query=searchView.getQuery().toString();

        if (query == null || query.trim().isEmpty()) {
            query = lastQuery;
            if (searchView != null)
                searchView.setQuery(lastQuery, true);

        }
        if (query != null && !query.trim().isEmpty()) {
            Log.d(LOG, "*** deepSearch");
            // Searching for
            isChangedSearchText = true;
            lastQuery = query;
            searchView.setQuery(query, false);
            Log.d(LOG, "#### currentQuery: " + query);
            Log.d(LOG,"++++ currentPageQuery: "+currentPage);
            VenueSearchQuery venueSearchQuery;
            if (filterLocation.isClickable() && !filterLocation.getText().toString().equals("Near Me")) {
                venueSearchQuery = new VenueSearchQuery(query, filterLocation.getText().toString().trim());
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
                               if(venues.size()>0) {
                                   if (mapViewButton.isChecked())
                                       displayVenuesList();
                                   else {
                                       //calls map services to display positions
                                       displayVenuesOnMap();
                                   }
                               }
                               else{

                                   if(this.getCurrentPage()>0) {
                                       this.setCurrentPage(getCurrentPage() - 1);
                                       reachedMaxVenues=true;
                                   }
                               }
                                progressDialog.dismiss();

                            },
                            throwable -> {
                                //handle exception
                                Log.d(LOG, throwable.toString());

                            }
                    );
        }
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


            }

            @Override
            public void afterTextChanged(Editable s) {
                String changedLocation = s.toString().trim();
                Log.d(LOG, "last fitler location: " + lastFilterLocation);
                Log.d(LOG, "changed location: " + changedLocation);
                if (!changedLocation.equals(lastFilterLocation)) {
                    lastFilterLocation = changedLocation;
                    PlaceService placeService = ServiceFactory.createRetrofitService(PlaceService.class, GOOGLE_PLACE_URL);
                    placeService.getSuggestedPlaces(changedLocation, "geocode", getString(R.string.google_maps_key).trim())
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(placeAutoComplete -> {
                                        Log.d(LOG, "status: " + placeAutoComplete.getStatus());
                                        Log.d(LOG, "predictions: " + placeAutoComplete.getPredictions());
                                        List<Prediction> predictions = placeAutoComplete.getPredictions();
                                        if (placeAdapter == null || predictions.size() > 0) {
                                            Log.d(LOG, "#### created place adapter");
                                            placeAdapter = new PlaceAdapter(getContext(), predictions);
                                            filterLocation.setAdapter(placeAdapter);
                                            placeAdapter.notifyDataSetChanged();
                                        }

                                    },
                                    throwable -> {
                                        Log.d(LOG, "exception");

                                    });
                }
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
                // deepSearch(searchView.getQuery().toString().trim());
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
                    if (mapViewButton.isChecked()) {
                        // change toggle button to list view
                        // isMapView = false;
                        displayVenuesList();
                    } else {
                        //TODO
                        // change toggle button to map view
                        // isMapView = true;
                        displayVenuesOnMap();

                    }
                }
            }
        };

    }

    private void displayVenuesList() {
            Log.d(LOG, "1.*** displayVenuesList");
            VenuesListFragment venuesListFragment = (VenuesListFragment) venueStatePageAdapter.getItem(0);
            // handle back from venue in detail
            if (venuesListFragment.getView() == null) {
                initVenueStatePageAdapter();
                venuesListFragment = (VenuesListFragment) venueStatePageAdapter.getItem(0);
            }
      /*  if(submitNewQuery){
            venuesListFragment.getSearchResultAdapter().setSearchResultViewHolderList(new ArrayList<Venue>());
        }*/
            venuesListFragment.setParent(this);

            venuesListFragment.updateRecyclerView(venues);
            venuesViewPager.setCurrentItem(0);

    }

    public boolean isSubmitNewQuery() {
        return submitNewQuery;
    }

    public void setSubmitNewQuery(boolean submitNewQuery) {
        this.submitNewQuery = submitNewQuery;
    }

    private void displayVenuesOnMap() {
        Log.d(LOG, "2.*** displayVenuesOnMap");
        VenuesOnMapFragment venuesOnMapFragment = (VenuesOnMapFragment) venueStatePageAdapter.getItem(1);
        // handle back from venue in detail
        if (venuesOnMapFragment.getView() == null) {
            initVenueStatePageAdapter();
            venuesOnMapFragment = (VenuesOnMapFragment) venueStatePageAdapter.getItem(1);
        }

        venuesOnMapFragment.updateVenuesMarker(venues);
        //venuesOnMapFragment.updateRecyclerView(venues);
        venuesViewPager.setCurrentItem(1);
    }


    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    @Override

    public void onPlaceSelected(Place place) {

    }

    @Override
    public void onError(Status status) {

    }

    public AdapterView.OnItemClickListener createOnItemClick() {
        return
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(LOG, " ******* onItemclick ******");
                        String query = searchView.getQuery().toString();
                        if (!query.isEmpty()) {
                            venues=new ArrayList<Venue>();
                           // submitNewQuery=true;
                            currentPage=0;
                            reachedMaxVenues=false;
                            progressDialog = new ProgressDialog(getActivity(), 1);
                            progressDialog.setIndeterminate(true);
                            progressDialog.setMessage("Waiting for searching...");
                            progressDialog.show();
                            Prediction place = (Prediction) parent.getItemAtPosition(position);
                            filterLocation.setText(place.getDescription());
                            filterLocation.setSelection(filterLocation.getText().length());
                            deepSearch();
                        }

                    }
                };
    }


}
