package tk.internet.praktikum.foursquare;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MeFragment extends Fragment {

    TabLayout tabLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=  inflater.inflate(R.layout.fragment_me, container, false);

        //Toolbar toolbar = (Toolbar) view.findViewById(R.id.main_toolbar);
        //view.setSupportActionBar(toolbar);


        tabLayout= (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Home").setIcon(R.mipmap.user_home));
        tabLayout.addTab(tabLayout.newTab().setText("Profile").setIcon(R.mipmap.user_profile));
        tabLayout.addTab(tabLayout.newTab().setText("History").setIcon(R.mipmap.user_history));
        tabLayout.addTab(tabLayout.newTab().setText("Friends").setIcon(R.mipmap.user_friends));
        tabLayout.addTab(tabLayout.newTab().setText("Inbox").setIcon(R.mipmap.user_message));
        return view;
    }


}
