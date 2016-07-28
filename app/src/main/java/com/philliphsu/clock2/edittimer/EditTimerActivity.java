package com.philliphsu.clock2.edittimer;

import android.os.Bundle;

import com.philliphsu.clock2.BaseActivity;
import com.philliphsu.clock2.R;

public class EditTimerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int layoutResId() {
        return R.layout.activity_edit_timer;
    }

    @Override
    protected int menuResId() {
        return 0;
    }
}
