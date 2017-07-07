package tk.internet.praktikum.foursquare.user;


//import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.UserService;
import tk.internet.praktikum.storage.LocalStorage;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    private TextView name, email, password, city;
    private Button upload, edit, save;
    private RadioButton male, female, none;
    private ImageView avatarPicture;

    private static final String LOG_TAG = ProfileFragment.class.getSimpleName();
    private final String URL = "https://dev.ip.stimi.ovh/";
    private User currentUser;
    private Bitmap avatar;

    private final int REQUEST_CAMERA = 0;
    private final int REQUEST_GALLERY = 1;

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
        none = (RadioButton) view.findViewById(R.id.radioButton3);

        avatarPicture = (ImageView) view.findViewById(R.id.profile_avatar);

        upload.setOnClickListener(v -> uploadPicture());
        edit.setOnClickListener(v -> edit());
        save.setOnClickListener(v -> save());

        view.clearFocus();
        initialiseProfile();
        return view;
    }

    private void initialiseProfile() {
        UserService service = ServiceFactory
                .createRetrofitService(UserService.class, URL, LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

        try {
            service.profile(LocalStorage.
                    getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.NAME, ""))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            user -> {
                                currentUser = user;
                                name.setText(currentUser.getDisplayName());
                                email.setText(currentUser.getEmail());
                                city.setText(currentUser.getName());
                            },
                            throwable -> {
                                Toast.makeText(getActivity().getApplicationContext(), "Error fetching user Informations.", Toast.LENGTH_SHORT).show();
                            }
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
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
        none.setEnabled(false);
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
        none.setEnabled(true);
    }

    private void uploadPicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String[] options = {"Camera", "Gallery", "Cancel"};
        builder.setTitle("Select an option to choose your avatar.");
        builder.setItems(options, (dialog, option) -> {
            switch (options[option]) {
                case "Camera":
                    Log.d(LOG_TAG, "Camera");
                    cameraIntent();
                    break;
                case "Gallery":
                    Log.d(LOG_TAG, "gallery");
                    galleryIntent();
                    break;
                case "Cancel":
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                    avatar = (Bitmap) data.getExtras().get("data");
                    avatarPicture.setImageBitmap(avatar);
                }
            break;

            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    try {
                        avatar = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                        avatarPicture.setImageBitmap(avatar);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            break;
        }
    }
}
