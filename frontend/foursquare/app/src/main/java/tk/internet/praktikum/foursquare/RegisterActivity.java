package tk.internet.praktikum.foursquare;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tk.internet.praktikum.foursquare.api.ServiceFactory;
import tk.internet.praktikum.foursquare.api.pojo.LoginCredentials;
import tk.internet.praktikum.foursquare.api.services.UserService;

public class RegisterActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegisterActivity.class.getSimpleName();
    private final String URL = "https://dev.ip.stimi.ovh/";

    private EditText nameInput, emailInput, passwordInput;
    private AppCompatButton registerBtn;
    private TextView loginLbl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameInput = (EditText) findViewById(R.id.register_name_input);
        emailInput = (EditText) findViewById(R.id.user_input);
        passwordInput = (EditText) findViewById(R.id.register_password_input);
        registerBtn = (AppCompatButton) findViewById(R.id.create_acc_btn);
        loginLbl = (TextView) findViewById(R.id.login_link);

        registerBtn.setOnClickListener(v -> register());

        loginLbl.setOnClickListener(v -> finish());
    }

    /**
     * Start the Registration process. At the moment it validates the input and shows the progress dialog.
     */
    private void register() {
        if (!validate()) {
            failedRegister();
            return;
        }

        registerBtn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this, 0);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Waiting for the Registration...");
        progressDialog.show();

        String name = nameInput.getText().toString();
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        UserService service = ServiceFactory.createRetrofitService(UserService.class, URL);

        service.postRegistration(new LoginCredentials(name, email, password))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        loginCredentials -> {
                            successfulRegister();
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Registration complete.", Toast.LENGTH_LONG).show();
                        },
                        throwable -> {
                            failedRegister();
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

        String name = nameInput.getText().toString();
        String eMail = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (eMail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(eMail).matches()) {
            emailInput.setError("Please enter a valid email address.");
            valid = false;
        } else
            emailInput.setError(null);

        if (name.isEmpty() || name.length() < 3) {
            nameInput.setError("Please enter a valid name.");
            valid = false;
        } else
            nameInput.setError(null);

        if (password.isEmpty() || password.length() < 6) {
            passwordInput.setError("Please enter a valid password (> 6 characters).");
            valid = false;
        } else
            passwordInput.setError(null);

        return valid;
    }

    /**
     * Start up the next Activity or Fragment after a successful registration. At the moment it just logs
     * the login and finishes the Activity.
     */
    private void successfulRegister() {
        Log.d(LOG_TAG, "Successful login.");
        registerBtn.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    /**
     * Routine to execute on Failed registration. Logs the login attempt and displays a Toast for the user.
     */
    private void failedRegister() {
        Log.d(LOG_TAG, "Failed login.");
        registerBtn.setEnabled(true);
        Toast.makeText(getBaseContext(), "Failed to register.", Toast.LENGTH_LONG).show();
    }
}
