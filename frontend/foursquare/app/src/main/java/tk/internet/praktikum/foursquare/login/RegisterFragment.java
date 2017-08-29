package tk.internet.praktikum.foursquare.login;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.UserService;

public class RegisterFragment extends Fragment {

    private static final String LOG_TAG = RegisterFragment.class.getSimpleName();
    private final String URL = "https://dev.ip.stimi.ovh/";
    private EditText nameInput, emailInput, passwordInput;
    private AppCompatButton registerBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        nameInput = (EditText) view.findViewById(R.id.register_name_input);
        emailInput = (EditText) view.findViewById(R.id.user_input);
        passwordInput = (EditText) view.findViewById(R.id.register_password_input);
        registerBtn = (AppCompatButton) view.findViewById(R.id.create_acc_btn);
        TextView loginLbl = (TextView) view.findViewById(R.id.login_link);

        registerBtn.setOnClickListener(v -> register());

        loginLbl.setOnClickListener(v -> ((LoginActivity) getActivity()).changeFragment(0));

        passwordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    register();
                return true;
            }
        });

        return view;
    }

    /**
     * Start the Registration process.
     */
    private void register() {
        if (!validate()) {
            failedRegister();
            return;
        }

        registerBtn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity(), 0);

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.register_dialog));
        progressDialog.show();

        String name = nameInput.getText().toString();
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        UserService service = ServiceFactory.createRetrofitService(UserService.class, URL);

        service.register(new User(name, email, password))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            successfulRegister();
                            progressDialog.dismiss();
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.registration_complete), Toast.LENGTH_LONG).show();
                        },
                        throwable -> {
                            Log.d(LOG_TAG, throwable.getMessage());
                            failedRegister();
                            Toast.makeText(getActivity().getBaseContext(), getString(R.string.registration_failed), Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                );
    }

    /**
     * Validates the entered input.
     * @return True or false depending on if the input is valid.
     */
    private boolean validate() {
        boolean valid = true;

        String name = nameInput.getText().toString();
        String eMail = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (eMail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(eMail).matches()) {
            emailInput.setError(getString(R.string.register_invalid_mail));
            valid = false;
        } else
            emailInput.setError(null);

        if (name.isEmpty() || name.length() < 3) {
            nameInput.setError(getString(R.string.register_invalid_name));
            valid = false;
        } else
            nameInput.setError(null);

        if (password.isEmpty() || password.length() < 6) {
            passwordInput.setError(getString(R.string.register_invalid_password));
            valid = false;
        } else
            passwordInput.setError(null);

        return valid;
    }

    /**
     * Forwards the user to the login fragment.
     */
    private void successfulRegister() {
        registerBtn.setEnabled(true);
       ((LoginActivity) getActivity()).changeFragment(0);
    }

    /**
     * Routine to execute on Failed registration. Logs the login attempt.
     */
    private void failedRegister() {
        Log.d(LOG_TAG, "Failed login.");
        registerBtn.setEnabled(true);
    }
}
