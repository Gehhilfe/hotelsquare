package tk.internet.praktikum.foursquare.search;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.Location;
import tk.internet.praktikum.foursquare.api.bean.Venue;
import tk.internet.praktikum.foursquare.api.bean.VenueSearchQuery;
import tk.internet.praktikum.foursquare.api.service.VenueService;


public class DeepSearchFragment extends Fragment implements android.support.v7.widget.SearchView.OnQueryTextListener {
    private final String URL = "https://dev.ip.stimi.ovh/";
    private final String LOG=DeepSearchFragment.class.getSimpleName();
    private SearchView searchView;
    private RecyclerView recyclerView;
    private EditText filterLocation;
    private SeekBar filterRadius;
    private TextView seekBarView;
    private List<?> optionalFilters;
    private List<Venue> venues;
    private SearchResultAdapter searchResultAdapter;
    private View view;
    private boolean isNearMe;

    public DeepSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_deep_search, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.searching_results);

        filterLocation = (EditText) view.findViewById(R.id.location);
        filterRadius = (SeekBar) view.findViewById(R.id.seekBarRadius);
        seekBarView=(TextView)view.findViewById(R.id.seekBarView);
        filterLocation.onCommitCompletion(null);
        filterLocation.addTextChangedListener(createTextWatcherLocation());
        filterRadius.setOnSeekBarChangeListener(createOnSeekBarChangeListener());

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
        //searchView.setOnSearchClickListener(v -> deepSearch(searchView.getQuery().toString()));
        searchView.setOnQueryTextListener(this);
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
        searchResultAdapter = new SearchResultAdapter(venues);
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
         Log.d(LOG,"I am here");
       // System.out.println("I am here");
        // Searching for
        VenueSearchQuery venueSearchQuery;
        if (filterLocation.isClickable() && !filterLocation.getText().toString().equals("Near Me")) {
            venueSearchQuery = new VenueSearchQuery(query, filterLocation.getText().toString());
        } else {
            // TODO
            // gets current location based on gps; "Near me"
            //Location currentLocation= LocationReader.getLocationReader(getContext()).getCurrentLocation(LocationManager.GPS_PROVIDER);
            // Toast.makeText(getActivity().getApplicationContext(), currentLocation.toString(), Toast.LENGTH_LONG).show();
            venueSearchQuery = new VenueSearchQuery(query, dummyLocation().getLongitude(), dummyLocation().getLatitude());
        }
        venueSearchQuery.setRadius(filterRadius.getProgress());
        // TODO
        // Add more optional filters later

        VenueService venueService = ServiceFactory.createRetrofitService(VenueService.class, URL);
        venueService.queryVenue(venueSearchQuery).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(venueSearchResult -> {
                            venueSearchResult.getResults()
                                    .forEach(e->
                                    {
                                        Log.d(LOG,e.getName());
                                        Log.d(LOG,"long: "+e.getLocation().getLongitude()+ "lat: "+e.getLocation().getLatitude());

                                    }
                            );
                            venues = venueSearchResult.getResults();

                            updateRecyclerView(venues);
                        },
                        throwable -> {
                            //TODO
                            //handle exception

                        }
                );
    }

    /**
     * listens the changes of location
     * @return
     */
    public TextWatcher createTextWatcherLocation(){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                deepSearch(searchView.getQuery().toString().trim());
            }
        };

    }

    /**
     * listens the changes of seekbar
     * @return
     */
    public SeekBar.OnSeekBarChangeListener createOnSeekBarChangeListener(){
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
     * update new venues list
     * @param venues
     */
    private  void updateRecyclerView(List<Venue> venues){
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new SearchResultAdapter(venues));
    }

    /**
     * dummy location for testing "near me"
     * @return
     */
    public Location dummyLocation() {
        // Luisen Darmstadt
        return new Location(8.6511929, 49.8728253);
    }


}
