package tk.internet.praktikum.foursquare.login;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.LoginCredentials;
import tk.internet.praktikum.foursquare.api.bean.User;
import tk.internet.praktikum.foursquare.api.service.SessionService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

public class LoginFragment extends Fragment {
    private static final String LOG_TAG = LoginFragment.class.getSimpleName();
    private final String URL = "https://dev.ip.stimi.ovh/";

    private EditText userInput, passwordInput;
    private AppCompatButton loginBtn;
    private TextView registerLbl, passwordForgottenLbl;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        userInput = (EditText) view.findViewById(R.id.user_input);
        passwordInput = (EditText) view.findViewById(R.id.register_password_input);
        loginBtn = (AppCompatButton) view.findViewById(R.id.login_btn);
        registerLbl = (TextView) view.findViewById(R.id.login_link);
        passwordForgottenLbl = (TextView) view.findViewById(R.id.forgotten_password);

        loginBtn.setOnClickListener(v -> login());
        registerLbl.setOnClickListener(v -> register());
        passwordForgottenLbl.setOnClickListener(v -> restorePassword());

        return view;
    }


    /**
     * Starts the login sequence. For now it only validates the input and displays the Progress dialog for 3 seconds.
     */
    private void login() {
        loginBtn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity(), 0);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Waiting for login...");
        progressDialog.show();

        String email = userInput.getText().toString();
        String password = passwordInput.getText().toString();

        // Get the SessionService from the backend api
        // Url should be stored somewhere as an constant
        SessionService service = ServiceFactory.createRetrofitService(SessionService.class, URL);

        /**
         * Use RxJava to handle a long running backend api call without blocking the application
         * Would be more clean if we used java 1.8 target with Retrolambda
         */
        service.postSession(new LoginCredentials(email, password))
                .subscribeOn(Schedulers.io())                // call is executed i a new thread
                .observeOn(AndroidSchedulers.mainThread())          // response is handled in main thread
                .subscribe(
                        tokenInformation -> {
                            successfulLogin();
                            progressDialog.dismiss();
                            LocalStorage.getLocalStorageInstance(getActivity().getApplicationContext()).saveLoggedinInformation(tokenInformation, new User(email, email));

                        },
                        throwable -> {
                            Toast.makeText(getActivity().getBaseContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                            failedLogin();
                            progressDialog.dismiss();
                        }
                );
    }

    /**
     * Start up the next Activity or Fragment after a successful login. At the moment it just logs
     * the login and finishes the Activity.
     */
    private void successfulLogin() {
        Log.d(LOG_TAG, "Successful login.");
        loginBtn.setEnabled(true);

        if (getArguments().getBoolean("UserActivity"))
            getActivity().setResult(3, null);
        else
            getActivity().setResult(2, null);

        getActivity().finish();
    }

    /**
     * Routine to execute on Failed login. Logs the login attempt and displays a Toast for the user.
     */
    private void failedLogin() {
        Log.d(LOG_TAG, "Failed login.");
        loginBtn.setEnabled(true);
    }

    /**
     * Starts the Register Fragment
     */
    private void register() {
        ((LoginActivity) getActivity()).changeFragment(1);
    }

    // TODO - Restore Password
    private void restorePassword() {
        ((LoginActivity) getActivity()).changeFragment(2);
    }
}
