package tk.internet.praktikum.foursquare.user;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import tk.internet.praktikum.Constants;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ImageCacheLoader;
import tk.internet.praktikum.foursquare.api.ImageSize;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.UploadHelper;
import tk.internet.praktikum.foursquare.api.bean.Gender;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.ProfileService;
import tk.internet.praktikum.foursquare.api.service.UserService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    private static final String LOG  = ProfileFragment.class.getSimpleName();
    private TextView name, email, password, city, age;
    private Button edit, save;
    private RadioButton male, female, none;
    private ImageView avatarPicture;

    private static final String LOG_TAG = ProfileFragment.class.getSimpleName();
    private final String URL = "https://dev.ip.stimi.ovh/";
    private User currentUser;
    private Bitmap avatar;
    private boolean newPicture;

    private final int REQUEST_CAMERA = 0;
    private final int REQUEST_GALLERY = 1;
    private boolean isEdited = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        name = (TextView) view.findViewById(R.id.profile_name);
        email = (TextView) view.findViewById(R.id.profile_email);
        password = (TextView) view.findViewById(R.id.profile_password);
        city = (TextView) view.findViewById(R.id.profile_city);
        age = (TextView) view.findViewById(R.id.profile_age);

        edit = (Button) view.findViewById(R.id.profile_fragment_edit_cancel);
        save = (Button) view.findViewById(R.id.profile_fragment_edit_save);

        male = (RadioButton) view.findViewById(R.id.radio_male);
        female = (RadioButton) view.findViewById(R.id.radio_female);
        none = (RadioButton) view.findViewById(R.id.radio_anonymous);

        avatarPicture = (ImageView) view.findViewById(R.id.profile_avatar);

        avatarPicture.setOnClickListener(v -> uploadPicture());

        edit.setOnClickListener(v -> edit());
        save.setOnClickListener(v -> save());

        age.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    save();
                return true;
            }
        });

        currentUser = null;
        initialiseProfile();
        return view;
    }

    /**
     * Cancel and discard change
     */
    private void cancel() {
        edit.setOnClickListener(v -> edit());
        edit.setText(R.string.profile_edit);

        name.setText(currentUser.getDisplayName());
        email.setText(currentUser.getEmail());
        city.setText(currentUser.getCity());
        age.setText(String.format(Locale.ENGLISH, "%1$d", currentUser.getAge()));
        Gender gender = currentUser.getGender();
        if (gender == Gender.MALE)
            male.setChecked(true);
        else if (gender == Gender.FEMALE)
            female.setChecked(true);
        else
            none.setChecked(true);

        password.setEnabled(false);
        city.setEnabled(false);
        age.setEnabled(false);

        password.clearFocus();
        city.clearFocus();
        age.clearFocus();

        save.setEnabled(false);

        male.setEnabled(false);
        female.setEnabled(false);
        none.setEnabled(false);

        isEdited = false;
    }

    /**
     * Loads data for the given userId data from the server.
     */
    private void initialiseProfile() {
        ProfileService service = ServiceFactory
                .createRetrofitService(ProfileService.class, URL, LocalStorage.
                        getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

        final ProgressDialog progressDialog = new ProgressDialog(getActivity(), 0);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.load_profile));
        progressDialog.show();

        try {
            service.profile()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            user -> {
                                currentUser = user;
                                name.setText(currentUser.getDisplayName());
                                email.setText(currentUser.getEmail());
                                city.setText(currentUser.getCity());
                                age.setText(String.format(Locale.ENGLISH, "%1$d", currentUser.getAge()));
                                Gender gender = currentUser.getGender();
                                if (gender == Gender.MALE)
                                    male.setChecked(true);
                                else if (gender == Gender.FEMALE)
                                    female.setChecked(true);
                                else
                                    none.setChecked(true);

                                // Loads the avatar.
                                if (currentUser.getAvatar() != null) {
                                    ImageCacheLoader imageCacheLoader = new ImageCacheLoader(this.getContext());
                                    imageCacheLoader.loadBitmap(currentUser.getAvatar(), ImageSize.MEDIUM)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(bitmap -> {
                                                avatarPicture.setImageBitmap(bitmap);
                                            }, throwable -> Log.d(LOG_TAG, throwable.getMessage())
                                            );
                                }

                            progressDialog.dismiss();
                            },
                            throwable -> {
                                progressDialog.dismiss();
                                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_user_data), Toast.LENGTH_SHORT).show();
                                Log.d(LOG, throwable.getMessage());
                            }
                    );
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Changes the ui settings, fetches the new user information and calls the upload function.
     */
    private void save() {
        password.setEnabled(false);
        city.setEnabled(false);
        age.setEnabled(false);

        password.clearFocus();
        city.clearFocus();
        age.clearFocus();

        save.setEnabled(false);

        male.setEnabled(false);
        female.setEnabled(false);
        none.setEnabled(false);

        if (password.getText() != "")
            currentUser.setPassword(password.getText().toString());

        currentUser.setCity(city.getText().toString());
        currentUser.setAge(Integer.parseInt(age.getText().toString()));

        if (male.isChecked())
            currentUser.setGender(Gender.MALE);
        else if (female.isChecked())
            currentUser.setGender(Gender.FEMALE);
        else
            currentUser.setGender(Gender.UNSPECIFIED);

        isEdited = true;
        uploadChanges();
        edit.setOnClickListener(v -> edit());
        edit.setText(R.string.profile_edit);
    }

    /**
     * Uploads the new user information.
     */
    private void uploadChanges() {
        if  (newPicture) {
            ProfileService service = ServiceFactory
                    .createRetrofitService(ProfileService.class, URL, LocalStorage.
                            getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

            try {
                MultipartBody.Part img = UploadHelper.createMultipartBodySync(avatar, getContext(), true);
                service.uploadAvatar(img)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(user -> Log.d(LOG_TAG, "AVATAR ID" + user.getAvatar().getId()),
                                throwable -> {
                                    Log.d(LOG, throwable.getMessage());
                                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_upload_user_data), Toast.LENGTH_SHORT).show();
                                }
                        );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (isEdited) {
            UserService service2 = ServiceFactory
                    .createRetrofitService(UserService.class, URL, LocalStorage.
                            getSharedPreferences(getActivity().getApplicationContext()).getString(Constants.TOKEN, ""));

            try {
                service2.update(currentUser)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(user -> {
                                    currentUser = user;
                                },
                                throwable -> {
                                    Toast.makeText(getActivity().getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        isEdited = false;
    }

    /**
     * Setup the ui settings to be editable
     */
    private void edit() {
        password.setFocusable(true);
        city.setFocusable(true);
        age.setFocusable(true);

        password.setEnabled(true);
        city.setEnabled(true);
        age.setEnabled(true);

        save.setEnabled(true);

        male.setEnabled(true);
        female.setEnabled(true);
        none.setEnabled(true);
        newPicture = false;

        edit.setOnClickListener(v -> cancel());
        edit.setText(R.string.profile_cancel);
    }

    /**
     * Starts a selection screen to select an Image from either your camera or your gallery
     */
    private void uploadPicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String[] options = {getString(R.string.image_camera), getString(R.string.image_gallery), getString(R.string.image_cancel)};
        builder.setTitle(getString(R.string.image_title));
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
                case "Kamera":
                    Log.d(LOG_TAG, "Camera");
                    cameraIntent();
                    break;
                case "Abbrechen":
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    /**
     * Starts the camera intent.
     */
    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    /**
     * Starts the gallery intent.
     */
    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Set the image from the camera or the gallery as avatar.
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                    avatar = (Bitmap) data.getExtras().get("data");
                    avatarPicture.setImageBitmap(avatar);
                    newPicture = true;
                }
            break;

            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    try {
                        avatar = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                        avatarPicture.setImageBitmap(avatar);
                        newPicture = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            break;
        }
    }
}
