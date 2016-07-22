package com.philliphsu.clock2.editalarm;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.philliphsu.clock2.R;

/**
 * Created by Phillip Hsu on 7/21/2016.
 *
 * AppCompat-themed AlertDialog.
 */
public class NumberGridTimePickerDialog extends DialogFragment {

    public static NumberGridTimePickerDialog newInstance() {
        return new NumberGridTimePickerDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use an AlertDialog to display footer buttons, rather than
        // re-invent them in our layout.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.dialog_time_picker_number_grid)
                // The action strings are already defined and localized by the system!
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
//        return super.onCreateDialog(savedInstanceState);
    }


}
