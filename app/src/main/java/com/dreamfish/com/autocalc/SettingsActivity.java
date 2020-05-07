package com.dreamfish.com.autocalc;

import android.os.Bundle;
import android.view.MenuItem;

import com.dreamfish.com.autocalc.dialog.CommonDialogs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference app_check_update = findPreference("app_check_update");
            Preference app_privacy_policy = findPreference("app_privacy_policy");

            assert app_privacy_policy != null;
            app_privacy_policy.setOnPreferenceClickListener(preference -> {
                CommonDialogs.showPrivacyPolicyAndAgreement(this.getActivity(), null);
                return true;
            });
            assert app_check_update != null;
            app_check_update.setOnPreferenceClickListener(preference -> {
                //code for what you want it to do
                return true;
            });
            app_check_update.setTitle(R.string.app_version);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}