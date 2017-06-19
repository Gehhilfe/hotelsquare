package tk.internet.praktikum.foursquare.search;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import tk.internet.praktikum.foursquare.R;


public class DeepSearchFragment extends Fragment {
    private  SearchView searchView;
    private View view;
    public DeepSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         view=  inflater.inflate(R.layout.fragment_deep_search, container, false);
         setHasOptionsMenu(true);
        return view;
    }
    private  void deepSearch(){
       // Searching for

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        inflater.inflate(R.menu.search_view, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(item);



        // toolbar.addView(searchView);
        //searchView.setOnQueryTextListener(this);

        MenuItemCompat.setOnActionExpandListener(item,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Do something when collapsed
                        // adapter.setFilter(mCountryModel);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        // Do something when expanded
                        searchView.setOnClickListener(v->deepSearch());
                        searchView.setQueryHint(getResources().getString(R.string.searching_question));
                        return true; // Return true to expand action view
                    }
                });
    }



}
