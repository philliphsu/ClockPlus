package com.philliphsu.clock2.alarms.ui;

import android.content.Context;
import android.support.design.widget.CheckableImageButton;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;

/**
 * Created by Phillip Hsu on 9/26/2016.
 *
 * Temporary fix for design support library's CheckableImageButton that toggles itself when clicked.
 */
public class TempCheckableImageButton extends CheckableImageButton {

    public TempCheckableImageButton(Context context) {
        super(context);
    }

    public TempCheckableImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TempCheckableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override // borrowed from CompoundButton#performClick()
    public boolean performClick() {
        toggle();
        final boolean handled = super.performClick();
        if (!handled) {
            // View only makes a sound effect if the onClickListener was
            // called, so we'll need to make one here instead.
            playSoundEffect(SoundEffectConstants.CLICK);
        }
        return handled;
    }

}
