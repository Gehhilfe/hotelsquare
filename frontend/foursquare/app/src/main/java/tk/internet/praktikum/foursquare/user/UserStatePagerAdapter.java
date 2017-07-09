package tk.internet.praktikum.foursquare.user;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;

import java.util.ArrayList;
import java.util.List;

import tk.internet.praktikum.foursquare.R;

public class UserStatePagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentTitleList = new ArrayList<>();
    private Context context;

    public UserStatePagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    public void addFragment(Fragment fragment, String title) {
        fragmentList.add(fragment);
        fragmentTitleList.add(title);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Drawable drawable = context.getResources().getDrawable(R.drawable.ic_menu_search, null);
        String title = "";

        switch (position) {
            case 6:
                // home
            break;
            case 0: // profile
                title = " \n" + " Profile";
                drawable = context.getResources().getDrawable(R.mipmap.user_profile, null);
                //drawable = context.getResources().getDrawable(R.drawable.ic_menu_search, null);
            break;
            case 2: // history

            break;
            case 1: // friends
                title = " \n" + " Friends";
            break;
            case 4: // inbox

            break;
            default:
            break;
        }
        SpannableStringBuilder sb = new SpannableStringBuilder("" + title);

        try {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            ImageSpan imageSpan = new ImageSpan(drawable, DynamicDrawableSpan.ALIGN_BOTTOM);
            sb.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb;
    }
}
