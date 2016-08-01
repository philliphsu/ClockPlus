package com.philliphsu.clock2;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * Created by Phillip Hsu on 5/31/2016.
 */
public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final Context mContext;
    private final OnListItemInteractionListener<T> mListener;
    private T mItem;

    public BaseViewHolder(ViewGroup parent, @LayoutRes int layoutRes, OnListItemInteractionListener<T> listener) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false));
        ButterKnife.bind(this, itemView);
        mContext = parent.getContext();
        mListener = listener;
        itemView.setOnClickListener(this);
    }

    /**
     * Call to super must be the first line in the overridden implementation,
     * so that the base class can keep a reference to the item parameter.
     */
    @CallSuper
    public void onBind(T item) {
        mItem = item;
    }

    public final Context getContext() {
        return mContext;
    }

    public final T getItem() {
        return mItem;
    }

    @Override
    public final void onClick(View v) {
        if (mListener != null) {
            mListener.onListItemClick(mItem, getAdapterPosition());
        }
    }
}
