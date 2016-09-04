package com.philliphsu.clock2.alarms;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ToggleButton;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.DaysOfWeek;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.RingtonePickerDialog;
import com.philliphsu.clock2.aospdatetimepicker.Utils;
import com.philliphsu.clock2.util.AlarmController;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Phillip Hsu on 7/31/2016.
 */
public class ExpandedAlarmViewHolder extends BaseAlarmViewHolder {
    private static final String TAG = "ExpandedAlarmViewHolder";
    private static final String TAG_RINGTONE_PICKER = "ringtone_picker";

    @Bind(R.id.ok) Button mOk;
    @Bind(R.id.delete) Button mDelete;
    @Bind(R.id.ringtone) Button mRingtone;
    @Bind(R.id.vibrate) CheckBox mVibrate;
    @Bind({R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6})
    ToggleButton[] mDays;

    private final ColorStateList mDayToggleColors;

    public ExpandedAlarmViewHolder(ViewGroup parent, final OnListItemInteractionListener<Alarm> listener,
                                   AlarmController controller) {
        super(parent, R.layout.item_expanded_alarm, listener, controller);
        // Manually bind listeners, or else you'd need to write a getter for the
        // OnListItemInteractionListener in the BaseViewHolder for use in method binding.
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onListItemDeleted(getAlarm());
            }
        });
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Since changes are persisted as soon as they are made, there's really
                // nothing we have to persist here. Let the listener know we should
                // collapse this VH.
                // While this works, it also makes an update to the DB and thus reschedules
                // the alarm, so the snackbar will show up as well. We want to avoid that..
//                listener.onListItemUpdate(getAlarm(), getAdapterPosition());
                // TODO: This only works because we know what the implementation looks like..
                // This is bad because we just made the proper function of this dependent
                // on the implementation.
                listener.onListItemClick(getAlarm(), getAdapterPosition());
            }
        });

        // https://code.google.com/p/android/issues/detail?id=177282
        // https://stackoverflow.com/questions/15673449/is-it-confirmed-that-i-cannot-use-themed-color-attribute-in-color-state-list-res
        // Programmatically create the ColorStateList for our day toggles using themed color
        // attributes, "since prior to M you can't create a themed ColorStateList from XML but you
        // can from code." (quote from google)
        // The first array level is analogous to an XML node defining an item with a state list.
        // The second level lists all the states considered by the item from the first level.
        // An empty list of states represents the default stateless item.
        int[][] states = {
                /*item 1*/{/*states*/android.R.attr.state_checked},
                /*item 2*/{/*states*/}
        };
        // TODO: Phase out Utils.getColorFromThemeAttr because it doesn't work for text colors.
        // WHereas getTextColorFromThemeAttr works for both regular colors and text colors.
        int[] colors = {
                /*item 1*/Utils.getTextColorFromThemeAttr(getContext(), R.attr.colorAccent),
                /*item 2*/Utils.getTextColorFromThemeAttr(getContext(), android.R.attr.textColorHint)
        };
        mDayToggleColors = new ColorStateList(states, colors);

        RingtonePickerDialog picker = (RingtonePickerDialog)
                mFragmentManager.findFragmentByTag(TAG_RINGTONE_PICKER);
        if (picker != null) {
            Log.i(TAG, "Restoring ringtone picker callback");
            picker.setOnRingtoneSelectedListener(newOnRingtoneSelectedListener());
        }
    }

    @Override
    public void onBind(Alarm alarm) {
        super.onBind(alarm);
        bindDays(alarm);
        bindRingtone();
        bindVibrate(alarm.vibrates());
    }

    @Override
    protected void bindLabel(boolean visible, String label) {
        super.bindLabel(true, label);
    }

    @OnClick(R.id.ok)
    void save() {
        // TODO
    }

//    @OnClick(R.id.delete)
//    void delete() {
//        // TODO
//    }

    @OnClick(R.id.ringtone)
    void showRingtonePickerDialog() {
//        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
//        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
//                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
//                // The ringtone to show as selected when the dialog is opened
//                .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, getSelectedRingtoneUri())
//                // Whether to show "Default" item in the list
//                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
//        // The ringtone that plays when default option is selected
//        //.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, DEFAULT_TONE);
//        // TODO: This is VERY BAD. Use a Controller/Presenter instead.
//        // The result will be delivered to MainActivity, and then delegated to AlarmsFragment.
//        ((Activity) getContext()).startActivityForResult(intent, AlarmsFragment.REQUEST_PICK_RINGTONE);
        RingtonePickerDialog dialog = RingtonePickerDialog.newInstance(
                newOnRingtoneSelectedListener(), getSelectedRingtoneUri());
        dialog.show(mFragmentManager, TAG_RINGTONE_PICKER);
    }

    @OnClick(R.id.vibrate)
    void onVibrateToggled() {
        final Alarm oldAlarm = getAlarm();
        Alarm newAlarm = oldAlarm.toBuilder()
                .vibrates(mVibrate.isChecked())
                .build();
        oldAlarm.copyMutableFieldsTo(newAlarm);
        mAlarmController.save(newAlarm);
    }

    @OnClick({ R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6 })
    void onDayToggled(ToggleButton view) {
        final Alarm oldAlarm = getAlarm();
        Alarm newAlarm = oldAlarm.toBuilder().build();
        oldAlarm.copyMutableFieldsTo(newAlarm);
        // ---------------------------------------------------------------------------------
        // TOneverDO: precede copyMutableFieldsTo()
        int position = ((ViewGroup) view.getParent()).indexOfChild(view);
        int weekDayAtPosition = DaysOfWeek.getInstance(getContext()).weekDayAt(position);
        Log.d(TAG, "Day toggle #" + position + " checked changed. This is weekday #"
                + weekDayAtPosition + " relative to a week starting on Sunday");
        newAlarm.setRecurring(weekDayAtPosition, view.isChecked());
        // ---------------------------------------------------------------------------------
        mAlarmController.save(newAlarm);
    }

    private void bindDays(Alarm alarm) {
        for (int i = 0; i < mDays.length; i++) {
            mDays[i].setTextColor(mDayToggleColors);
            int weekDay = DaysOfWeek.getInstance(getContext()).weekDayAt(i);
            String label = DaysOfWeek.getLabel(weekDay);
            mDays[i].setTextOn(label);
            mDays[i].setTextOff(label);
            mDays[i].setChecked(alarm.isRecurring(weekDay));
        }
    }

    private void bindRingtone() {
        int iconTint = Utils.getTextColorFromThemeAttr(getContext(), R.attr.themedIconTint);

        Drawable ringtoneIcon = mRingtone.getCompoundDrawablesRelative()[0/*start*/];
        ringtoneIcon = DrawableCompat.wrap(ringtoneIcon.mutate());
        DrawableCompat.setTint(ringtoneIcon, iconTint);
        mRingtone.setCompoundDrawablesRelativeWithIntrinsicBounds(ringtoneIcon, null, null, null);

        String title = RingtoneManager.getRingtone(getContext(),
                getSelectedRingtoneUri()).getTitle(getContext());
        mRingtone.setText(title);
    }

    private void bindVibrate(boolean vibrates) {
        mVibrate.setChecked(vibrates);
    }

    private Uri getSelectedRingtoneUri() {
        // If showing an item for "Default" (@see EXTRA_RINGTONE_SHOW_DEFAULT), this can be one
        // of DEFAULT_RINGTONE_URI, DEFAULT_NOTIFICATION_URI, or DEFAULT_ALARM_ALERT_URI to have the
        // "Default" item checked.
        //
        // Otherwise, use RingtoneManager.getActualDefaultRingtoneUri() to get the "actual sound URI".
        //
        // Do not use RingtoneManager.getDefaultUri(), because that just returns one of
        // DEFAULT_RINGTONE_URI, DEFAULT_NOTIFICATION_URI, or DEFAULT_ALARM_ALERT_URI
        // depending on the type requested (i.e. what the docs calls "symbolic URI
        // which will resolved to the actual sound when played").
        String ringtone = getAlarm().ringtone();
        return ringtone.isEmpty() ?
                RingtoneManager.getActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_ALARM)
                : Uri.parse(ringtone);
    }

    private RingtonePickerDialog.OnRingtoneSelectedListener newOnRingtoneSelectedListener() {
        return new RingtonePickerDialog.OnRingtoneSelectedListener() {
            @Override
            public void onRingtoneSelected(Uri ringtoneUri) {
                Log.d(TAG, "Selected ringtone: " + ringtoneUri.toString());
                final Alarm oldAlarm = getAlarm();
                Alarm newAlarm = oldAlarm.toBuilder()
                        .ringtone(ringtoneUri.toString())
                        .build();
                oldAlarm.copyMutableFieldsTo(newAlarm);
                mAlarmController.save(newAlarm);
            }
        };
    }
}
