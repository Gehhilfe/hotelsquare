package tk.internet.praktikum.foursquare.search;

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

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultViewHolder> {
    List<Venue> searchResultViewHolderList;

    public SearchResultAdapter(List<Venue> searchResultViewHolderList) {
        this.searchResultViewHolderList = searchResultViewHolderList;
    }

    @Override
    public SearchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_searching_result,parent,false);
        return  new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchResultViewHolder holder, int position) {
         holder.render(searchResultViewHolderList.get(position));
    }

    @Override
    public int getItemCount() {
        return searchResultViewHolderList.size();
    }
}
