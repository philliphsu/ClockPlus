package com.philliphsu.clock2;

import android.support.annotation.NonNull;

/**
 * Created by Phillip Hsu on 4/30/2016.
 */
public interface DurationDisplayer {
    /**
     * Callback interface to be implemented by a parent/host of this displayer.
     * The host should use this to tell its displayer to update its duration text
     * via showAsText(long).
     */
    /*public*/ interface OnTickListener {
        // Listeners implement this to be notified of when
        // they should update this displayer's text
        void onTick();
    }

    /** Hosts of this displayer use this to attach themselves */
    void setOnTickListener(@NonNull OnTickListener listener);
    void startTicking(boolean resume);
    void stopTicking();
    void forceTick();
    void showAsText(long duration);
}
