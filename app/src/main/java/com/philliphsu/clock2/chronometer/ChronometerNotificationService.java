/*
 * Copyright (C) 2016 Phillip Hsu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.philliphsu.clock2.chronometer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.SimpleArrayMap;

import com.philliphsu.clock2.R;

/**
 * Created by Phillip Hsu on 9/10/2016.
 */
public abstract class ChronometerNotificationService extends Service {
    public static final String ACTION_START_PAUSE = "com.philliphsu.clock2.timers.action.START_PAUSE";
    public static final String ACTION_STOP = "com.philliphsu.clock2.timers.action.STOP";

    public static final String EXTRA_ACTION_ID = "com.philliphsu.clock2.extra.ID";

    // TODO: I think we'll need a collection of builders too. However, we can have a common immutable
    // builder instance with attributes that all timer notifications will have.
//    private NotificationCompat.Builder mNoteBuilder;
    private NotificationManager mNotificationManager;
    /**
     * The default capacity of an array map is 0.
     * The minimum amount by which the capacity of a ArrayMap will increase
     * is currently {@link SimpleArrayMap#BASE_SIZE 4}.
     */
    private final SimpleArrayMap<Long, NotificationCompat.Builder> mNoteBuilders = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, ChronometerNotificationThread> mThreads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, ChronometerDelegate> mDelegates = new SimpleArrayMap<>();

    /**
     * @return the icon for the notification
     */
    @DrawableRes
    protected abstract int getSmallIcon();

    /**
     * @return an optional content intent that is fired when the notification is clicked
     */
    @Nullable
    protected abstract PendingIntent getContentIntent();

    /**
     * @return whether the chronometer should be counting down
     */
    protected abstract boolean isCountDown();

    /**
     * @return the id for the foreground notification, if {@link #isForeground()} returns true.
     * Otherwise, this value will not be considered for anything.
     */
    protected abstract int getNoteId();

    /**
     * @return an optional tag associated with the notification(s). The default implementation
     * returns null if {@link #isForeground()} returns true; otherwise, it returns the class's name.
     */
    protected String getNoteTag() {
        return isForeground() ? null : getClass().getName();
    }

    /**
     * @return whether this service should run in the foreground. The default is true.
     */
    protected boolean isForeground() {
        return true;
    }

    /**
     * The intent received in {@link #onStartCommand(Intent, int, int)}
     * has no {@link Intent#getAction() action} set. At this point, you
     * should configure the notification to be displayed.
     * @param intent
     * @param flags
     * @param startId
     */
    protected abstract void handleDefaultAction(Intent intent, int flags, int startId);

    protected abstract void handleStartPauseAction(Intent intent, int flags, int startId);

    protected abstract void handleStopAction(Intent intent, int flags, int startId);

    /**
     * This will be called if the command in {@link #onStartCommand(Intent, int, int)}
     * has an action that your subclass defines.
     * @param action Your custom action.
     * @param startId
     */
    protected abstract void handleAction(@NonNull String action, Intent intent, int flags, int startId);

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (isForeground()) {
            registerNewNoteBuilder(getNoteId());
            // IGNORE THE LINT WARNING ABOUT UNNECESSARY BOXING. Because getNoteId() returns an int,
            // it gets boxed to an Integer. A Long and an Integer are never interchangeable, even
            // if they wrap the same integer value.
            startForeground(getNoteId(), mNoteBuilders.get(Long.valueOf(getNoteId())).build());
        }
    }

    private void registerNewChronometer(long id) {
        if (mDelegates.containsKey(id))
            return;
        ChronometerDelegate delegate = new ChronometerDelegate();
        delegate.init();
        delegate.setCountDown(isCountDown());
        mDelegates.put(id, delegate);
    }

    /**
     * If a notification builder is not already registered with the provided ID,
     * then register a new instance. Each new builder comes with a new chronometer.
     */
    protected final void registerNewNoteBuilder(long id) {
        // If we didn't have this check, then we'd be replacing a previous
        // builder with a new instance. The problem with that is the new id->builder
        // mapping here will not kept in sync with the builder reference in the
        // ChronometerNotificationThread, unless you make a later call to
        // startNewThread(). If the chronometer represents a Timer and it gets paused,
        // quitCurrentThread() is called, but startNewThread() will NOT be called.
        // In quitCurrentThread(), we tell the thread to update the notification for us
        // prior to actually quiting. This is where the problem manifests: because the
        // thread is holding onto the previous builder instance, updating the notification
        // means the old builder's attributes will be used to build a new notification.
        // When we later make attribute changes to the builder via e.g. addAction(),
        // we are actually affecting the new builder instance registered here,
        // not the old instance held by the thread.
        //
        // We could have avoided this out-of-sync issue by updating the notification
        // in this class, using getNoteTag() and the id passed to quitCurrentThread(),
        // instead of telling the thread to do it for us.
        if (mNoteBuilders.containsKey(id))
            return;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(getSmallIcon())
                .setShowWhen(false)
                .setOngoing(true)
                .setContentIntent(getContentIntent());
        mNoteBuilders.put(id, builder);
        registerNewChronometer(id);
    }

    // Didn't work!
//    @Override
//    public void onTrimMemory(int level) {
//        if (level >= TRIM_MEMORY_BACKGROUND) {
//            Log.d("ChronomNotifService", "Stopping foreground");
//            // The penultimate trim level, indicates the process is around the
//            // middle of the background LRU list.
//            // If we didn't call this, we would continue to run in the foreground
//            // until we get killed, and the notification would be removed with it.
//            // We want to keep the notification alive even if the process is killed,
//            // so the user can still be aware of the stopwatch.
//            stopForeground(true);
//            // Post it again, but outside of the foreground state.
//            updateNotification(true);
//        }
//    }

    @Override
    public void onDestroy() {
        for (int i = 0; i < mThreads.size(); i++) {
            // TOneverDO: quitCurrentThread() because that posts the notification again
            quitThread(mThreads.keyAt(i));
        }
    }

    @CallSuper
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_START_PAUSE:
                        handleStartPauseAction(intent, flags, startId);
                        break;
                    case ACTION_STOP:
                        handleStopAction(intent, flags, startId);
                        break;
                    default:
                        // Defer to the subclass
                        handleAction(action, intent, flags, startId);
                        break;
                }
            } else {
                handleDefaultAction(intent, flags, startId);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public final IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * If there is a thread currently running, then this will push any notification updates
     * you might have configured in the Builder and then call the thread's {@link
     * ChronometerNotificationThread#quit() quit()}.
     * @param id the id associated with the thread to quit
     */
    public void quitCurrentThread(long id) {
        ChronometerNotificationThread thread = mThreads.get(id);
        if (thread != null) {
            // Display any notification updates associated with the current state
            // of the chronometer. If we relied on the HandlerThread to do this for us,
            // the message delivery would be delayed.
            // TODO: We could update the notification ourselves using getNoteTag() and
            // the id param. This is desirable if we ever encounter other "out-of-sync"
            // issues regarding the notification builder references between this class
            // and the thread. See #registerNewNoteBuilder() for more info.
            thread.updateNotification(false/*updateText*/);
            // If the chronometer has been set to not run, the effect is obvious.
            // Otherwise, we're preparing for the start of a new thread.
            quitThread(id);
        }
    }

    /**
     * Instantiates a new HandlerThread and calls its {@link Thread#start() start()}.
     * The calling thread will be blocked until the HandlerThread created here finishes
     * initializing its looper.
     * @param id
     * @param base the new base time of the chronometer
     */
    public void startNewThread(long id, long base) {
        // An instance of Thread cannot be started more than once. You must create
        // a new instance if you want to start the Thread's work again.
        ChronometerNotificationThread thread = new ChronometerNotificationThread(
                mDelegates.get(id),
                mNotificationManager,
                mNoteBuilders.get(id),
                getResources(),
                getNoteTag(),
                (int) id);
        mThreads.put(id, thread);
        // Initializes this thread as a looper. HandlerThread.run() will be executed
        // in this thread.
        // This gives you a chance to create handlers that then reference this looper,
        // before actually starting the loop.
        thread.start();
        // If this thread has been started, this method will block *the calling thread*
        // until the looper has been initialized. This ensures the handler thread is
        // fully initialized before we proceed.
        thread.getLooper();
        // -------------------------------------------------------------------------------
        // TOneverDO: Set base BEFORE the thread is ready to begin working, or else when
        // the thread actually begins working, it will initially show that some time has
        // passed.
        ChronometerDelegate delegate = mDelegates.get(id);
        delegate.setBase(base);
        // -------------------------------------------------------------------------------
    }

    /**
     * Releases all resources associated with this id. This is only
     * necessary for subclasses that support multiple notifications,
     * because they don't have the convenience of stopping the service
     * altogether to GC all resources.
     */
    @CallSuper
    protected void releaseResources(long id) {
        mNoteBuilders.remove(id);
        quitThread(id);
        mThreads.remove(id);
        mDelegates.remove(id);
    }

    /**
     * Helper method to add the start/pause action to the notification's builder.
     * @param running whether the chronometer is running
     * @param id The id of the notification that the action should be added to.
     *           Will be used as an integer request code to create the PendingIntent that
     *           is fired when this action is clicked.
     */
    protected final void addStartPauseAction(boolean running, long id) {
        addAction(ACTION_START_PAUSE,
                running ? R.drawable.ic_pause_24dp : R.drawable.ic_start_24dp,
                getString(running ? R.string.pause : R.string.resume),
                id);
    }

    /**
     * Helper method to add the stop action to the notification's builder.
     * @param id The id of the notification that the action should be added to.
     *           Will be used as an integer request code to create the PendingIntent that
     *           is fired when this action is clicked.
     */
    protected final void addStopAction(long id) {
        addAction(ACTION_STOP, R.drawable.ic_stop_24dp, getString(R.string.stop), id);
    }

    /**
     * Clear the notification builder's set actions.
     * @param id the id associated with the builder whose actions should be cleared
     */
    protected final void clearActions(long id) {
        // TODO: The source indicates mActions is hidden, so how are we able to access it?
        // Will it remain accessible for all SDK versions? If not, we would have to rebuild
        // the entire notification with a new local Builder instance.
        mNoteBuilders.get(id).mActions.clear();
    }

    /**
     * @param id The id associated with the chronometer that you wish to modify.
     */
    protected final void setBase(long id, long base) {
        mDelegates.get(id).setBase(base);
    }

    /**
     * @param id The id associated with the chronometer that you wish to modify.
     */
    protected final long getBase(long id) {
        return mDelegates.get(id).getBase();
    }

    /**
     * @param id The id associated with the thread that should update the notification.
     */
    protected final void updateNotification(long id, boolean updateText) {
        mThreads.get(id).updateNotification(updateText);
    }

    /**
     * @param id The id associated with the builder that should update its content title.
     */
    protected final void setContentTitle(long id, CharSequence title) {
        mNoteBuilders.get(id).setContentTitle(title);
    }

    /**
     * Adds the specified action to the notification's Builder.
     * @param id The id of the notification that the action should be added to.
     *           Will be used as an integer request code to create the PendingIntent that
     *           is fired when this action is clicked.
     */
    protected final void addAction(String action, @DrawableRes int icon, String actionTitle, long id) {
        Intent intent = new Intent(this, getClass())
                .setAction(action)
                .putExtra(EXTRA_ACTION_ID, id);
        PendingIntent pi = PendingIntent.getService(
                this, (int) id, intent, 0/*no flags*/);
        mNoteBuilders.get(id).addAction(icon, actionTitle, pi);
    }

    /**
     * Cancels the notification with the pair ({@link #getNoteTag() tag}, id)
     */
    protected final void cancelNotification(long id/*TODO: change to int noteId?*/) {
        mNotificationManager.cancel(getNoteTag(), (int) id);
    }

    /**
     * Causes the handler thread's looper to terminate without processing
     * any more messages in the message queue.
     */
    private void quitThread(long id) {
        ChronometerNotificationThread thread = mThreads.get(id);
        if (thread != null && thread.isAlive()) {
            thread.quit();
        }
    }
}
