package com.philliphsu.clock2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.philliphsu.clock2.model.BaseItemCursor;
import com.philliphsu.clock2.model.ObjectWithId;

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
        OnListItemInteractionListener<T> {

    private A mAdapter;

    // TODO: Rename id to recyclerView?
    // TODO: Rename variable to mRecyclerView?
    @Bind(R.id.list) RecyclerView mList;

    public abstract void onFabClick();

    /**
     * @return the adapter to set on the RecyclerView. SUBCLASSES MUST OVERRIDE THIS, BECAUSE THE
     * DEFAULT IMPLEMENTATION WILL ALWAYS RETURN AN UNINITIALIZED ADAPTER INSTANCE.
     */
    @Nullable
    protected A getAdapter() {
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mList.setLayoutManager(getLayoutManager());
        mList.setAdapter(mAdapter = getAdapter());
        return view;
    }

    @Override
    public void onLoadFinished(Loader<C> loader, C data) {
        mAdapter.swapCursor(data);
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
        // TODO: Rename to fragment_recycler_view
        return R.layout.fragment_alarms;
    }
}
