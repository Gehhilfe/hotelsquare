package tk.internet.praktikum.foursquare.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.stream.Collectors;

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
          //Venue venue=searchResultViewHolderList.get(position);
         //System.out.println("render venue position: "+position+ "name: "+venue.getName());
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
        venueInDetailFragment.setParent(parentFragment);
        redirectToFragment(venueInDetailFragment);
    }
    public  void addMoreVenues(List<Venue> venues){
        this.searchResultViewHolderList.addAll(filterVenue(venues));
        this.notifyDataSetChanged();
    }



   private void redirectToFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = parentFragment.getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(fragment.getTag());
        fragmentTransaction.commit();

    }
    public List<Venue> getSearchResultViewHolderList() {
        return searchResultViewHolderList;
    }

    public void setSearchResultViewHolderList(List<Venue> searchResultViewHolderList) {
        this.searchResultViewHolderList = searchResultViewHolderList;
    }

    public List<Venue> filterVenue(List<Venue> venues){
          return venues.parallelStream().filter(venue ->!containVenue(venue)).collect(Collectors.toList());

    }
    public boolean containVenue(Venue venue){
        return  searchResultViewHolderList.parallelStream().filter(v->v.getId().equals(venue.getId())).findFirst().isPresent();
    }

}
