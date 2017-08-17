package tk.internet.praktikum.foursquare.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.Venue;


public class VenuesListFragment extends Fragment {
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private SearchResultAdapter searchResultAdapter;
    private int currentPage;
    private int visibleItemCount;
    private int itemCount;
    private int lastVisibleItemPosition;
    private int firstVisibleItem;
    private int currentVisibleItem;
    private int maxLastVisibleItemPosition=0;
    private DeepSearchFragment parent;
    View view;

    public DeepSearchFragment getParent() {
        return parent;
    }

    public void setParent(DeepSearchFragment parent) {
        this.parent = parent;
    }




    public VenuesListFragment() {

        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_venues_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.searching_results);
        this.setRetainInstance(true);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        firstVisibleItem=linearLayoutManager.findFirstVisibleItemPosition();
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewOnScrollListener();
    }

    public SearchResultAdapter getSearchResultAdapter() {
        return searchResultAdapter;
    }

    public void setSearchResultAdapter(SearchResultAdapter searchResultAdapter) {
        this.searchResultAdapter = searchResultAdapter;
    }

    /**
     * update new venues list
     *
     * @param venues
     */
    protected void updateRecyclerView(List<Venue> venues) {
        if(parent.getCurrentPage()==0){
            searchResultAdapter=new SearchResultAdapter(this, venues);
            recyclerView.setAdapter(searchResultAdapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        }
        else{

           searchResultAdapter.addMoreVenues(venues);
        }

        for(Venue venue:venues){
            System.out.println(venue.getName());
        }
        System.out.println("**** all venues:");
        for(Venue venue:searchResultAdapter.getSearchResultViewHolderList()){
            System.out.println(venue.getName());
        }


    }


    public void recyclerViewOnScrollListener() {
        recyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = linearLayoutManager.getChildCount();
                itemCount = linearLayoutManager.getItemCount();
                lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                currentVisibleItem=linearLayoutManager.findFirstVisibleItemPosition();
                System.out.println("dy: "+dy);
                System.out.println("lastVisibleItemPosition "+lastVisibleItemPosition);
                System.out.println("visibleItemCount "+visibleItemCount);
                System.out.println("itemCount: "+itemCount);
                if(dy>0&&(lastVisibleItemPosition+visibleItemCount)>=itemCount &&lastVisibleItemPosition%10==9 && !parent.isReachedMaxVenues() ){
                    parent.setSubmitNewQuery(false);
                    System.out.println("**** get next page");
                    System.out.println("parentFragment: "+parent);
                    currentPage=parent.getCurrentPage()+1;
                    parent.setCurrentPage(currentPage);
                    System.out.println("current Page: "+parent.getCurrentPage());
                    maxLastVisibleItemPosition=Math.max(maxLastVisibleItemPosition,lastVisibleItemPosition);
                    parent.deepSearch();
                }
            }
        });
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }


}
