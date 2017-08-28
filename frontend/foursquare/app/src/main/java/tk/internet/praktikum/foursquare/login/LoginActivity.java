package tk.internet.praktikum.foursquare.login;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.utils.LanguageHelper;

public class LoginActivity extends AppCompatActivity {
    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;

    private RestorePasswordFragment restorePasswordFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        setTitle(getApplicationContext().getResources().getString(R.string.login));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Typeface type = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/Pacifico.ttf");
        TextView hotelsquare = (TextView) findViewById(R.id.login_hotelsquare);
        hotelsquare.setTypeface(type);

        SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getApplicationContext());
        String language=sharedPreferences.getString("LANGUAGE","de");
        LanguageHelper.updateResources(this,language);
        super.onCreate(savedInstanceState);

        addFragment();
    }

    public void addFragment() {
        loginFragment = new LoginFragment();
        Bundle arg = new Bundle();
        arg.putString("Destination", getIntent().getStringExtra("Destination"));
        loginFragment.setArguments(arg);
        getSupportFragmentManager().beginTransaction().add(R.id.login_layout, loginFragment).commit();
    }

    public void changeFragment(int fragmentId) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        switch (fragmentId) {
            case 0:
                fragmentTransaction.replace(R.id.login_layout, loginFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 1:
                if (registerFragment == null)
                    registerFragment = new RegisterFragment();
                fragmentTransaction.replace(R.id.login_layout, registerFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 2:
                if (restorePasswordFragment == null)
                    restorePasswordFragment = new RestorePasswordFragment();
                fragmentTransaction.replace(R.id.login_layout, restorePasswordFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
        }
    }
}
