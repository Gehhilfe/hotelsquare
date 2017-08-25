package tk.internet.praktikum.foursquare.login;

//import android.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.storage.LocalStorage;
import tk.internet.praktikum.foursquare.utils.LanguageHelper;

public class LoginActivity extends AppCompatActivity {
    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;

    private RestorePasswordFragment restorePasswordFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = LocalStorage.getSharedPreferences(getApplicationContext());
        String language=sharedPreferences.getString("LANGUAGE","de");
        LanguageHelper.updateResources(this,language);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        addFragment();
    }



    public void addFragment() {
        loginFragment = new LoginFragment();
        Bundle arg = new Bundle();
        arg.putBoolean("UserActivity", getIntent().getBooleanExtra("UserActivity", false));
        loginFragment.setArguments(arg);
        getSupportFragmentManager().beginTransaction().add(R.id.login_layout, loginFragment).commit();
        //getFragmentManager().beginTransaction().add(R.id.login_layout, loginFragment).commit();
    }

    public void changeFragment(int fragmentId) {
        //FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

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
