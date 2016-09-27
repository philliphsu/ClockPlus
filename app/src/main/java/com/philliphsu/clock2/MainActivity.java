package com.philliphsu.clock2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.philliphsu.clock2.alarms.ui.AlarmsFragment;
import com.philliphsu.clock2.list.RecyclerViewFragment;
import com.philliphsu.clock2.settings.SettingsActivity;
import com.philliphsu.clock2.stopwatch.ui.StopwatchFragment;
import com.philliphsu.clock2.timers.ui.TimersFragment;

import butterknife.Bind;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    public static final int    PAGE_ALARMS     = 0;
    public static final int    PAGE_TIMERS     = 1;
    public static final int    PAGE_STOPWATCH  = 2;
    public static final String EXTRA_SHOW_PAGE = "com.philliphsu.clock2.extra.SHOW_PAGE";

    public static final  int    REQUEST_THEME_CHANGE = 5;
    private static final String LAST_TAB             = "last_tab";
    private static final int    DEFAULT_TAB          = 0;

    private ModifyOffsetOnPageChangeListener mModifyFabPositionListener;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Drawable             mAddItemDrawable;

    @Bind(R.id.container)
    ViewPager            mViewPager;
    @Bind(R.id.fab)
    FloatingActionButton mFab;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        // http://stackoverflow.com/a/24035591/5055032
        // http://stackoverflow.com/a/3948036/5055032
        // The views in our layout have begun drawing.
        // There is no lifecycle callback that tells us when our layout finishes drawing;
        // in my own test, drawing still isn't finished by onResume().
        // Post a message in the UI events queue to be executed after drawing is complete,
        // so that we may get their dimensions.
        rootView.post(new Runnable() {
            @Override
            public void run() {
                if (mViewPager.getCurrentItem() == mSectionsPagerAdapter.getCount() - 1) {
                    // Restore the FAB's translationX from a previous configuration.
                    mFab.setTranslationX(mViewPager.getWidth() / -2f + getFabPixelOffsetForXTranslation(mFab));
                }
            }
        });

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        this.mModifyFabPositionListener = new ModifyOffsetOnPageChangeListener(mFab, mSectionsPagerAdapter, mViewPager);

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
                BaseFragment f = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem());
                if (f instanceof RecyclerViewFragment) {
                    ((RecyclerViewFragment) f).onFabClick();
                }
            }
        });

        final int initialPage = getIntent().getIntExtra(EXTRA_SHOW_PAGE, -1);
        if (initialPage >= 0 && initialPage <= mSectionsPagerAdapter.getCount() - 1) {
            // This is so we don't keep it around when the configuration changes.
            getIntent().removeExtra(EXTRA_SHOW_PAGE);
            // Run this only after the ViewPager is finished drawing
            mViewPager.post(new Runnable() {
                @Override
                public void run() {
                    // TOneverDO: smoothScroll == false, or else the onPageScrolled callback won't
                    // be called for the intermediate pages that are responsible for translating
                    // the FAB
                    mViewPager.setCurrentItem(initialPage, true/*smoothScroll*/);
                }
            });
        }
        final String key = getString(R.string.key_last_tab);
        final int fromPrefsLastTab = PreferenceManager.getDefaultSharedPreferences(this).getInt(key, DEFAULT_TAB);
        restoreTab(savedInstanceState, fromPrefsLastTab);
    }

    private void restoreTab(final Bundle savedInstanceState, int def) {
        final int tabToSwitchTo = savedInstanceState == null ?
                def : savedInstanceState.getInt(LAST_TAB, def);
        mViewPager.setCurrentItem(tabToSwitchTo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;
        // If we get here, either this Activity OR one of its hosted Fragments
        // started a requested Activity for a result. The latter case may seem
        // strange; the Fragment is the one starting the requested Activity, so why
        // does the result end up in its host Activity? Shouldn't it end up in
        // Fragment#onActivityResult()? Actually, the Fragment's host Activity gets the
        // first shot at handling the result, before delegating it to the Fragment
        // in Fragment#onActivityResult().
        //
        // There are subtle points to keep in mind when it is actually the Fragment
        // that should handle the result, NOT this Activity. You MUST start
        // the requested Activity with Fragment#startActivityForResult(), NOT
        // Activity#startActivityForResult(). The former calls
        // FragmentActivity#startActivityFromFragment() to implement its behavior.
        // Among other things (not relevant to the discussion),
        // FragmentActivity#startActivityFromFragment() sets internal bit flags
        // that are necessary to achieve the described behavior (that this Activity
        // should delegate the result to the Fragment). Finally, you MUST call
        // through to the super implementation of Activity#onActivityResult(),
        // i.e. FragmentActivity#onActivityResult(). It is this method where
        // the aforementioned internal bit flags will be read to determine
        // which of this Activity's hosted Fragments started the requested
        // Activity.
        //
        // If you are not careful with these points and instead mistakenly call
        // Activity#startActivityForResult(), THEN YOU WILL ONLY BE ABLE TO
        // HANDLE THE REQUEST HERE; the super implementation of onActivityResult()
        // will not delegate the result to the Fragment, because the requisite
        // internal bit flags are not set with Activity#startActivityForResult().
        //
        // Further reading:
        // http://stackoverflow.com/q/6147884/5055032
        // http://stackoverflow.com/a/24303360/5055032
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_THEME_CHANGE
                && data != null && data.getBooleanExtra(SettingsActivity.EXTRA_THEME_CHANGED, false)) {
            recreate();
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState, final PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(LAST_TAB, mViewPager.getCurrentItem());
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreTab(savedInstanceState, DEFAULT_TAB);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewPager.addOnPageChangeListener(mModifyFabPositionListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mViewPager.removeOnPageChangeListener(mModifyFabPositionListener);
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onDestroy() {
        final int currentTab = mViewPager.getCurrentItem();
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putInt(getString(R.string.key_last_tab), currentTab)
                .commit();

        super.onDestroy();
        this.mModifyFabPositionListener = null;
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
            startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_THEME_CHANGE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * @return the positive offset in pixels required to rebase an X-translation of the FAB
     * relative to its center position. An X-translation normally is done relative to a view's
     * left position.
     */
    private static float getFabPixelOffsetForXTranslation(FloatingActionButton fab) {
        final int margin;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Since each side's margin is the same, any side's would do.
            margin = ((ViewGroup.MarginLayoutParams) fab.getLayoutParams()).rightMargin;
        } else {
            // Pre-Lollipop has measurement issues with FAB margins. This is
            // probably as good as we can get to centering the FAB, without
            // hardcoding some small margin value.
            margin = 0;
        }
        // X-translation is done relative to a view's left position; by adding
        // an offset of half the FAB's width, we effectively rebase the translation
        // relative to the view's center position.
        return fab.getWidth() / 2f + margin;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private static class SectionsPagerAdapter extends FragmentPagerAdapter {
        // We can't use an ArrayList because the structure reorganizes as elements are removed,
        // so page indices won't stay in sync with list indices. SparseArray allows you to have
        // gaps in your range of indices.
        private final SparseArray<BaseFragment> mFragments = new SparseArray<>(getCount());

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public BaseFragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
            case PAGE_ALARMS:
                return AlarmsFragment.newInstance(1);
            case PAGE_TIMERS:
                return new TimersFragment();
            case PAGE_STOPWATCH:
                return new StopwatchFragment();
            default:
                throw new IllegalStateException("No fragment can be instantiated for position " + position);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            BaseFragment fragment = (BaseFragment) super.instantiateItem(container, position);
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

        BaseFragment getFragment(int position) {
            return mFragments.get(position);
        }
    }

    private static final class ModifyOffsetOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        private final FloatingActionButton mFab;
        private final SectionsPagerAdapter mSectionsPagerAdapter;
        private final ViewPager            mViewPager;

        private ModifyOffsetOnPageChangeListener(final FloatingActionButton mFab,
                                                 final SectionsPagerAdapter mSectionsPagerAdapter,
                                                 final ViewPager mViewPager) {
            this.mFab = mFab;
            this.mSectionsPagerAdapter = mSectionsPagerAdapter;
            this.mViewPager = mViewPager;
        }

        /**
         * @param position       Either the current page position if the offset is increasing,
         *                       or the previous page position if it is decreasing.
         * @param positionOffset If increasing from [0, 1), scrolling right and position = currentPagePosition
         *                       If decreasing from (1, 0], scrolling left and position = (currentPagePosition - 1)
         */
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.d(TAG, String.format("pos = %d, posOffset = %f, posOffsetPixels = %d",
                    position, positionOffset, positionOffsetPixels));
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
                    // or else the FAB will immediately translate by that many pixels, appearing
                    // to skip/jump.
                    translationX += positionOffset * getFabPixelOffsetForXTranslation(mFab);
                    mFab.setTranslationX(translationX);
                }
            }
        }

        @Override
        public void onPageSelected(int position) {
            Log.d(TAG, "onPageSelected");
            if (position < mSectionsPagerAdapter.getCount() - 1) {
                mFab.setImageResource(R.drawable.ic_add_24dp);
            }
            BaseFragment f = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem());
            // NOTE: This callback is fired after a rotation, right after onStart().
            // Unfortunately, the FragmentManager handling the rotation has yet to
            // tell our adapter to re-instantiate the Fragments, so our collection
            // of fragments is empty. You MUST keep this check so we don't cause a NPE.
            if (f != null) {
                f.onPageSelected();
            }
        }
    }
}
