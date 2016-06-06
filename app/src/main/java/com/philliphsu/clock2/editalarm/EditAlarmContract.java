package com.philliphsu.clock2.editalarm;

/**
 * Created by Phillip Hsu on 6/2/2016.
 */
public interface EditAlarmContract {

    interface View extends AlarmContract.View {
        void showTime(int hour, int minutes);
        void showRecurringDays(int weekDay, boolean recurs);
        void showRingtone(String ringtone);
        void showVibrates(boolean vibrates);
        void showEditorClosed();
        void showNumpad(boolean show);
        void showRingtonePickerDialog();
        void setTimeTextHint();
        void showTimeText(String timeText);
        void showTimeTextPostBackspace(String newStr);
        void showTimeTextFocused(boolean focused);
        int getHour();
        int getMinutes();
        boolean isEnabled();
        boolean isRecurringDay(int weekDay);
        String getLabel();
        String getRingtone();
        boolean vibrates();
    }

    interface Presenter extends AlarmContract.Presenter {
        void loadAlarm(long id);
        void save();
        void delete();
        void showNumpad();
        void hideNumpad();
        // not sure
        void onBackspace(String newStr);
        void acceptNumpadChanges();
        void onPrepareOptionsMenu();
        void openRingtonePickerDialog();
        void setTimeTextHint();
        void onNumberInput(String formattedInput);
        void focusTimeText();
    }
}
