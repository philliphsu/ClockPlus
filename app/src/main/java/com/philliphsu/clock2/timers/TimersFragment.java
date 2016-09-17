package com.philliphsu.clock2.timers;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philliphsu.clock2.AsyncTimersTableUpdateHandler;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.RecyclerViewFragment;
import com.philliphsu.clock2.Timer;
import com.philliphsu.clock2.edittimer.EditTimerActivity;
import com.philliphsu.clock2.model.TimerCursor;
import com.philliphsu.clock2.model.TimersListCursorLoader;

import static butterknife.ButterKnife.findById;
import static com.philliphsu.clock2.util.ConfigurationUtils.getOrientation;

public class TimersFragment extends RecyclerViewFragment<
        Timer,
        TimerViewHolder,
        TimerCursor,
        TimersCursorAdapter> {
    // TODO: Different number of columns for different display densities, instead of landscape.
    // Use smallest width qualifiers. I can imagine 3 or 4 columns for a large enough tablet in landscape.
    private static final int LANDSCAPE_LAYOUT_COLUMNS = 2;

    public static final int REQUEST_CREATE_TIMER = 0;
    public static final String EXTRA_SCROLL_TO_TIMER_ID = "com.philliphsu.clock2.timers.extra.SCROLL_TO_TIMER_ID";

    private AsyncTimersTableUpdateHandler mAsyncTimersTableUpdateHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAsyncTimersTableUpdateHandler = new AsyncTimersTableUpdateHandler(getActivity(), this);

        // TimerNotificationService was supposed to put this extra in its content intent.
        // Currently, it does not implement this feature. May be left for a future release?
        long scrollToStableId = getActivity().getIntent().getLongExtra(EXTRA_SCROLL_TO_TIMER_ID, -1);
        if (scrollToStableId != -1) {
            setScrollToStableId(scrollToStableId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        final Resources r = getResources();
        if (getOrientation(r) == Configuration.ORIENTATION_LANDSCAPE) {
            RecyclerView list = findById(view, R.id.list);
            int cardViewMargin = r.getDimensionPixelSize(R.dimen.cardview_margin);
            list.setPaddingRelative(cardViewMargin/*start*/, cardViewMargin/*top*/, 0, list.getPaddingBottom());
        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null)
            return;
        // TODO: From EditTimerActivity, pass back the Timer as a parcelable and
        // retrieve it here directly.
        int hour = data.getIntExtra(EditTimerActivity.EXTRA_HOUR, -1);
        int minute = data.getIntExtra(EditTimerActivity.EXTRA_MINUTE, -1);
        int second = data.getIntExtra(EditTimerActivity.EXTRA_SECOND, -1);
        String label = data.getStringExtra(EditTimerActivity.EXTRA_LABEL);
        boolean startTimer = data.getBooleanExtra(EditTimerActivity.EXTRA_START_TIMER, false);
        // TODO: Timer's group?

        Timer t = Timer.createWithLabel(hour, minute, second, label);
        if (startTimer) {
            t.start();
        }
        mAsyncTimersTableUpdateHandler.asyncInsert(t);
    }

    @Override
    public void onFabClick() {
        Intent intent = new Intent(getActivity(), EditTimerActivity.class);
        startActivityForResult(intent, REQUEST_CREATE_TIMER);
    }

    @Override
    protected TimersCursorAdapter onCreateAdapter() {
        // Create a new adapter. This is called before we can initialize mAsyncTimersTableUpdateHandler,
        // so right now it is null. However, after super.onCreate() returns, it is initialized, and
        // the reference variable will be pointing to an actual object. This assignment "propagates"
        // to all references to mAsyncTimersTableUpdateHandler.
        return new TimersCursorAdapter(this, mAsyncTimersTableUpdateHandler);
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        switch (getOrientation(getResources())) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return new GridLayoutManager(getActivity(), LANDSCAPE_LAYOUT_COLUMNS);
            default:
                return super.getLayoutManager();
        }
    }

    @Override
    protected int emptyMessage() {
        return R.string.empty_timers_container;
    }

    @Override
    public Loader<TimerCursor> onCreateLoader(int id, Bundle args) {
        return new TimersListCursorLoader(getActivity());
    }

    @Override
    public void onListItemClick(Timer item, int position) {

    }

    @Override
    public void onListItemDeleted(Timer item) {

    }

    @Override
    public void onListItemUpdate(Timer item, int position) {

    }

    @Override
    protected void onScrolledToStableId(long id, int position) {

    }
}
