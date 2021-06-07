package com.example.runningevents;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.runningevents.Login.activities.LoginActivity;
import com.example.runningevents.Main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class SettingsPreferencesFragment extends PreferenceFragmentCompat {

    FirebaseAuth firebaseAuth;
    Preference login;
    Preference logout;
    ListPreference languageSelect;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);

        firebaseAuth = FirebaseAuth.getInstance();

        login = findPreference("login");
        logout = findPreference("logout");
        languageSelect = findPreference("select_language");

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser.isAnonymous())
        {
            logout.setVisible(false);
        }
        else {
            login.setVisible(false);
        }

        languageSelect.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("setinzi", newValue.toString());
                Utils.setAppLocale(newValue.toString(), getResources());
                ((MainActivity) getActivity()).finish();
                return true;
            }
        });

        logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                signOut();
                return false;
            }
        });
    }

    private void signOut(){
        firebaseAuth.signOut();
        Intent intent = new Intent(((MainActivity) getActivity()), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
