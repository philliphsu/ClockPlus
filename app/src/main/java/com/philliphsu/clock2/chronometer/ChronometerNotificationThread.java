package com.philliphsu.clock2.chronometer;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Phillip Hsu on 9/10/2016.
 *
 * A thread that updates a chronometer-based notification. While notifications
 * have built-in support for using a chronometer, it lacks pause/resume functionality
 * and the ability to choose between count up or count down.
 */
public class ChronometerNotificationThread extends HandlerThread {
    private static final String TAG = "ChronomNotifThread";

    private static final int MSG_WHAT = 2;

    private final ChronometerDelegate mDelegate;
    private final NotificationManager mNotificationManager;
    private final NotificationCompat.Builder mNoteBuilder;
    private final Resources mResources;
    private final String mNoteTag;
    private final int mNoteId;

    private Handler mHandler;

    /**
     * @param delegate Configured by the client service, including whether to be counting down or not.
     * @param builder A preconfigured Builder from the client service whose content
     *                text will be updated and eventually built from.
     * @param resources Required only if the ChronometerDelegate is configured to count down.
*                  Used to retrieve a String resource if/when the countdown reaches negative.
*                  TODO: Will the notification be cancelled fast enough before the countdown
     * @param noteTag An optional tag for posting notifications.
     */
    public ChronometerNotificationThread(@NonNull ChronometerDelegate delegate,
                                         @NonNull NotificationManager manager,
                                         @NonNull NotificationCompat.Builder builder,
                                         @Nullable Resources resources,
                                         @Nullable String noteTag,
                                         int noteId) {
        super(TAG);
        mDelegate = delegate;
        mNotificationManager = manager;
        mNoteBuilder = builder;
        mResources = resources;
        mNoteTag = noteTag;
        mNoteId = noteId;
    }

    // There won't be a memory leak since our handler is using a looper that is not
    // associated with the main thread. The full Lint warning confirmed this.
    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        // This is called after the looper has completed initializing, but before
        // it starts looping through its message queue. Right now, there is no
        // message queue, so this is the place to create it.
        // By default, the constructor associates this handler with this thread's looper.
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message m) {
                updateNotification(true);
                sendMessageDelayed(Message.obtain(this, MSG_WHAT), 1000);
            }
        };
        // Once the handler is initialized, we may immediately begin our work.
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_WHAT), 1000);
    }

    /**
     * @param updateText whether the new notification should update its chronometer.
     *                   Use {@code false} if you are updating everything else about the notification,
     *                   e.g. you just want to refresh the actions due to a start/pause state change.
     */
    public void updateNotification(boolean updateText) {
        if (updateText) {
            CharSequence text = mDelegate.formatElapsedTime(SystemClock.elapsedRealtime(), mResources);
            mNoteBuilder.setContentText(text);
        }
        mNotificationManager.notify(mNoteTag, mNoteId, mNoteBuilder.build());
    }

    @Override
    public boolean quit() {
        // TODO: I think we can call removeCallbacksAndMessages(null)
        // to remove ALL messages.
        mHandler.removeMessages(MSG_WHAT);
        return super.quit();
    }
}
