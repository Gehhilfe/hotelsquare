package tk.internet.praktikum.foursquare.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.user.ProfileActivity;
import tk.internet.praktikum.foursquare.user.UserActivity;

public class ChatActivity extends AppCompatActivity {
    private ChatFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Chat");

        // Create the ChatFragment and forwards the arguments from the chat activity to the fragment.
        fragment = new ChatFragment();
        Intent intent = getIntent();
        Bundle args = new Bundle();
        String chatId = intent.getStringExtra("chatId");
        String userName = intent.getStringExtra("currentUserName");
        args.putString("chatId", chatId);
        args.putString("currentUserName", userName);
        fragment.setArguments(args);
        addFragment();
    }

    /**
     * Adds the chat activity fragment to the fragment container.
     */
    private void addFragment() {
        getSupportFragmentManager().beginTransaction().add(R.id.chat_activity_container, fragment).commit();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    @Override
    public Intent getParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    /**
     * Sets the intent for the return destination depending on the given parent view.
     * @return Destination intent.
     */
    private Intent getParentActivityIntentImpl() {
        Intent i = null;
        Bundle bundle = getIntent().getExtras();
        String parentActivity = bundle.getString("Parent");

        if (parentActivity != null) {
            if (parentActivity.equals("UserActivity")) {
                i = new Intent(this, UserActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else {
                i = new Intent(this, ProfileActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            }
        }

        return i;
    }
}
