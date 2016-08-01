package com.philliphsu.clock2.alarms;

import android.media.RingtoneManager;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ToggleButton;

import com.philliphsu.clock2.Alarm;
import com.philliphsu.clock2.DaysOfWeek;
import com.philliphsu.clock2.OnListItemInteractionListener;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.util.AlarmController;

import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

/**
 * Created by Phillip Hsu on 7/31/2016.
 */
public class ExpandedAlarmViewHolder extends BaseAlarmViewHolder {

    @Bind(R.id.save) Button mSave;
    @Bind(R.id.delete) Button mDelete;
    @Bind(R.id.ringtone) Button mRingtone;
    @Bind(R.id.vibrate) CheckBox mVibrate;
    @Bind({R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6})
    ToggleButton[] mDays;

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
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onListItemUpdate(getAlarm(), getAdapterPosition());
            }
        });
    }

    @Override
    public void onBind(Alarm alarm) {
        super.onBind(alarm);
        bindDays(alarm);
        bindRingtone(alarm.ringtone());
        bindVibrate(alarm.vibrates());
    }

    @Override
    protected void bindLabel(boolean visible, String label) {
        super.bindLabel(true, label);
    }

    @OnClick(R.id.save)
    void save() {
        // TODO
    }

//    @OnClick(R.id.delete)
//    void delete() {
//        // TODO
//    }

    @OnClick(R.id.ringtone)
    void showRingtonePickerDialog() {
        // TODO
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // We didn't have to write code like this in EditAlarmActivity, because we never committed
    // any changes until the user explicitly clicked save. We have to do this here now because
    // we should commit changes as they are made.
    @OnCheckedChanged(R.id.vibrate)
    void onVibrateToggled() {
        // TODO
    }

    @OnCheckedChanged({ R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6 })
    void onDayToggled() {
        // TODO
    }
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void bindDays(Alarm alarm) {
        for (int i = 0; i < mDays.length; i++) {
            int weekDay = DaysOfWeek.getInstance(getContext()).weekDayAt(i);
            String label = DaysOfWeek.getLabel(weekDay);
            mDays[i].setTextOn(label);
            mDays[i].setTextOff(label);
            mDays[i].setChecked(alarm.isRecurring(weekDay));
        }
    }

    private void bindRingtone(String ringtone) {
        // Initializing to Settings.System.DEFAULT_ALARM_ALERT_URI will show
        // "Default ringtone (Name)" on the button text, and won't show the
        // selection on the dialog when first opened. (unless you choose to show
        // the default item in the intent extra?)
        // Compare with getDefaultUri(int), which returns the symbolic URI instead of the
        // actual sound URI. For TYPE_ALARM, this actually returns the same constant.
        Uri mSelectedRingtoneUri; // TODO: This was actually an instance variable in EditAlarmActivity.
        if (null == ringtone || ringtone.isEmpty()) {
            mSelectedRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(
                    getContext(), RingtoneManager.TYPE_ALARM);
        } else {
            mSelectedRingtoneUri = Uri.parse(ringtone);
        }
        String title = RingtoneManager.getRingtone(getContext(),
                mSelectedRingtoneUri).getTitle(getContext());
        mRingtone.setText(title);
    }

    private void bindVibrate(boolean vibrates) {
        mVibrate.setChecked(vibrates);
    }
}
