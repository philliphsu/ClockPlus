package com.philliphsu.clock2.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.philliphsu.clock2.BaseActivity;
import com.philliphsu.clock2.R;

/**
 * Created by Phillip Hsu on 6/6/2016.
 */
public class SettingsActivity extends BaseActivity {
    public static final String EXTRA_THEME_CHANGED = "com.philliphsu.clock2.settings.extra.THEME_CHANGED";

    private String mInitialTheme;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mInitialTheme = getSelectedTheme();
    }

    @Override
    protected int layoutResId() {
        return R.layout.activity_settings;
    }

    @Override
    protected int menuResId() {
        return 0;
    }

    @Override
    protected boolean isDisplayShowTitleEnabled() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setThemeResult(getSelectedTheme());
                return false; // Don't capture, proceed as usual
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        setThemeResult(getSelectedTheme());
        super.onBackPressed();
    }

    private String getSelectedTheme() {
        return mPrefs.getString(getString(R.string.key_theme), "");
    }

    private void setThemeResult(String selectedTheme) {
        Intent result = new Intent();
        result.putExtra(EXTRA_THEME_CHANGED, !selectedTheme.equals(mInitialTheme));
        setResult(Activity.RESULT_OK, result);
    }
}