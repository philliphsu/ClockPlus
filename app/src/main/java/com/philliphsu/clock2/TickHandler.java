package com.philliphsu.clock2;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.philliphsu.clock2.DurationDisplayer.OnTickListener;

import static com.philliphsu.clock2.util.Preconditions.checkNotNull;

/**
 * Created by Phillip Hsu on 4/18/2016.
 */
public class TickHandler extends Handler {
    private static final int MSG_TICK = 0;
    private static final int MSG_FORCE_TICK = 1;

    @NonNull private final OnTickListener mOnTickListener;
    private final long mTickInterval;

    public TickHandler(@NonNull OnTickListener listener, long tickInterval) {
        super(Looper.getMainLooper());
        mOnTickListener = checkNotNull(listener);
        mTickInterval = tickInterval;
    }

    @Override
    public void handleMessage(Message msg) {
        // Account for the time showDuration() takes to execute
        // and subtract that off from the countdown interval
        // (so the countdown doesn't get postponed by however long
        // showDuration() takes)
        long startOnTick = SystemClock.elapsedRealtime();
        mOnTickListener.onTick();
        long countdownRemainder = mTickInterval -
                (SystemClock.elapsedRealtime() - startOnTick);

        // special case: onTick took longer than countdown
        // interval to complete, skip to next interval
        while (countdownRemainder < 0)
            countdownRemainder += mTickInterval;

        if (msg.what == MSG_TICK) { // as opposed to MSG_FORCE_TICK
            sendMessageDelayed(obtainMessage(MSG_TICK), mTickInterval);
        }
    }

    public void startTicking(boolean resume) {
        if (hasMessages(MSG_TICK))
            return;
        if (resume) {
            sendMessage(obtainMessage(MSG_TICK));
        } else {
            sendMessageDelayed(obtainMessage(MSG_TICK), mTickInterval);
        }
    }

    public void stopTicking() {
        removeMessages(MSG_TICK);
    }

    /**
     * Forces a single call to {@link OnTickListener#onTick() onTick()}
     * without scheduling looped messages on this handler.
     */
    public void forceTick() {
        sendMessage(obtainMessage(MSG_FORCE_TICK));
    }
}
