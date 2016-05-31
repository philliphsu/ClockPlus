package com.philliphsu.clock2;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by Phillip Hsu on 5/31/2016.
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseViewHolder<T>> {

    @Override
    public BaseViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<T> holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
