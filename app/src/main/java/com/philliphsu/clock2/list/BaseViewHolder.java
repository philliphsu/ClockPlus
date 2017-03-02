/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.philliphsu.clock2.list;

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
