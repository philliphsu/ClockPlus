package com.philliphsu.clock2.edittimer;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayout;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.philliphsu.clock2.BaseActivity;
import com.philliphsu.clock2.R;

import butterknife.Bind;

public class EditTimerActivity extends BaseActivity {

    @Bind(R.id.label) TextView mLabel;
    @Bind(R.id.duration) TextView mDuration; // TODO: Change to something suitable for input fields
    @Bind(R.id.add_one_minute) ImageButton mAddOneMinute;
    @Bind(R.id.fab) FloatingActionButton mFab;
    @Bind(R.id.stop) ImageButton mStop;
    // TODO: Consider making an abstract EditItemActivity.
    // Define these buttons in a layout file that subclasses can include
    // into their own activity layouts. Bind OnClick listeners to abstract
    // protected methods deleteItem() and saveItem().
    @Bind(R.id.delete) Button mDelete;
    @Bind(R.id.save) Button mSave;
    @Bind(R.id.numpad) GridLayout mNumpad; // TODO: Actual numpad type
    @Bind(R.id.progress_bar) ProgressBar mProgressBar;

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
