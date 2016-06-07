package com.philliphsu.clock2.alarms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.TextView;

import com.philliphsu.clock2.DurationDisplayer;
import com.philliphsu.clock2.TickHandler;
import com.philliphsu.clock2.util.DurationUtils;

/**
 * Created by Phillip Hsu on 4/30/2016.
 */
public class AlarmCountdown extends TextView implements DurationDisplayer {
    private static final String TAG = "NextAlarmText";
    private static final int TICK_INTERVAL = 60000; // per minute

    private TickHandler mHandler;

    public AlarmCountdown(Context context) {
        super(context);
    }

    public AlarmCountdown(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setOnTickListener(@NonNull OnTickListener listener) {
        mHandler = new TickHandler(listener, TICK_INTERVAL);
    }

    @Override
    public void showAsText(long duration) {
        setText(DurationUtils.toString(getContext(), duration, true));
    }

    @Override
    public void startTicking(boolean resume) {
        mHandler.startTicking(resume);
    }

    @Override
    public void stopTicking() {
        mHandler.stopTicking();
    }

    @Override
    public void forceTick() {
        mHandler.forceTick();
    }
}
