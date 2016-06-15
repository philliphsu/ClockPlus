package com.philliphsu.clock2.ringtone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.model.AlarmsRepository;
import com.philliphsu.clock2.util.AlarmUtils;

import static com.philliphsu.clock2.util.Preconditions.checkNotNull;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * TODO: Make this abstract and make appropriate subclasses for Alarms and Timers.
 */
public class RingtoneActivity extends AppCompatActivity {
    private static final String TAG = "RingtoneActivity";

    // Shared with RingtoneService
    public static final String EXTRA_ITEM_ID = "com.philliphsu.clock2.ringtone.extra.ITEM_ID";
    public static final String ACTION_FINISH = "com.philliphsu.clock2.ringtone.action.UNBIND";

    private static boolean sIsAlive = false;

    private Alarm mAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtone);
        sIsAlive = true;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        long id = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (id < 0) {
            throw new IllegalStateException("Cannot start RingtoneActivity without item's id");
        }
        mAlarm = checkNotNull(AlarmsRepository.getInstance(this).getItem(id));
        Log.d(TAG, "Ringing alarm " + mAlarm);

        // TODO: If the upcoming alarm notification isn't present, verify other notifications aren't affected.
        // This could be the case if we're starting a new instance of this activity after leaving the first launch.
        AlarmUtils.removeUpcomingAlarmNotification(this, mAlarm);

        Intent intent = new Intent(this, RingtoneService.class)
                .putExtra(EXTRA_ITEM_ID, id);
        startService(intent);

        // TODO: Butterknife binding
        Button snooze = (Button) findViewById(R.id.btn_snooze);
        snooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snooze();
            }
        });
        Button dismiss = (Button) findViewById(R.id.btn_dismiss);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mFinishReceiver, new IntentFilter(ACTION_FINISH));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFinishReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent); // Not needed since no fragments hosted?

        // Notifies alarm missed and stops the service
        // TODO: Find a better, cleaner way? Starting the service just to set a flag and stop itself
        // is not very clear from this code. Perhaps the same Broadcast concept as done here.
        startService(new Intent(this, RingtoneService.class).setAction(RingtoneService.ACTION_NOTIFY_MISSED));
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

    private void snooze() {
        AlarmUtils.snoozeAlarm(this, mAlarm);
        // Can't call dismiss() because we don't want to also call cancelAlarm()! Why? For example,
        // we don't want the alarm, if it has no recurrence, to be turned off right now.
        stopAndFinish();
    }

    private void dismiss() {
        AlarmUtils.cancelAlarm(this, mAlarm, false); // TODO do we really need to cancel the intent and alarm?
        stopAndFinish();
    }

    private void stopAndFinish() {
        stopService(new Intent(this, RingtoneService.class));
        finish();
    }

    private final BroadcastReceiver mFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopAndFinish();
        }
    };
}