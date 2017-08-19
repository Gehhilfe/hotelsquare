package tk.internet.praktikum.foursquare.search;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by truongtud on 18.08.2017.
 */

public class VenueViewPager extends ViewPager {
    public VenueViewPager(Context context) {
        super(context);
    }

    public VenueViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return  false;
    }
}
