package com.example.medibook.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.example.medibook.MainActivity;

/** A tab inside MainActivity. Reloads whenever it becomes visible or is reselected. */
public abstract class TabFragment extends Fragment {

    /** Fetch and render this tab's data. Called every time the tab is shown. */
    protected abstract void load();

    /** A search or filter handed over by another tab. */
    public void applyArgs(Bundle args) {}

    public void onReselected() {
        load();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) load();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && isResumed()) load();
    }

    /** False once the view is gone; async callbacks check it before touching views. */
    protected boolean alive() {
        return isAdded() && getView() != null;
    }

    protected MainActivity shell() {
        return (MainActivity) requireActivity();
    }
}
