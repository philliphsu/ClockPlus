package com.philliphsu.clock2.editalarm;

import android.view.ViewGroup;

/**
 * Created by Phillip Hsu on 7/12/2016.
 *
 * The callback interface used to indicate the user is done filling in
 * the time (they clicked on the 'Set' button).
 */
public interface OnTimeSetListener {
    /**
     * @param viewGroup The view associated with this listener.
     * @param hourOfDay The hour that was set.
     * @param minute The minute that was set.
     */
    void onTimeSet(ViewGroup viewGroup, int hourOfDay, int minute);
}
