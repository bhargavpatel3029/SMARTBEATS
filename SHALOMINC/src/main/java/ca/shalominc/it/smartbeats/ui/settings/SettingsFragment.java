//Bhargav Patel (N01373029) & Ripal Patel (N01354619) & Vidhi Kanhye (N01354573) & Nicholas Mohan (N01361663), Section-RNA
package ca.shalominc.it.smartbeats.ui.settings;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import ca.shalominc.it.smartbeats.AboutUsActivity;
import ca.shalominc.it.smartbeats.PrivacyPolicyActivity;
import ca.shalominc.it.smartbeats.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    int flag = 1;
    Button shalomShowSettings;

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

//        shalomShowSettings = view.findViewById(R.id.shalom_show_settings);
//        shalomShowSettings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

    }

    //Sets Visibility to false in this fragment for power button In menu
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.musicBtn).setVisible(false);
        menu.findItem(R.id.lightsPwrBtn).setVisible(false);
        menu.findItem(R.id.bluetoothBtn).setVisible(false);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference myPrefportraitmode = (Preference)findPreference("portrait_switch");


        myPrefportraitmode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
                return true;
            }
        });

        Preference myPrefaboutUs = (Preference)findPreference("about_us");
        myPrefaboutUs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent;
                intent = new Intent(getContext(), AboutUsActivity.class);
                startActivity(intent);

                return false;
            }
        });
/*
        Preference myPrefHelpAndSupport = (Preference)findPreference("help_and_support");
        myPrefHelpAndSupport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent;
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/gmail/about/"));
                startActivity(intent);

                return false;
            }
        });

 */

        Preference myPrefPrivacyPolicy = (Preference)findPreference("privacy_policy");
        myPrefPrivacyPolicy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent;
                intent = new Intent(getContext(), PrivacyPolicyActivity.class);
                startActivity(intent);

                return false;
            }
        });

    }



}