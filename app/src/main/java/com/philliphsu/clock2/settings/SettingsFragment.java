package com.philliphsu.clock2.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.view.MenuItem;

import com.philliphsu.clock2.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String mInitialTheme;
    private SharedPreferences mPrefs;

    public SettingsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // We don't have a menu, but this is needed so we can receive callbacks to the options menu.
        // The only callback we are interested in is onOptionsItemSelected().
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.preferences);
        // Set ringtone summary
        setSummary(getPreferenceScreen().getSharedPreferences(), getString(R.string.key_timer_ringtone));
        findPreference(getString(R.string.key_alarm_volume))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                am.adjustStreamVolume(
                        AudioManager.STREAM_ALARM,
                        AudioManager.ADJUST_SAME, // no adjustment
                        AudioManager.FLAG_SHOW_UI); // show the volume toast
                return true;
            }
        });
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mInitialTheme = mPrefs.getString(keyOfThemePreference(), "");
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
         * https://developer.android.com/guide/topics/ui/menus.html#RespondingOptionsMenu
         * "If your activity includes fragments, the system first calls onOptionsItemSelected()
         * for the activity then for each fragment (in the order each fragment was added)
         * until one returns true or all fragments have been called."
         */
        switch (item.getItemId()) {
            case android.R.id.home:
                String selectedTheme = mPrefs.getString(keyOfThemePreference(), "");
                Intent result = new Intent();
                result.putExtra(SettingsActivity.EXTRA_THEME_CHANGED, !selectedTheme.equals(mInitialTheme));
                getActivity().setResult(Activity.RESULT_OK, result);
                return false; // Don't capture, proceed as usual
            default:
                return false;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setSummary(sharedPreferences, key);
        // --------------------------------------------------------------------------------------
        // TOneverDO
        //
        // A new instance of the activity would be created and onCreate() called again.
        // Our reading of the initial theme for the previous instance, which would be destroyed,
        // would not be retained.
        // Reading the initial theme *for the new instance* would give us the theme
        // that the previous instance had just changed to, prior to its recreate().
        // As such, the result passed back *by the new instance* via the up button
        // would always indicate the theme didn't change.
//        if (key.equals(keyOfThemePreference())) {
//            getActivity().recreate();
//        }
        // --------------------------------------------------------------------------------------
    }

    private void setSummary(SharedPreferences prefs, String key) {
        Preference pref = findPreference(key);
        // Setting a ListPreference's summary value to "%s" in XML automatically updates the
        // preference's summary to display the selected value.
        if (pref instanceof RingtonePreference) {
            Uri ringtoneUri = Uri.parse(prefs.getString(key, ""));
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
            pref.setSummary(ringtone.getTitle(getActivity()));
        }
    }

    private String keyOfThemePreference() {
        return getString(R.string.key_theme);
    }
}
