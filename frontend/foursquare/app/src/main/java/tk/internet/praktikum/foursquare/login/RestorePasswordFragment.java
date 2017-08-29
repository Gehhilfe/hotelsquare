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
import tk.internet.praktikum.foursquare.api.bean.PasswordResetInformation;
import tk.internet.praktikum.foursquare.api.service.UserService;

public class RestorePasswordFragment extends Fragment {

    private static final String LOG_TAG = RestorePasswordFragment.class.getSimpleName();
    private EditText email, name;
    private AppCompatButton resetPwBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restore_password, container, false);

        name = (EditText) view.findViewById(R.id.reset_name);
        email = (EditText) view.findViewById(R.id.reset_email);
        resetPwBtn = (AppCompatButton) view.findViewById(R.id.reset_password_btn);

        resetPwBtn.setOnClickListener(v -> resetPassword());

        name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    resetPassword();
                return true;
            }
        });

        return view;
    }

    /**
     * Validates the user input and starts the restore password process
     */
    private void resetPassword() {
        if (!validate()) {
            failedReset();
            return;
        }
        resetPwBtn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity(), 0);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.reset_dialog));
        progressDialog.show();


        UserService service = ServiceFactory.createRetrofitService(UserService.class, "https://dev.ip.stimi.ovh/");

        service.passwordReset(new PasswordResetInformation(name.getText().toString(), email.getText().toString()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((a) -> successfulReset(progressDialog), (a) -> failedReset(progressDialog, a));

    }

    /**
     * Notifies the user that the reset process was successful and dismisses the progress dialog.
     * @param progressDialog Dialog to dismiss
     */
    private void successfulReset(ProgressDialog progressDialog) {
        Toast.makeText(getContext(), getString(R.string.reset_completed), Toast.LENGTH_SHORT).show();
        resetPwBtn.setEnabled(true);
        progressDialog.dismiss();
       ((LoginActivity) getActivity()).changeFragment(0);
    }

    /**
     * Notifies the user that the reset process failed, dismisses the progress dialog and logs the attempt.
     * @param progressDialog Dialog to dismiss.
     * @param throwable Error message for the log.
     */
    private void failedReset(ProgressDialog progressDialog, Throwable throwable) {
        Log.d(LOG_TAG, throwable.getMessage());
        progressDialog.dismiss();
        resetPwBtn.setEnabled(true);
        Toast.makeText(getActivity().getBaseContext(), getString(R.string.reset_failed), Toast.LENGTH_LONG).show();
    }

    /**
     * Notifies the user that the reset process failed, dismisses the progress dialog and logs the attempt.
     */
    private void failedReset() {
        resetPwBtn.setEnabled(true);
        Toast.makeText(getActivity().getBaseContext(), getString(R.string.reset_failed), Toast.LENGTH_LONG).show();
    }

    /**
     * Validates the entered email.
     * @return True or false depending on if the input is valid.
     */
    private boolean validate() {
        boolean valid = true;

        String eMail = email.getText().toString();

        if (eMail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(eMail).matches()) {
            email.setError(getString(R.string.register_invalid_mail));
            valid = false;
        } else
            email.setError(null);

        return valid;
    }
}
