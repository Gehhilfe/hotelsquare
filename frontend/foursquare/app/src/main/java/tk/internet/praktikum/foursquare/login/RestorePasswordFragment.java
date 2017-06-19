package tk.internet.praktikum.foursquare.login;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import tk.internet.praktikum.foursquare.R;

public class RestorePasswordFragment extends Fragment {

    private static final String LOG_TAG = RestorePasswordFragment.class.getSimpleName();

    private EditText email;
    private AppCompatButton resetPwBtn;
    private  LoginGeneralFragment loginGeneralFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restore_password, container, false);

        email = (EditText) view.findViewById(R.id.reset_email);
        resetPwBtn = (AppCompatButton) view.findViewById(R.id.reset_password_btn);

        resetPwBtn.setOnClickListener(v -> resetPassword());

        return view;
    }

    private void resetPassword() {
        if (!validate()) {
            failedReset();
            return;
        }

        resetPwBtn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity(), 0);
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
        ((LoginActivity) getActivity()).changeFragment(0);
        //loginGeneralFragment.changeFragment(0);
    }

    /**
     * Routine to execute on Failed rest. Logs the attempt and displays a Toast for the user.
     */

    private void failedReset() {
        Log.d(LOG_TAG, "Failed login.");
        resetPwBtn.setEnabled(true);
        Toast.makeText(getActivity().getBaseContext(), "Couldn't reset the password.", Toast.LENGTH_LONG).show();
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
    public LoginGeneralFragment getLoginGeneralFragment() {
        return loginGeneralFragment;
    }

    public void setLoginGeneralFragment(LoginGeneralFragment loginGeneralFragment) {
        this.loginGeneralFragment = loginGeneralFragment;
    }
}
