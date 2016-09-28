package com.philliphsu.clock2.list;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.philliphsu.clock2.BaseFragment;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.data.BaseItemCursor;
import com.philliphsu.clock2.data.ObjectWithId;

import butterknife.Bind;

/**
 * Created by Phillip Hsu on 7/26/2016.
 */
public abstract class RecyclerViewFragment<
        T extends ObjectWithId,
        VH extends BaseViewHolder<T>,
        C extends BaseItemCursor<T>,
        A extends BaseCursorAdapter<T, VH, C>>
    extends BaseFragment implements
        LoaderManager.LoaderCallbacks<C>,
        OnListItemInteractionListener<T>,
        ScrollHandler {

    public static final String ACTION_SCROLL_TO_STABLE_ID = "com.philliphsu.clock2.list.action.SCROLL_TO_STABLE_ID";
    public static final String EXTRA_SCROLL_TO_STABLE_ID = "com.philliphsu.clock2.list.extra.SCROLL_TO_STABLE_ID";

    private A mAdapter;
    private long mScrollToStableId = RecyclerView.NO_ID;

    // TODO: Rename id to recyclerView?
    // TODO: Rename variable to mRecyclerView?
    @Bind(R.id.list)
    RecyclerView mList;

    @Nullable // Subclasses are not required to use the default content layout, so this may not be present.
    @Bind(R.id.empty_view)
    TextView mEmptyView;

    public abstract void onFabClick();

    /**
     * Callback invoked when we have scrolled to the stable id as set in
     * {@link #setScrollToStableId(long)}.
     * @param id the stable id we have scrolled to
     * @param position the position of the item with this stable id
     */
    protected abstract void onScrolledToStableId(long id, int position);

    /**
     * @return the adapter to set on the RecyclerView. Called in onCreateView().
     */
    protected abstract A onCreateAdapter();

    /**
     * @return a resource to a String that will be displayed when the list is empty
     */
    @StringRes
    protected int emptyMessage() {
        // The reason this isn't abstract is so we don't require subclasses that
        // don't have an empty view to implement this.
        return 0;
    }

    /**
     * @return a resource to a Drawable that will be displayed when the list is empty
     */
    @DrawableRes
    protected int emptyIcon() {
        // The reason this isn't abstract is so we don't require subclasses that
        // don't have an empty view to implement this.
        return 0;
    }

    /**
     * @return whether the list should show an empty view when its adapter has an item count of zero
     */
    protected boolean hasEmptyView() {
        return true;
    }

    /**
     * @return the adapter instance created from {@link #onCreateAdapter()}
     */
    protected final A getAdapter() {
        return mAdapter;
    }

    /**
     * @return the LayoutManager to set on the RecyclerView. The default implementation
     * returns a vertical LinearLayoutManager.
     */
    protected RecyclerView.LayoutManager getLayoutManager() {
        // Called in onCreateView(), so the host activity is alive already.
        return new LinearLayoutManager(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mList.setLayoutManager(getLayoutManager());
        mList.setAdapter(mAdapter = onCreateAdapter());
        if (hasEmptyView() && mEmptyView != null) {
            // Configure the empty view, even if there currently are items.
            mEmptyView.setText(emptyMessage());
            mEmptyView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, emptyIcon(), 0, 0);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // http://stackoverflow.com/a/14632434/5055032
        // A Loader's lifecycle is bound to its Activity, not its Fragment.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onLoadFinished(Loader<C> loader, C data) {
        mAdapter.swapCursor(data);
        if (hasEmptyView() && mEmptyView != null) {
            // TODO: Last I checked after a fresh install, this worked fine.
            // However, previous attempts (without fresh installs) didn't hide the empty view
            // upon an item being added. Verify this is no longer the case.
            mEmptyView.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
        // This may have been a requery due to content change. If the change
        // was an insertion, scroll to the last modified alarm.
        performScrollToStableId(mScrollToStableId);
        mScrollToStableId = RecyclerView.NO_ID;
    }

    @Override
    public void onLoaderReset(Loader<C> loader) {
        mAdapter.swapCursor(null);
    }

    /**
     * @return a layout resource that MUST contain a RecyclerView. The default implementation
     * returns a layout that has just a single RecyclerView in its hierarchy.
     */
    @Override
    protected int contentLayout() {
        return R.layout.fragment_recycler_view;
    }

    @Override
    public void setScrollToStableId(long id) {
        mScrollToStableId = id;
    }

    @Override
    public void scrollToPosition(int position) {
        mList.smoothScrollToPosition(position);
    }

    public final void performScrollToStableId(long stableId) {
        if (stableId != RecyclerView.NO_ID) {
            int position = -1;
            for (int i = 0; i < mAdapter.getItemCount(); i++) {
                if (mAdapter.getItemId(i) == stableId) {
                    position = i;
                    break;
                }
            }
            if (position >= 0) {
                scrollToPosition(position);
                onScrolledToStableId(stableId, position);
            }
        }
    }
}
