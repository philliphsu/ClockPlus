package com.philliphsu.clock2.ringtone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.UpcomingAlarmReceiver;
import com.philliphsu.clock2.editalarm.AlarmUtils;
import com.philliphsu.clock2.model.AlarmsRepository;

import static com.philliphsu.clock2.util.Preconditions.checkNotNull;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * TODO: Make this abstract and make appropriate subclasses for Alarms and Timers.
 * TODO: Implement dismiss and extend logic here.
 */
public class RingtoneActivity extends AppCompatActivity {

    // Shared with RingtoneService
    public static final String EXTRA_ITEM_ID = "com.philliphsu.clock2.ringtone.extra.ITEM_ID";

    private Alarm mAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtone);
        long id = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (id < 0) {
            throw new IllegalStateException("Cannot start RingtoneActivity without item's id");
        }
        mAlarm = checkNotNull(AlarmsRepository.getInstance(this).getItem(id));

        // Play the ringtone
        Intent intent = new Intent(this, RingtoneService.class)
                .putExtra(EXTRA_ITEM_ID, mAlarm.id());
        startService(intent);

        AlarmUtils.removeUpcomingAlarmNotification(this, mAlarm);

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

    private void snooze() {
        // Schedule another launch
        Intent intent = new Intent(this, RingtoneActivity.class)
                .setData(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pi);
        // Post snoozing notif right away
        Intent intent2 = new Intent(this, UpcomingAlarmReceiver.class)
                .setAction(UpcomingAlarmReceiver.ACTION_SHOW_SNOOZING);
        sendBroadcast(intent2);
        dismiss();
    }

    private void dismiss() {
        // TODO: Use appropriate subclass
        stopService(new Intent(this, RingtoneService.class));
        // TODO: Do we need to cancel the PendingIntent and the alarm in AlarmManager?
        finish();
    }
}
