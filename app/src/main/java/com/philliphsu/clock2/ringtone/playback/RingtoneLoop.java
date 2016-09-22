package com.philliphsu.clock2.ringtone.playback;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

/**
 * Created by Phillip Hsu on 9/5/2016.
 *
 * A MediaPlayer configured to play a ringtone in a loop.
 */
public final class RingtoneLoop {

    private final Context mContext;
    private final AudioManager mAudioManager;
    private final Uri mUri;

    private MediaPlayer mMediaPlayer;

    public RingtoneLoop(Context context, Uri uri) {
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mUri = uri;
    }

    public void play() {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(mContext, mUri);
            if (mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                // "Must call this method before prepare() or prepareAsync() in order
                // for the target stream type to become effective thereafter."
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setLooping(true);
                // There is prepare() and prepareAsync().
                // "For files, it is OK to call prepare(), which blocks until
                // MediaPlayer is ready for playback."
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (SecurityException | IOException e) {
            destroyLocalPlayer();
        }
    }

    public void stop() {
        if (mMediaPlayer != null) {
            destroyLocalPlayer();
        }
    }

    private void destroyLocalPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

}
