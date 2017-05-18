package praktikum.internet.tk.hotelsquare;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput;
    private AppCompatButton registerBtn;
    private TextView loginLbl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameInput = (EditText) findViewById(R.id.register_name_input);
        emailInput = (EditText) findViewById(R.id.register_mail_input);
        passwordInput = (EditText) findViewById(R.id.register_password_input);
        registerBtn = (AppCompatButton) findViewById(R.id.create_acc_btn);
        loginLbl = (TextView) findViewById(R.id.login_link);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        loginLbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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

        // TODO - Registration process with the backend api.

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                successfulRegister();
                progressDialog.dismiss();
            }
        }, 3000);
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

        if (name.isEmpty() || name.length() < 2) {
            nameInput.setError("Please enter a valid name.");
            valid = false;
        } else
            nameInput.setError(null);

        if (eMail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(eMail).matches()) {
            emailInput.setError("Please enter a valid email address.");
            valid = false;
        } else
            emailInput.setError(null);

        if (password.isEmpty() || password.length() < 8 || password.length() > 12) {
            passwordInput.setError("Please enter a valid password (8 - 12 characters).");
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
        Log.d("LOGIN_ACTIVITY", "Successful login.");
        registerBtn.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    /**
     * Routine to execute on Failed registration. Logs the login attempt and displays a Toast for the user.
     */
    private void failedRegister() {
        Log.d("LOGIN_ACTIVITY", "Failed login.");
        registerBtn.setEnabled(true);
        Toast.makeText(getBaseContext(), "Failed to register.", Toast.LENGTH_LONG).show();
    }
}
