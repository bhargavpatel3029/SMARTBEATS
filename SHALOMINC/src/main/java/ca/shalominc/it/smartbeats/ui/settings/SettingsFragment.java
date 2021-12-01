package ca.shalominc.it.smartbeats.ui.settings;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;

import ca.shalominc.it.smartbeats.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    int flag = 1;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference myPref = (Preference)findPreference("portrait_switch");
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                switch (flag)
                {
                    case 1:
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        Toast.makeText(getContext(),R.string.lock_portrait,Toast.LENGTH_LONG).show();
                        flag++;

                        break;

                    case 2:
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        Toast.makeText(getContext(),R.string.portrait_lock_disable,Toast.LENGTH_LONG).show();
                        flag=1;

                        break;
                }
                return false;
            }
        });

    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu)
    {
        setHasOptionsMenu(true);
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.musicBtn).setVisible(false);
        menu.findItem(R.id.lightsPwrBtn).setVisible(false);
        menu.findItem(R.id.bluetoothBtn).setVisible(false);
    }

//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//
//        return inflater.inflate(R.layout.fragment_settings, container, false);
//    }


}