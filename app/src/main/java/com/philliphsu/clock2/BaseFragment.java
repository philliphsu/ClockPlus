package com.philliphsu.clock2;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * Created by Phillip Hsu on 6/30/2016.
 */
public abstract class BaseFragment extends Fragment {
    /**
     * Required empty public constructor. Subclasses do not
     * need to implement their own.
     */
    public BaseFragment() {}

    /**
     * @return the layout resource for this Fragment
     */
    @LayoutRes
    protected abstract int contentLayout();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(contentLayout(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this); // Only for fragments!
    }

    /**
     * Callback invoked when this Fragment is part of a ViewPager and it has been
     * selected, as indicated by {@link android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)
     * onPageSelected(int)}.
     */
    public void onPageSelected() {
        // TODO: Consider making this abstract. The reason it wasn't abstract in the first place
        // is not all Fragments in our ViewPager need to do things upon being selected. As such,
        // those Fragments' classes would just end up stubbing this implementation.
    }
}
