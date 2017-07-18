package tk.internet.praktikum.foursquare.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.Venue;

/**
 * Created by truongtud on 02.07.2017.
 */

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultViewHolder> implements SearchResultAdapterListener{
    List<Venue> searchResultViewHolderList;
    Fragment parentFragment;

    public SearchResultAdapter(Fragment  parentFragment,List<Venue> searchResultViewHolderList) {
        this.parentFragment=parentFragment;
        this.searchResultViewHolderList = searchResultViewHolderList;
    }

    @Override
    public SearchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_searching_result,parent,false);
        SearchResultViewHolder searchResultViewHolder=new SearchResultViewHolder(view,this);
        searchResultViewHolder.setContext(parentFragment.getContext());
        return  searchResultViewHolder;
    }

    @Override
    public void onBindViewHolder(SearchResultViewHolder holder, int position) {
         holder.render(searchResultViewHolderList.get(position));

    }

    @Override
    public int getItemCount() {
        return searchResultViewHolderList.size();
    }

    @Override
    public void clickOnVenue(String venueId) {
        VenueInDetailFragment venueInDetailFragment=new VenueInDetailFragment();
        venueInDetailFragment.setVenueId(searchResultViewHolderList.get(Integer.valueOf(venueId)).getId());
        redirectToFragment(venueInDetailFragment);
    }



   private void redirectToFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = parentFragment.getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(fragment.getTag());
        fragmentTransaction.commit();

    }

}
