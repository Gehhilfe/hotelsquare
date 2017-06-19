package tk.internet.praktikum.foursquare.search;

import android.app.Fragment;
import android.app.FragmentTransaction;
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


public class FastSearchFragment extends Fragment {
    private  SearchView searchView;
    View view;
    public FastSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=  inflater.inflate(R.layout.fragment_fast_search, container, false);
        //searchView=(SearchView)view.findViewById(R.id.fast_search);
        setHasOptionsMenu(true);

        return view;
    }
    private  void fastSearch(){

        //Intent intent = new Intent(getActivity().getApplicationContext(), DeepSearchFragment.class);
        //startActivityForResult(intent, 1);

        // Todo
        // also gets the suggested value from 9 categories
        Fragment fragment=new DeepSearchFragment();
        FragmentTransaction fragmentTransaction= getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        inflater.inflate(R.menu.search_view, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint(getResources().getString(R.string.searching_question));
        //searchView.clearFocus();
       // searchView.setInputType(InputType.TYPE_NULL);
       // searchView.setOnClickListener(v->fastSearch());


        // toolbar.addView(searchView);
        //searchView.setOnQueryTextListener(this);

         MenuItemCompat.setOnActionExpandListener(item,
               new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Do something when collapsed
                       // adapter.setFilter(mCountryModel);
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        // Do something when expanded
                        fastSearch();
                        return true; // Return true to expand action view
                    }
                });
    }



}
