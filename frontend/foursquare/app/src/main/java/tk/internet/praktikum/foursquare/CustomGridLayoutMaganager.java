package tk.internet.praktikum.foursquare;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by gehhi on 12.08.2017.
 */

class CustomGridLayoutMaganager extends LinearLayoutManager {
    public CustomGridLayoutMaganager(Context applicationContext) {
        super(applicationContext);
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }
}
