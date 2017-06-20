package tk.internet.praktikum.foursquare.friendlist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import tk.internet.praktikum.foursquare.R;

public class DummyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        addFragment();
    }

    public void addFragment() {
        getFragmentManager().beginTransaction().add(R.id.fragment_container, new FriendListFragment()).commit();
    }

}
