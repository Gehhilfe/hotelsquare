package tk.internet.praktikum.foursquare.search;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

import tk.internet.praktikum.foursquare.R;


public class FastSearchFragment extends Fragment {
    private  SearchView searchView;
    private  String PREFIX_SUGGESTION="suggestion_";
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
        for (int i=1;i<10;i++){
            String buttonId=PREFIX_SUGGESTION+i;
            Button button = (Button) view.findViewById(getResources().getIdentifier(buttonId,"id",getActivity().getPackageName().toString()));
            button.setOnClickListener(v->deepSearch(button.getText().toString()));
        }
        return view;
    }
    private  void deepSearch(String keyWord){


        // also gets the suggested value from 9 categories
        Fragment fragment=new DeepSearchFragment();
        Bundle bundle=new Bundle();
        bundle.putString("keyword",keyWord);
        fragment.setArguments(bundle);
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
                        deepSearch("");
                        return true; // Return true to expand action view
                    }
                });
    }



}
