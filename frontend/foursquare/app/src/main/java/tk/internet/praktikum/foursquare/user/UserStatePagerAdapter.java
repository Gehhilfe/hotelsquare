package tk.internet.praktikum.foursquare.user;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;

import java.util.ArrayList;
import java.util.List;

import tk.internet.praktikum.foursquare.R;

class UserStatePagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentTitleList = new ArrayList<>();
    private Context context;

    UserStatePagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    void addFragment(Fragment fragment, String title) {
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
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_menu_search);
        String title = "";
        // Initialises the tab with their title and logo.
        switch (position) {
            case 0: // profile
                title = " \n" + context.getResources().getString(R.string.user_tab_profile);
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_person_white_24dp);
                break;
            case 1:
                title = " \n" + context.getResources().getString(R.string.user_tab_requests);
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_person_add_white_24dp);
                break;
            case 2: // friends
                title = " \n" + context.getResources().getString(R.string.user_tab_friends);
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_people_white_24dp);
                break;
            case 3: // inbox
                title = " \n" + context.getResources().getString(R.string.user_tab_inbox);
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_chat_white_24dp);
                break;
            default:
            break;
        }

        // Builds up the title for the tabs by placing their title name below their icon.
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
