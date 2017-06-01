package tk.internet.praktikum.foursquare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.bean.LoginCredentials;
import tk.internet.praktikum.foursquare.api.service.SessionService;
import tk.internet.praktikum.foursquare.api.service.UserService;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    private static final int REGISTER_REQUEST = 0;    // Register Request Tag für switching Between the Register and Login Activity
    private static final int RESTORE_PW_REQUEST = 1;
    private final String URL = "https://dev.ip.stimi.ovh/";

    private EditText userInput, passwordInput;
    private AppCompatButton loginBtn;
    private TextView registerLbl, passwordForgottenLbl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userInput = (EditText) findViewById(R.id.user_input);
        passwordInput = (EditText) findViewById(R.id.register_password_input);
        loginBtn = (AppCompatButton) findViewById(R.id.login_btn);
        registerLbl = (TextView) findViewById(R.id.login_link);
        passwordForgottenLbl = (TextView) findViewById(R.id.forgotten_password);

        loginBtn.setOnClickListener(v -> login());
        registerLbl.setOnClickListener(v -> register());
        passwordForgottenLbl.setOnClickListener(v -> restorePassword());
    }

    /**
     * Starts the login sequence. For now it only validates the input and displays the Progress dialog for 3 seconds.
     */
    private void login() {
        loginBtn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, 0);
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
         * Would be more clean if we used java 1.8 target with Jack
         */
        service.postSession(new LoginCredentials(email, password))
                .subscribeOn(Schedulers.newThread())                // call is executed i a new thread
                .observeOn(AndroidSchedulers.mainThread())          // response is handled in main thread
                .subscribe(
                        tokenInformation -> {
                            UserService uservice = ServiceFactory.createRetrofitService(UserService.class, URL, tokenInformation.getToken());
                            successfulLogin();
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), tokenInformation.getToken(), Toast.LENGTH_LONG).show();
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

        finish();
        // TODO - Return to the FourSquareActivity.
    }

    /**
     * Routine to execute on Failed login. Logs the login attempt and displays a Toast for the user.
     */
    private void failedLogin() {
        Log.d(LOG_TAG, "Failed login.");
        loginBtn.setEnabled(true);
        Toast.makeText(getBaseContext(), "Failed to login.", Toast.LENGTH_LONG).show();
    }

    /**
     * Starts the Register Activity.
     */
    private void register() {
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivityForResult(intent, REGISTER_REQUEST);
    }

    // TODO - Restore Password
    private void restorePassword() {
        Intent intent = new Intent(getApplicationContext(), RestorePasswordActivity.class);
        startActivityForResult(intent, RESTORE_PW_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REGISTER_REQUEST :
                if (resultCode == RESULT_OK) {
                    // TODO - Start the new Activity after a successful registration. Probably going on the the User Setting view.
                    // TODO - Or wait at the login activity for an email confirmation and then login.
                    break;
                } else
                    break;

            case RESTORE_PW_REQUEST :
                if (resultCode == RESULT_OK) {
                    // TODO - Wait at the login activity for the user to restore his password and login.
                    break;
                } else
                    break;
        }
    }
}
