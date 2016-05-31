package com.philliphsu.clock2;

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

    private final OnClickListener<T> mOnClickListener;
    private T mItem;

    public BaseViewHolder(ViewGroup parent, @LayoutRes int layoutRes, OnClickListener<T> listener) {
        super(LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false));
        ButterKnife.bind(this, itemView);
        mOnClickListener = listener;
    }

    @CallSuper
    public void onBind(T item) {
        mItem = item;
    }

    @Override
    public final void onClick(View v) {
        mOnClickListener.onClick(mItem);
    }

    public interface OnClickListener<T> {
        void onClick(T item);
    }
}
