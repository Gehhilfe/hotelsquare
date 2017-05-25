package tk.internet.praktikum.foursquare;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

public class RestorePasswordActivity extends AppCompatActivity {

    private static final String LOG_TAG = RestorePasswordActivity.class.getSimpleName();

    private EditText email;
    private AppCompatButton resetPwBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_password);

        email = (EditText) findViewById(R.id.reset_email);
        resetPwBtn = (AppCompatButton) findViewById(R.id.reset_password_btn);

        resetPwBtn.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        if (!validate()) {
            failedReset();
            return;
        }

        resetPwBtn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(RestorePasswordActivity.this, 0);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Waiting for the server response...");
        progressDialog.show();


        new android.os.Handler().postDelayed(() -> {
            successfulReset();
            progressDialog.dismiss();
        }, 3000);

    }

    /**
     * Logs the reset and returns to the login activity.
     */
    private void successfulReset() {
        Log.d(LOG_TAG, "Successfully reset the password.");
        resetPwBtn.setEnabled(true);
        finish();
    }

    /**
     * Routine to execute on Failed rest. Logs the attempt and displays a Toast for the user.
     */

    private void failedReset() {
        Log.d(LOG_TAG, "Failed login.");
        resetPwBtn.setEnabled(true);
        Toast.makeText(getBaseContext(), "Couldn't reset the password.", Toast.LENGTH_LONG).show();
    }

    /**
     * Validates the entered email.
     * @return True or false depending on if the input is valid.
     */
    private boolean validate() {
        boolean valid = true;

        String eMail = email.getText().toString();

        if (eMail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(eMail).matches()) {
            email.setError("Please enter a valid email address.");
            valid = false;
        } else
            email.setError(null);

        return valid;
    }
}
