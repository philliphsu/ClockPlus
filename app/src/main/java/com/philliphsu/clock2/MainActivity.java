package com.philliphsu.clock2;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.philliphsu.clock2.alarms.AlarmsFragment;
import com.philliphsu.clock2.settings.SettingsActivity;
import com.philliphsu.clock2.stopwatch.StopwatchFragment;
import com.philliphsu.clock2.timers.TimersFragment;

import butterknife.Bind;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Drawable mAddItemDrawable;

//    // For delaying fab.show() on SCROLL_STATE_SETTLING
//    private final Handler mHandler = new Handler();
//
//    private boolean mScrollStateDragging;
//    private int mPageDragging = -1; // TOneverDO: initial value >= 0
//    private boolean mDraggingPastEndBoundaries;

    @Bind(R.id.container)
    ViewPager mViewPager;

    @Bind(R.id.fab)
    FloatingActionButton mFab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: On device rotation, if we were last on stopwatch page, restore the fab's translationX.

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            /**
             * @param position Either the current page position if the offset is increasing,
             *                 or the previous page position if it is decreasing.
             * @param positionOffset If increasing from [0, 1), scrolling right and position = currentPagePosition
             *                       If decreasing from (1, 0], scrolling left and position = (currentPagePosition - 1)
             */
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.d(TAG, String.format("pos = %d, posOffset = %f, posOffsetPixels = %d",
//                        position, positionOffset, positionOffsetPixels));
                int pageBeforeLast = mSectionsPagerAdapter.getCount() - 2;
                if (position <= pageBeforeLast) {
                    if (position < pageBeforeLast) {
                        // When the scrolling is due to tab selection between multiple tabs apart,
                        // this callback is called for each intermediate page, but each of those pages
                        // will briefly register a sparsely decreasing range of positionOffsets, always
                        // from (1, 0). As such, you would notice the FAB to jump back and forth between
                        // x-positions as each intermediate page is scrolled through.
                        // This is a visual optimization that ends the translation motion, immediately
                        // returning the FAB to its target position.
                        // TODO: The animation visibly skips to the end. We could interpolate
                        // intermediate x-positions if we cared to smooth it out.
                        mFab.setTranslationX(0);
                    } else {
                        // Initially, the FAB's translationX property is zero because, at its original
                        // position, it is not translated. setTranslationX() is relative to the view's
                        // left position, at its original position; this left position is taken to be
                        // the zero point of the coordinate system relative to this view. As your
                        // translationX value is increasingly negative, the view is translated left.
                        // But as translationX is decreasingly negative and down to zero, the view
                        // is translated right, back to its original position.
                        float translationX = positionOffsetPixels / -2f;
                        // NOTE: You MUST scale your own additional pixel offsets by positionOffset,
                        // or else the FAB will immediately translate by that many pixels, causing
                        // jitter as you scroll.
                        final int margin;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            // Since each side's margin is the same, any side's would do.
                            margin = ((ViewGroup.MarginLayoutParams) mFab.getLayoutParams()).rightMargin;
                        } else {
                            // Pre-Lollipop has measurement issues with FAB margins. This is
                            // probably as good as we can get to centering the FAB, without
                            // hardcoding some small margin value.
                            margin = 0;
                        }
                        // Translation is done relative to a view's left position; by adding
                        // an offset of half the FAB's width, we effectively rebase the translation
                        // relative to the view's center position.
                        translationX += positionOffset * (mFab.getWidth() / 2f + margin);
                        mFab.setTranslationX(translationX);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (position < mSectionsPagerAdapter.getCount() - 1) {
                    mFab.setImageDrawable(mAddItemDrawable);
                }
            }
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                // TODO: This was not sufficient to prevent the user from quickly
//                // hitting the fab for the previous page.
//                switch (state) {
//                    case ViewPager.SCROLL_STATE_DRAGGING:
//                        if (mDraggingPastEndBoundaries) {
//                            return;
//                        }
//                        mScrollStateDragging = true;
//                        mPageDragging = mViewPager.getCurrentItem();
//                        mFab.hide();
//                        break;
//                    case ViewPager.SCROLL_STATE_SETTLING:
//                        if (!mScrollStateDragging) {
//                            mFab.hide();
//                        }
//                        mScrollStateDragging = false;
//                        // getCurrentItem() has changed to the target page we're settling on.
//                        // 200ms is the same as show/hide animation duration
//                        int targetPage = mViewPager.getCurrentItem();
//                        if (targetPage != 2) { // TODO: Use page constant
//                            int delay = mPageDragging == targetPage ? 0 : 200;
//                            mHandler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mFab.show();
//                                }
//                            }, delay);
//                        }
//                        mPageDragging = -1;
//                        break;
//                    case ViewPager.SCROLL_STATE_IDLE:
//                        // Nothing
//                        break;
//                }
//            }
        });
//        mViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
//            @Override
//            public void transformPage(View page, float position) {
//                Log.d(TAG, "position: " + position);
//                // position represents a page's offset from the front-and-center position of 0 (the page
//                // that is in full view). Consider pages A, B, C, D.
//                // If we are now on page A (position 0), then pages B, C, and D are respectively
//                // in positions 1, 2, 3.
//                // If we move to the right to page B (now in position 0), then pages A, C, D are
//                // respectively in positions -1, 1, 2.
//                int currentPage = mViewPager.getCurrentItem();
//                // TODO: Use page constants
//                // Page 0 can't move one full page position to the right (i.e. there is no page to
//                // the left of page 0 that can adopt the front-and-center position of 0 while page 0
//                // moves to adopt position 1)
//                mDraggingPastEndBoundaries = currentPage == 0 && position >= 0f
//                        // The last page can't move one full page position to the left (i.e. there
//                        // is no page to the right of the last page that can adopt the front-and-center
//                        // position of 0 while the last page moves to adopt position -1)
//                        || currentPage == mSectionsPagerAdapter.getCount() - 1 && position <= 0f;
//                Log.d(TAG, "Draggin past end bounds: " + mDraggingPastEndBoundaries);
//            }
//        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        // Using the resources is fine since tab icons will never change once they are set.
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_alarm_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_timer_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_stopwatch_24dp);

        // TODO: @OnCLick instead.
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, EditAlarmActivity.class);
//                // Call Fragment#startActivityForResult() instead of Activity#startActivityForResult()
//                // because we want the result to be handled in the Fragment, not in this Activity.
//                // FragmentActivity does NOT deliver the result to the Fragment, i.e. your
//                // Fragment's onActivityResult() will NOT be called.
//                mSectionsPagerAdapter.getFragment()
//                        .startActivityForResult(intent, AlarmsFragment.REQUEST_CREATE_ALARM);

                Fragment f = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem());
                if (f instanceof RecyclerViewFragment) {
                    ((RecyclerViewFragment) f).onFabClick();
                }
            }
        });

        mAddItemDrawable = ContextCompat.getDrawable(this, R.drawable.ic_add_24dp);
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
            startActivity(new Intent(this, SettingsActivity.class));
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
    private static class SectionsPagerAdapter extends FragmentPagerAdapter {
        // We can't use an ArrayList because the structure reorganizes as elements are removed,
        // so page indices won't stay in sync with list indices. SparseArray allows you to have
        // gaps in your range of indices.
        private final SparseArray<Fragment> mFragments = new SparseArray<>(getCount());

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    return AlarmsFragment.newInstance(1);
                case 1:
                    return new TimersFragment();
                case 2:
                    return new StopwatchFragment();
                default:
                    return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            mFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        // TODO: If you wish to have text labels for your tabs, then implement this method.
//        @Override
//        public CharSequence getPageTitle(int position) {
//            switch (position) {
//                case 0:
//                    return "SECTION 1";
//                case 1:
//                    return "SECTION 2";
//                case 2:
//                    return "SECTION 3";
//            }
//            return null;
//        }

        public Fragment getFragment(int position) {
            return mFragments.get(position);
        }
    }
}
