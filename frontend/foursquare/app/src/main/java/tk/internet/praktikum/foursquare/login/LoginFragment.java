package tk.internet.praktikum.foursquare.login;

/*import android.app.Fragment;
import android.app.FragmentTransaction;*/

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
import tk.internet.praktikum.foursquare.user.MeFragment;

public class LoginFragment extends Fragment {
    private static final String LOG_TAG = LoginFragment.class.getSimpleName();
    private final String URL = "https://dev.ip.stimi.ovh/";

    private EditText userInput, passwordInput;
    private AppCompatButton loginBtn;
    private TextView registerLbl, passwordForgottenLbl;
    private  LoginGeneralFragment loginGeneralFragment;


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
                .subscribeOn(Schedulers.newThread())                // call is executed i a new thread
                .observeOn(AndroidSchedulers.mainThread())          // response is handled in main thread
                .subscribe(
                        tokenInformation -> {
                            //UserService uservice = ServiceFactory.createRetrofitService(UserService.class, URL, tokenInformation.getToken());
                            successfulLogin();
                            progressDialog.dismiss();
                            //Toast.makeText(getActivity().getApplicationContext(), tokenInformation.getToken(), Toast.LENGTH_LONG).show();
                            LocalStorage.getLocalStorageInstance(getActivity().getApplicationContext()).saveLoggedinInformation(tokenInformation,new User(email,email));

                        },
                        throwable -> {
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
   /*   Intent intent = new Intent(getActivity().getApplicationContext(), UserActivity.class);
        startActivityForResult(intent, 1);
        getActivity().finish();
*/
     try {
            Fragment fragment = MeFragment.class.newInstance();
            redirectToFragment(fragment);
        }
        catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }
    private void redirectToFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.login_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    /**
     * Routine to execute on Failed login. Logs the login attempt and displays a Toast for the user.
     */
    private void failedLogin() {
        Log.d(LOG_TAG, "Failed login.");
        loginBtn.setEnabled(true);
        Toast.makeText(getActivity().getBaseContext(), "Failed to login.", Toast.LENGTH_LONG).show();
    }

    /**
     * Starts the Register Fragment
     */
    private void register() {
        ((LoginActivity) getActivity()).changeFragment(1);
        //loginGeneralFragment.changeFragment(1);
    }

    // TODO - Restore Password
    private void restorePassword() {
       ((LoginActivity) getActivity()).changeFragment(2);
       // loginGeneralFragment.changeFragment(2);
    }

    public LoginGeneralFragment getLoginGeneralFragment() {
        return loginGeneralFragment;
    }

    public void setLoginGeneralFragment(LoginGeneralFragment loginGeneralFragment) {
        this.loginGeneralFragment = loginGeneralFragment;
    }
}
