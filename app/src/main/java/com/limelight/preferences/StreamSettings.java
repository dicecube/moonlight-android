package com.limelight.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Activity;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.limelight.PcView;
import com.limelight.R;
import com.limelight.binding.input.virtual_controller.VirtualController;
import com.limelight.binding.input.virtual_controller.VirtualControllerConfiguration;
import com.limelight.utils.UiHelper;

import java.util.Locale;

public class StreamSettings extends Activity {
    private PreferenceConfiguration previousPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        previousPrefs = PreferenceConfiguration.readPreferences(this);

        if (!previousPrefs.language.equals(PreferenceConfiguration.DEFAULT_LANGUAGE)) {
            Configuration config = new Configuration(getResources().getConfiguration());
            config.locale = new Locale(previousPrefs.language);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }

        setContentView(R.layout.activity_stream_settings);
        getFragmentManager().beginTransaction().replace(
                R.id.stream_settings, new SettingsFragment()
        ).commit();

        UiHelper.notifyNewRootView(this);
    }

    @Override
    public void onBackPressed() {
        finish();

        // Check for changes that require a UI reload to take effect
        PreferenceConfiguration newPrefs = PreferenceConfiguration.readPreferences(this);
        if (newPrefs.listMode != previousPrefs.listMode ||
                newPrefs.smallIconMode != previousPrefs.smallIconMode ||
                !newPrefs.language.equals(previousPrefs.language)) {
            // Restart the PC view to apply UI changes
            Intent intent = new Intent(this, PcView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent, null);
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            // Add a listener to the FPS and resolution preference
            // so the bitrate can be auto-adjusted
            Preference pref = findPreference(PreferenceConfiguration.RES_FPS_PREF_STRING);
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsFragment.this.getActivity());
                    String valueStr = (String) newValue;

                    // Write the new bitrate value
                    prefs.edit()
                            .putInt(PreferenceConfiguration.BITRATE_PREF_STRING,
                                    PreferenceConfiguration.getDefaultBitrate(valueStr))
                            .apply();

                    // Allow the original preference change to take place
                    return true;
                }
            });

            Preference siteVirtualControllerButton = (Preference)findPreference("button_open_virtual_controller_configuration");
            siteVirtualControllerButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference arg0)
                {
                    Intent virtualControllerConfiguration = new Intent(getActivity(), VirtualControllerConfiguration.class);
                    startActivity(virtualControllerConfiguration);

                    return true;
                }
            });
        }
    }
}
