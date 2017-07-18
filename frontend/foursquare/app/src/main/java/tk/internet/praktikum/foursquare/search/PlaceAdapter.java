package tk.internet.praktikum.foursquare.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.List;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.bean.Prediction;

/**
 * Created by truongtud on 18.07.2017.
 */

public class PlaceAdapter extends ArrayAdapter<Prediction> {
    private List<Prediction> suggestedPlaces;


    public PlaceAdapter(@NonNull Context context, @NonNull List<Prediction> places) {
        super(context, R.layout.place_row,places);
        suggestedPlaces=places;
    }


    public void setSuggestedPlaces(List<Prediction> suggestedPlaces) {
        this.suggestedPlaces = suggestedPlaces;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Prediction place=getItem(position);
        if(convertView==null)
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.place_row,parent,false);
        TextView placeView=(TextView) convertView.findViewById(R.id.suggested_place);
        placeView.setText(place.getDescription());
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return super.getFilter();
    }
}
