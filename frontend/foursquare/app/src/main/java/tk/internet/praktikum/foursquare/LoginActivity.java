package tk.internet.praktikum.foursquare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.RegisterActivity;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.pojo.LoginCredentials;
import tk.internet.praktikum.foursquare.api.services.SessionService;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    private EditText emailInput, passwordInput;
    private AppCompatButton loginBtn;
    private TextView registerLbl;
    private static int REGISTER_REQUEST = 0;    // Register Request Tag für switching Between the Register and Login Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = (EditText) findViewById(R.id.register_mail_input);
        passwordInput = (EditText) findViewById(R.id.register_password_input);
        loginBtn = (AppCompatButton) findViewById(R.id.login_btn);
        registerLbl = (TextView) findViewById(R.id.login_link);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        registerLbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    /**
     * Starts the login sequence. For now it only validates the input and displays the Progress dialog for 3 seconds.
     */
    private void login() {
        if (!validate()) {
            failedLogin();
            return;
        }

        loginBtn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, 0);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Waiting for login...");
        progressDialog.show();

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        // Get the SessionService from the backend api
        // Url should be stored somewhere as an constant
        SessionService service = ServiceFactory.createRetrofitService(SessionService.class, "https://dev.ip.stimi.ovh/");

        /**
         * Use RxJava to handle a long runing backend api call without blocking the application
         * Would be more clean if we used java 1.8 target with Jack
         */
        service.postSession(new LoginCredentials(email, password))
                .subscribeOn(Schedulers.newThread())                // call is executed i a new thread
                .observeOn(AndroidSchedulers.mainThread())          // response is handled in main thread
                .subscribe(
                        tokenInformation -> {
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
     * Validates the entered input. Might be unnecessary if we only validate on the backend.
     * @return True or false depending on if the input is valid.
     */
    private boolean validate() {
        boolean valid = true;

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        return valid;
    }

    /**
     * Start up the next Activity or Fragment after a successful login. At the moment it just logs
     * the login and finishes the Activity.
     */
    private void successfulLogin() {
        Log.d(LOG_TAG, "Successful login.");
        loginBtn.setEnabled(true);
        finish();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REGISTER_REQUEST) {
            if (resultCode == RESULT_OK) {
                // TODO - Start the new Activity after a successful registration. Probably going on the the User Setting view
                finish();
            }
        }
    }
}
