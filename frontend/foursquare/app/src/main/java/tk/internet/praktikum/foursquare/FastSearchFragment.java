package tk.internet.praktikum.foursquare;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;


public class FastSearchFragment extends Fragment {
    private  SearchView searchView;
    public FastSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=  inflater.inflate(R.layout.fragment_fast_search, container, false);
        searchView=(SearchView)view.findViewById(R.id.fast_search);
        searchView.setQueryHint(getResources().getString(R.string.searching_question));
        searchView.clearFocus();
        searchView.setInputType(InputType.TYPE_NULL);
        searchView.setOnClickListener(v->fastSearch());

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



}
