package tk.internet.praktikum.foursquare;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;


public class DeepSearchFragment extends Fragment {
    private  SearchView searchView;
    public DeepSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=  inflater.inflate(R.layout.fragment_deep_search, container, false);
        searchView=(SearchView)view.findViewById(R.id.deep_search);
        searchView.setQueryHint(getResources().getString(R.string.searching_question));
        searchView.setOnClickListener(v->deepSearch());
        return view;
    }
    private  void deepSearch(){
       // Searching for

    }



}
