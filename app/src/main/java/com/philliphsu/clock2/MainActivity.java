package com.philliphsu.clock2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.philliphsu.clock2.alarms.AlarmsFragment;
import com.philliphsu.clock2.editalarm.EditAlarmActivity;
import com.philliphsu.clock2.ringtone.RingtoneActivity;

public class MainActivity extends BaseActivity implements AlarmsFragment.OnAlarmInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEditAlarmActivity(-1);
                /*
                scheduleAlarm();
                Snackbar.make(view, "Alarm set for 1 minute from now", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                                PendingIntent pi = alarmIntent(true);
                                am.cancel(pi);
                                pi.cancel();
                                Intent intent = new Intent(MainActivity.this, UpcomingAlarmReceiver.class)
                                        .setAction(UpcomingAlarmReceiver.ACTION_CANCEL_NOTIFICATION);
                                sendBroadcast(intent);
                            }
                        }).show();
                        */
            }
        });
    }



    @Override
    protected int layoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected int menuResId() {
        return R.menu.menu_main;
    }

    @Override
    protected boolean isDisplayHomeUpEnabled() {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            return AlarmsFragment.newInstance(1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }

    @Override
    public void onListItemInteraction(Alarm item) {
        startEditAlarmActivity(item.id());
    }

    private void startEditAlarmActivity(long alarmId) {
        Intent intent = new Intent(this, EditAlarmActivity.class);
        intent.putExtra(EditAlarmActivity.EXTRA_ALARM_ID, alarmId);
        startActivity(intent);
    }

    private void scheduleAlarm() {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        // If there is already an alarm for this Intent scheduled (with the equality of two
        // intents being defined by filterEquals(Intent)), then it will be removed and replaced
        // by this one. For most of our uses, the relevant criteria for equality will be the
        // action, the data, and the class (component). Although not documented, the request code
        // of a PendingIntent is also considered to determine equality of two intents.

        // WAKEUP alarm types wake the CPU up, but NOT the screen. If that is what you want, you need
        // to handle that yourself by using a wakelock, etc..
        // We use a WAKEUP alarm to send the upcoming alarm notification so it goes off even if the
        // device is asleep. Otherwise, it will not go off until the device is turned back on.
        // todo: use alarm's ring time - (number of hours to be notified in advance, converted to millis)
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), notifyUpcomingAlarmIntent());
        // todo: get alarm's ring time
        am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, alarmIntent(false));
    }

    private static int alarmCount;

    private PendingIntent alarmIntent(boolean retrievePrevious) {
        // TODO: Use appropriate subclass instead
        Intent intent = new Intent(this, RingtoneActivity.class)
                .setData(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        // TODO: Pass in the id of the alarm to the intent. Alternatively, if the upcoming alarm note
        // only needs to show the alarm's ring time, just pass in the alarm's ringsAt().
        // TODO: Use unique request codes per alarm.
        // If a PendingIntent with this request code already exists, then we are likely modifying
        // an alarm, so we should cancel the existing intent.
        int requestCode = retrievePrevious ? alarmCount - 1 : alarmCount++;
        int flag = retrievePrevious
                ? PendingIntent.FLAG_NO_CREATE
                : PendingIntent.FLAG_CANCEL_CURRENT;
        return PendingIntent.getActivity(this, requestCode, intent, flag);
    }

    private PendingIntent notifyUpcomingAlarmIntent() {
        Intent intent = new Intent(this, UpcomingAlarmReceiver.class);
        // TODO: Use unique request codes per alarm.
        return PendingIntent.getBroadcast(this, alarmCount, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
