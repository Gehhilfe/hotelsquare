package tk.internet.praktikum.foursquare.login;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.R;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.LoginCredentials;
import tk.internet.praktikum.foursquare.api.service.SessionService;
import tk.internet.praktikum.foursquare.storage.LocalStorage;

public class LoginFragment extends Fragment {
    private static final String LOG_TAG = LoginFragment.class.getSimpleName();
    private final String URL = "https://dev.ip.stimi.ovh/";

    private EditText userInput, passwordInput;
    private AppCompatButton loginBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        userInput = (EditText) view.findViewById(R.id.user_input);
        passwordInput = (EditText) view.findViewById(R.id.register_password_input);
        loginBtn = (AppCompatButton) view.findViewById(R.id.login_btn);
        TextView registerLbl = (TextView) view.findViewById(R.id.login_link);
        TextView passwordForgottenLbl = (TextView) view.findViewById(R.id.forgotten_password);

        loginBtn.setOnClickListener(v -> login());
        registerLbl.setOnClickListener(v -> register());
        passwordForgottenLbl.setOnClickListener(v -> restorePassword());

        passwordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    login();
                return true;
            }
        });
        return view;
    }

    /**
     * Starts the login sequence and saves the returned web token.
     */
    private void login() {
        loginBtn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity(), 0);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.login_dialog));
        progressDialog.show();

        String email = userInput.getText().toString();
        String password = passwordInput.getText().toString();

        SessionService service = ServiceFactory.createRetrofitService(SessionService.class, URL);

        service.postSession(new LoginCredentials(email, password))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        tokenInformation -> {
                            successfulLogin();
                            progressDialog.dismiss();
                            LocalStorage.getLocalStorageInstance(getActivity().getApplicationContext()).saveLoggedinInformation(tokenInformation, tokenInformation.getUser());

                        },
                        throwable -> {
                            Toast.makeText(getActivity().getBaseContext(), getString(R.string.wrong_login_info), Toast.LENGTH_LONG).show();
                            Log.d(LOG_TAG, throwable.getMessage());
                            loginBtn.setEnabled(true);
                            progressDialog.dismiss();
                        }
                );
    }

    /**
     * Forwards the user to its desired destination by setting the suitable result code.
     */
    private void successfulLogin() {
        loginBtn.setEnabled(true);
        String destination = getArguments().getString("Destination");

        if (Objects.equals(destination, "FastSearch"))
            getActivity().setResult(2, null);
        else if (Objects.equals(destination, "PersonSearch"))
            getActivity().setResult(3, null);
        else if (Objects.equals(destination, "History"))
            getActivity().setResult(4, null);
        else if (Objects.equals(destination, "MyProfile"))
            getActivity().setResult(5, null);

        getActivity().finish();
    }

    /**
     * Forwards the user to the register fragment.
     */
    private void register() {
        ((LoginActivity) getActivity()).changeFragment(1);
    }

    /**
     * Forwards the user to the restore password fragment.
     */
    private void restorePassword() {
        ((LoginActivity) getActivity()).changeFragment(2);
    }
}
