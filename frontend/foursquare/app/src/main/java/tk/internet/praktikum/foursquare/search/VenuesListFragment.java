package tk.internet.praktikum.foursquare.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.Venue;


public class VenuesListFragment extends Fragment {
    private RecyclerView recyclerView;
    private CardView cardView;
    View view;

    public VenuesListFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_venues_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.searching_results);
        this.setRetainInstance(true);
        return view;
    }
    /**
     * update new venues list
     * @param venues
     */
    protected   void updateRecyclerView(List<Venue> venues){
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new SearchResultAdapter(this,venues));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),LinearLayoutManager.VERTICAL));

    }

}
