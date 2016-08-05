package com.philliphsu.clock2.ringtone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.philliphsu.clock2.util.LocalBroadcastHelper;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public abstract class RingtoneActivity<T extends Parcelable> extends AppCompatActivity {
    private static final String TAG = "RingtoneActivity";

    // Shared with RingtoneService
    public static final String ACTION_FINISH = "com.philliphsu.clock2.ringtone.action.FINISH";
    public static final String EXTRA_RINGING_OBJECT = "com.philliphsu.clock2.ringtone.extra.RINGING_OBJECT";

    private static boolean sIsAlive = false;
    private T mRingingObject;

    // TODO: Should we extend from BaseActivity instead?
    @LayoutRes
    public abstract int layoutResource();

    protected abstract Class<? extends RingtoneService> getRingtoneServiceClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutResource());
        sIsAlive = true;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        if ((mRingingObject = getIntent().getParcelableExtra(EXTRA_RINGING_OBJECT)) == null) {
            throw new IllegalStateException("Cannot start RingtoneActivity without a ringing object");
        }
        Intent intent = new Intent(this, getRingtoneServiceClass())
                .putExtra(EXTRA_RINGING_OBJECT, mRingingObject);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: Do we need this anymore? I think this broadcast was only sent from
        // EditAlarmActivity?
        LocalBroadcastHelper.registerReceiver(this, mFinishReceiver, ACTION_FINISH);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TODO: Do we need this anymore? I think this broadcast was only sent from
        // EditAlarmActivity?
        LocalBroadcastHelper.unregisterReceiver(this, mFinishReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent); // Not needed since no fragments hosted?
        // TODO: Do we need this anymore? I think the broadcast that calls through to
        // this was only sent from EditAlarmActivity?

        // Notifies alarm missed and stops the service
        LocalBroadcastHelper.sendBroadcast(this, RingtoneService.ACTION_NOTIFY_MISSED);
        finish();
        startActivity(intent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Set the content to appear under the system bars so that the
            // content doesn't resize when the system bars hide and show.
            // The system bars will remain hidden on user interaction;
            // however, they can be revealed using swipe gestures along
            // the region where they normally appear.
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;

            // Make status bar translucent, which automatically adds
            // SYSTEM_UI_FLAG_LAYOUT_STABLE and SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // Looks too light on the current background..
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void onBackPressed() {
        // Capture the back press and return. We want to limit the user's options for leaving
        // this activity as much as possible.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sIsAlive = false;
    }

    public static boolean isAlive() {
        return sIsAlive;
    }

    /**
     * Exposed to subclasses so they can force us to stop the
     * ringtone and finish us.
     */
    protected final void stopAndFinish() {
        stopService(new Intent(this, getRingtoneServiceClass()));
        finish();
    }

    protected final T getRingingObject() {
        return mRingingObject;
    }

    // TODO: Do we need this anymore? I think this broadcast was only sent from
    // EditAlarmActivity?
    private final BroadcastReceiver mFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopAndFinish();
        }
    };
}