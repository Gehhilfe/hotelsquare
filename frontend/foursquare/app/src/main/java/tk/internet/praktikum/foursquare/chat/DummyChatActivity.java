package tk.internet.praktikum.foursquare.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import tk.internet.praktikum.foursquare.R;

public class DummyChatActivity extends AppCompatActivity {
    private ChatFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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

    public void addFragment() {
        getSupportFragmentManager().beginTransaction().add(R.id.login_layout, fragment).commit();
    }
}
