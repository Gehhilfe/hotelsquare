package tk.internet.praktikum.foursquare.search;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.VenueInDetailsNestedScrollView;
import tk.internet.praktikum.foursquare.api.bean.Venue;

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
        Intent intent = new Intent(parentFragment.getActivity(), VenueInDetailsNestedScrollView.class);
        intent.putExtra("VENUE_ID", searchResultViewHolderList.get(Integer.valueOf(venueId)).getId());
        parentFragment.getActivity().startActivity(intent);
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

   /* public List<Venue> filterVenue(List<Venue> venues){
          return venues.parallelStream().filter(venue ->!containVenue(venue)).collect(Collectors.toList());
    }*/
   /* public boolean containVenue(Venue venue){
        return  searchResultViewHolderList.parallelStream().filter(v->v.getId().equals(venue.getId())).findFirst().isPresent();
    }*/

    public List<Venue> filterVenue(List<Venue> venues){
        List<Venue> newVenues=new ArrayList<>();
          for(Venue venue:venues){
              if(!containVenue(venue))
                  newVenues.add(venue);
          }
          return newVenues;
    }
    public boolean containVenue(Venue venue){
        for(Venue v: searchResultViewHolderList){
            if(v.getId().equals(venue.getId())||(v.getName().equals(venue.getName()) &&v.getLocation().equals(venue.getLocation())))
                return true;
        }
        return  false;
    }

}
