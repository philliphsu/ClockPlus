package com.philliphsu.clock2.settings;

import android.os.Bundle;

import com.philliphsu.clock2.BaseActivity;
import com.philliphsu.clock2.R;

/**
 * Created by Phillip Hsu on 6/6/2016.
 */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}