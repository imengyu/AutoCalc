package com.dreamfish.com.autocalc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.dreamfish.com.autocalc.dialog.CommonDialogs;
import com.dreamfish.com.autocalc.utils.PermissionsUtils;
import com.dreamfish.com.autocalc.utils.StatusBarUtils;
import com.dreamfish.com.autocalc.utils.UpdaterUtils;
import com.dreamfish.com.autocalc.widgets.MyTitleBar;

import androidx.annotation.NonNull;
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

        StatusBarUtils.setLightMode(this);

        MyTitleBar title_bar = findViewById(R.id.title_bar);
        title_bar.setTitle(getTitle());
        title_bar.setLeftIconOnClickListener((View v) -> finish());
    }

    private UpdaterUtils updater = null;

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference app_check_update = findPreference("app_check_update");
            Preference app_privacy_policy = findPreference("app_privacy_policy");
            Preference app_about = findPreference("app_about");

            Activity activity = getActivity();

            assert activity != null;
            assert app_privacy_policy != null;
            assert app_check_update != null;
            assert app_about != null;

            app_privacy_policy.setOnPreferenceClickListener(preference -> {
                CommonDialogs.showPrivacyPolicyAndAgreement(this.getActivity(), null);
                return true;
            });
            app_check_update.setOnPreferenceClickListener(preference -> {
                UpdaterUtils.getInstance().checkUpdate(false);
                activity.finish();
                return true;
            });
            app_about.setOnPreferenceClickListener(preference -> {
                CommonDialogs.showAbout(activity);
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}