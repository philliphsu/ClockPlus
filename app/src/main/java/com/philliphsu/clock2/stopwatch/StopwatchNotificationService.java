package com.philliphsu.clock2.stopwatch;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;

import com.philliphsu.clock2.MainActivity;
import com.philliphsu.clock2.R;

public class StopwatchNotificationService extends Service {
    private static final String ACTION_ADD_LAP = "com.philliphsu.clock2.stopwatch.action.ADD_LAP";
    private static final String ACTION_START_PAUSE = "com.philliphsu.clock2.stopwatch.action.START_PAUSE";
    private static final String ACTION_STOP = "com.philliphsu.clock2.stopwatch.action.STOP";

    private NotificationCompat.Builder mNoteBuilder;
    private NotificationManager mNotificationManager;
    private AsyncLapsTableUpdateHandler mLapsTableUpdateHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mLapsTableUpdateHandler = new AsyncLapsTableUpdateHandler(this, null);

        // Create base note
        // TODO: I think we can make this a foreground service so even
        // if the process is killed, this service remains alive.
        mNoteBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stopwatch_24dp)
                .setOngoing(true)
                // TODO: The chronometer takes the place of the 'when' timestamp
                // at its usual location. If you don't like this location,
                // we can write a thread that posts a new notification every second
                // that updates the content text.
                // TODO: We would have to write our own chronometer logic if there
                // is no way to pause/resume the native chronometer.
                .setUsesChronometer(true)
                .setContentTitle(getString(R.string.stopwatch));
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(null/*TODO:MainActivity.EXTRA_SHOW_PAGE*/, 2/*TODO:MainActivity.INDEX_STOPWATCH*/);
        mNoteBuilder.setContentIntent(PendingIntent.getActivity(this, 0, intent, 0));

        // TODO: Move adding these actions to the default case
        // TODO: Change fillColor to white, to accommodate API < 21.
        // Apparently, notifications on 21+ are automatically
        // tinted to gray to contrast against the native notification background color.
        addAction(ACTION_ADD_LAP, R.drawable.ic_add_lap_24dp, getString(R.string.lap));
        // TODO: Set icon and title according to state of stopwatch
        addAction(ACTION_START_PAUSE, R.drawable.ic_pause_24dp, getString(R.string.pause));
        addAction(ACTION_STOP, R.drawable.ic_stop_24dp, getString(R.string.stop));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action == null) {
                // TODO: Read the stopwatch's start time in shared prefs.
                mNoteBuilder.setWhen(System.currentTimeMillis());
                // TODO: Lap # content text
                mNoteBuilder.setContentText("Lap 1");
                // Use class name as tag instead of defining our own tag constant, because
                // the latter is limited to 23 (?) chars if you also want to use it as
                // a log tag.
                mNotificationManager.notify(getClass().getName(), 0, mNoteBuilder.build());
            } else {
                switch (action) {
                    case ACTION_ADD_LAP:
//                        mLapsTableUpdateHandler.asyncInsert(null/*TODO*/);
                        break;
                    case ACTION_START_PAUSE:
                        break;
                    case ACTION_STOP:
                        // Cancels all of the notifications issued by *this instance* of the manager,
                        // not those of any other instances (in this app or otherwise).
                        // TODO: We could cancel by (tag, id) if we cared.
                        mNotificationManager.cancelAll();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Builds and adds the specified action to the notification's mNoteBuilder.
     */
    private void addAction(String action, @DrawableRes int icon, String actionTitle) {
        Intent intent = new Intent(this, StopwatchNotificationService.class)
                .setAction(action);
        PendingIntent pi = PendingIntent.getService(this, 0/*no requestCode*/,
                intent, 0/*no flags*/);
        mNoteBuilder.addAction(icon, actionTitle, pi);
    }
}
