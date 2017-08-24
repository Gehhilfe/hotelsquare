package tk.internet.praktikum.foursquare.search;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;

import java.util.List;

/**
 * Created by truongtud on 24.08.2017.
 */

public class KeyWordsSuggestionAdapter extends CursorAdapter implements Filterable {
    List<String> suggestedKeyWords;

    public KeyWordsSuggestionAdapter(Context context, Cursor cursor, List<String>suggestedKeyWords) {
        super(context, cursor);
        this.suggestedKeyWords=suggestedKeyWords;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
