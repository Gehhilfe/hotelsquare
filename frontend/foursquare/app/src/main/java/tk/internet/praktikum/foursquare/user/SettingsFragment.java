package tk.internet.praktikum.foursquare.user;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.MainActivity;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.ProfileService;
import tk.internet.praktikum.foursquare.api.service.UserService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

public class SettingsFragment extends Fragment {

    Button deleteProfileButton;
    Spinner selectLanguageSpinner;
    CheckBox incognitoModeCheckBox;
    String TAG = this.getClass().getSimpleName();
    private String URL = "https://dev.ip.stimi.ovh/";
    private User user;
    public SettingsFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        deleteProfileButton = (Button) view.findViewById(R.id.delete_profile);
        selectLanguageSpinner = (Spinner) view.findViewById(R.id.lang_spinner);
        incognitoModeCheckBox = (CheckBox) view.findViewById(R.id.checkBox);


        deleteProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(getContext());
                }
                builder.setTitle("Delete Account")
                        .setMessage("Are you sure you want to delete your Profile?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                try {
                                    ProfileService profileService = ServiceFactory
                                            .createRetrofitService(ProfileService.class, URL, LocalStorage.
                                                    getSharedPreferences(getContext()).getString(Constants.TOKEN, ""));

                                    profileService.delete()
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(user -> {
                                                Log.d(TAG, "User: " + user.getDisplayName() + " was deleted");
                                            }, throwable -> {
                                                Log.d(TAG, "Exception: delete");
                                            });


                                } catch (Exception e) {
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

        List<String> langList = Arrays.asList(getContext().getResources().getStringArray(R.array.languages));
        List<String> localeList=Arrays.asList(getContext().getResources().getStringArray(R.array.locale));

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, langList);
        String currentLocale=LocalStorage.getSharedPreferences(getContext()).getString("LANGUAGE","de");
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        int selectedIndex=0;
        for(int i=0;i<localeList.size();i++){
            if(currentLocale.equals(localeList.get(i))) {
                selectedIndex = i;
                break;
            }
        }
        // attaching data adapter to spinner
        selectLanguageSpinner.setAdapter(dataAdapter);
        selectLanguageSpinner.setSelection(selectedIndex,false);

        selectLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Log.d("HUSSO", "item is: " + item);
                int index= langList.indexOf(item);
                LocalStorage.getLocalStorageInstance(getContext()).setLanguage("LANGUAGE", localeList.get(index));
               // getActivity().getSupportFragmentManager().beginTransaction().remove(getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container)).commit();
                Intent intent=new Intent(getContext(),MainActivity.class);
                getContext().startActivity(intent);
                //getActivity().recreate();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        incognitoModeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String token = LocalStorage.getSharedPreferences(getContext()).getString(Constants.TOKEN, "");
                if (token == "")
                    return;

                try {
                    UserService service = ServiceFactory
                            .createRetrofitService(UserService.class, URL, token);

                    User tmp = new User();
                    if (incognitoModeCheckBox.isChecked()) {
                        tmp.setIncognito(true);
                    } else {
                        tmp.setIncognito(false);
                    }
                    // send to server
                    service.update(tmp).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(user -> {
                                        Log.d(TAG, "Incognito Mode was set");
                                    },
                                    throwable -> {
                                        Log.d(TAG, "Exception: Incognito Mode: true");
                                    }
                            );

                } catch (Exception e) {
                    Log.d(TAG, "Exception: at UserService");
                }

            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


}
