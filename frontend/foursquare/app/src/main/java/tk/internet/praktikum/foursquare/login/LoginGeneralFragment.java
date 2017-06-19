package tk.internet.praktikum.foursquare.login;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tk.internet.praktikum.foursquare.R;

/** Todo
 * This fragment will be adapted later.
 */
public class  LoginGeneralFragment extends Fragment {
    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;
    private RestorePasswordFragment restorePasswordFragment;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        addFragment();

        return loginFragment.onCreateView(inflater,container,savedInstanceState);

    }
    public void addFragment() {
        loginFragment = new LoginFragment();
        loginFragment.setLoginGeneralFragment(this);
        getFragmentManager().beginTransaction().add(R.id.fragment_container, loginFragment).commit();
    }

    public void changeFragment(int fragmentId) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        switch (fragmentId) {
            case 0:
                fragmentTransaction.replace(R.id.fragment_container, loginFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 1:
                if (registerFragment == null) {
                    registerFragment = new RegisterFragment();
                    registerFragment.setLoginGeneralFragment(this);
                }
                fragmentTransaction.replace(R.id.fragment_container, registerFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 2:
                if (restorePasswordFragment == null) {
                    restorePasswordFragment = new RestorePasswordFragment();
                    restorePasswordFragment.setLoginGeneralFragment(this);
                }
                fragmentTransaction.replace(R.id.fragment_container, restorePasswordFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
        }
    }

}
