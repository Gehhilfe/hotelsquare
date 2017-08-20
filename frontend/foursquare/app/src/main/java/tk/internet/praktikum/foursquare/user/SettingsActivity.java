package tk.internet.praktikum.foursquare.user;


import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.ProfileService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Button deleteProfileButton;
    Spinner selectLanguageSpinner;
    CheckBox incognitoModeCheckBox;
    String TAG = this.getClass().getSimpleName();
    private String URL = "https://dev.ip.stimi.ovh/";
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        deleteProfileButton = (Button) findViewById(R.id.delete_profile);
        selectLanguageSpinner = (Spinner) findViewById(R.id.lang_spinner);
        incognitoModeCheckBox = (CheckBox) findViewById(R.id.checkBox);


        deleteProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(SettingsActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(SettingsActivity.this);
                }
                builder.setTitle("Delete Account")
                        .setMessage("Are you sure you want to delete your Profile?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                try{
                                    ProfileService profileService = ServiceFactory
                                            .createRetrofitService(ProfileService.class, URL, LocalStorage.
                                                    getSharedPreferences(getApplicationContext()).getString(Constants.TOKEN, ""));

                                    profileService.delete()
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(user -> {
                                                Log.d(TAG, "User: " + user.getDisplayName() + " was deleted");
                                            }, throwable -> {
                                                Log.d(TAG, "Exception: delete");
                                            });


                                } catch (Exception e){
                                    Log.d(TAG, "Exception: deleteProfileButton:onClick");
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        List<String> langList = new ArrayList<String>();
        langList.add("English");
        langList.add("Deutsch");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, langList);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        selectLanguageSpinner.setAdapter(dataAdapter);

        selectLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Log.d("HUSSO", "item is: " + item);
                LocalStorage.getLocalStorageInstance(getApplicationContext()).setLanguage("LANGUAGE", item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        incognitoModeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(incognitoModeCheckBox.isChecked()){

                    try{
                        ProfileService profileService = ServiceFactory
                                .createRetrofitService(ProfileService.class, URL, LocalStorage.
                                        getSharedPreferences(getApplicationContext()).getString(Constants.TOKEN, ""));

                        profileService.delete()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(user -> {
                                    Log.d(TAG, "User: " + user.getDisplayName() + " was deleted");
                                }, throwable -> {
                                    Log.d(TAG, "Exception: delete");
                                });


                    } catch (Exception e){
                        Log.d(TAG, "Exception: deleteProfileButton:onClick");
                    }

                }
            }
        });
        setTitle("Settings");


        // TODO - initialise fragment
        //addFragment();
    }

   // public void addFragment() {
        //getSupportFragmentManager().beginTransaction().add(R.id.settings_activity_container, fragment).commit();
   // }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_search:
                setResult(0, null);
                finish();
                break;
            case R.id.nav_search_person:
                setResult(1, null);
                finish();
                break;
            case R.id.nav_history:
                setResult(2, null);
                finish();
                break;
            case R.id.nav_me:
                setResult(3, null);
                finish();
                break;
            case R.id.nav_manage:
                setResult(4, null);
                finish();
                break;
            case R.id.nav_login_logout:
                setResult(5, null);
                finish();
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
