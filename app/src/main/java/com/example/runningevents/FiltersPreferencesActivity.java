package com.example.runningevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.util.Log;
import android.widget.Toast;

public class FiltersPreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters_preferences);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.filtersFragment, new FiltersFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static class FiltersFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

        androidx.preference.CheckBoxPreference checkBoxDistanceAll;
        androidx.preference.CheckBoxPreference checkBoxDistance5km;
        androidx.preference.CheckBoxPreference checkBoxDistance10km;
        androidx.preference.CheckBoxPreference checkBoxDistanceHalf;
        androidx.preference.CheckBoxPreference checkBoxDistanceMarathon;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.filter_preferences, rootKey);


            checkBoxDistanceAll = findPreference("distance_all");
            checkBoxDistance5km = findPreference("distance_5km");
            checkBoxDistance10km = findPreference("distance_10km");
            checkBoxDistanceHalf = findPreference("distance_half");
            checkBoxDistanceMarathon = findPreference("distance_marathon");


            //Click listeners
            checkBoxDistanceAll.setOnPreferenceClickListener(this);
            checkBoxDistance5km.setOnPreferenceClickListener(this);
            checkBoxDistance10km.setOnPreferenceClickListener(this);
            checkBoxDistanceHalf.setOnPreferenceClickListener(this);
            checkBoxDistanceMarathon.setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            if (key.equals("distance_all")) {
                if(checkBoxDistanceAll.isChecked() == true) {
                    checkBoxDistance5km.setChecked(false);
                    checkBoxDistance10km.setChecked(false);
                    checkBoxDistanceHalf.setChecked(false);
                    checkBoxDistanceMarathon.setChecked(false);
                }
                else if(checkBoxDistance5km.isChecked() == false && checkBoxDistance10km.isChecked() == false
                        && checkBoxDistanceHalf.isChecked() == false && checkBoxDistanceMarathon.isChecked() == false)
                {
                    checkBoxDistanceAll.setChecked(true);
                }
                return true;
            }

            else if (key.equals("distance_5km") || key.equals("distance_10km")  ||
                    key.equals("distance_half") || key.equals("distance_marathon")) {
                if (checkBoxDistanceAll.isChecked()) {
                    checkBoxDistanceAll.setChecked(false);
                } else if (checkBoxDistance5km.isChecked() == false && checkBoxDistance10km.isChecked() == false
                        && checkBoxDistanceHalf.isChecked() == false && checkBoxDistanceMarathon.isChecked() == false) {
                    checkBoxDistanceAll.setChecked(true);
                }
                return true;
            }

            else {
                return false;
            }
        }
    }
}
