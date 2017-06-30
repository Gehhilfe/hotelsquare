package tk.internet.praktikum.foursquare.user;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import tk.internet.praktikum.foursquare.R;

/**
 * Created by Christian on 22.06.2017.
 */

public class ProfileFragment extends Fragment {
    private TextView name, email, password, city;
    private Button upload, edit, save;
    private RadioButton male, female;
    private SwitchCompat genderSwitch;
    private ToggleButton genderButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        name = (TextView) view.findViewById(R.id.profile_name);
        email = (TextView) view.findViewById(R.id.profile_email);
        password = (TextView) view.findViewById(R.id.profile_password);
        city = (TextView) view.findViewById(R.id.profile_city);

        upload = (Button) view.findViewById(R.id.profile_avatar_upload_btn);
        edit = (Button) view.findViewById(R.id.profile_tmp_edit_btn);
        save = (Button) view.findViewById(R.id.profile_tmp_save_btn);

        male = (RadioButton) view.findViewById(R.id.radioButton);
        female = (RadioButton) view.findViewById(R.id.radioButton2);

        genderSwitch = (SwitchCompat) view.findViewById(R.id.profile_gender);
        genderButton = (ToggleButton) view.findViewById(R.id.toggleButton);

        upload.setOnClickListener(v -> uploadPicture());
        edit.setOnClickListener(v -> edit());
        save.setOnClickListener(v -> save());

        view.clearFocus();
        return view;
    }

    private void uploadPicture() {

    }

    private void save() {
        name.setEnabled(false);
        email.setEnabled(false);
        password.setEnabled(false);
        city.setEnabled(false);

        name.clearFocus();
        email.clearFocus();
        password.clearFocus();
        city.clearFocus();

        upload.setEnabled(false);
        save.setEnabled(false);

        male.setEnabled(false);
        female.setEnabled(false);

        genderSwitch.setEnabled(false);
        genderButton.setEnabled(false);
    }

    private void edit() {
        name.setFocusable(true);
        email.setFocusable(true);
        password.setFocusable(true);
        city.setFocusable(true);

        name.setEnabled(true);
        email.setEnabled(true);
        password.setEnabled(true);
        city.setEnabled(true);

        upload.setEnabled(true);
        save.setEnabled(true);

        male.setEnabled(true);
        female.setEnabled(true);

        genderSwitch.setEnabled(true);
        genderButton.setEnabled(true);
    }
}
