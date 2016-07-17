package com.philliphsu.clock2.editalarm;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.philliphsu.clock2.R;

/**
 * Created by Phillip Hsu on 7/17/2016.
 */
public class ScrollingGridAdapter extends RecyclerView.Adapter<ScrollingGridAdapter.ViewHolder> {

    private String[] mValues;
    private View.OnClickListener mOnClickListener;

    /**
     * @param values the values to display
     * @param listener a click listener so clients can be notified of when the View corresponding
     *                 to a value was clicked, so they may call notifyDataSetChanged() if need be
     */
    public ScrollingGridAdapter(String[] values, View.OnClickListener listener) {
        mValues = values;
        mOnClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scrolling_grid_value, parent, false);
        return new ViewHolder(view, mOnClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TextView tv = (TextView) holder.itemView;
        tv.setText(mValues[position]);
    }

    @Override
    public int getItemCount() {
        return mValues == null ? 0 : mValues.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView, View.OnClickListener listener) {
            super(itemView);
            itemView.setOnClickListener(listener);
        }
    }

}
