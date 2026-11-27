package com.example.medibook.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.utils.StateView;
import com.example.medibook.utils.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** An admin list tab: title, filter chips, optional search, and a board of rows. */
public abstract class AdminListFragment<T> extends TabFragment {
    public static final String ARG_FILTER = "filter";

    protected interface Result<T> {
        void ok(List<T> items);
        void fail(String error);
    }

    private ChipGroup filters;
    private EditText search;
    private TextView count;
    private View board;
    protected StateView state;
    protected List<T> all = new ArrayList<>();
    private String filter;
    private boolean loaded;

    protected abstract int title();

    /** Pairs of {key, label}; the first is the default. */
    protected abstract String[][] filterOptions();

    protected abstract RecyclerView.Adapter<?> createAdapter();

    protected abstract void fetch(Result<T> result);

    protected abstract boolean matches(T item, String filter, String query);

    protected abstract void show(List<T> items);

    protected abstract String emptyText();

    /** Hint for the search field; null hides search. */
    @Nullable
    protected String searchHint() {
        return null;
    }

    /** Label for the header's add button; null hides it. */
    @Nullable
    protected String addLabel() {
        return null;
    }

    protected void onAdd() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saved) {
        return inflater.inflate(R.layout.fragment_admin_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        ((TextView) v.findViewById(R.id.list_title)).setText(title());
        count = v.findViewById(R.id.list_count);
        filters = v.findViewById(R.id.list_filters);
        search = v.findViewById(R.id.list_search);
        board = v.findViewById(R.id.list_board);
        state = new StateView(v.findViewById(R.id.list_state));
        Ui.board((RecyclerView) board.findViewById(R.id.board_list), createAdapter());

        if (searchHint() != null) {
            v.findViewById(R.id.list_search_layout).setVisibility(View.VISIBLE);
            search.setHint(searchHint());
            search.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void afterTextChanged(Editable s) {
                    render();
                }
            });
        }
        if (addLabel() != null) {
            MaterialButton add = v.findViewById(R.id.list_add);
            add.setVisibility(View.VISIBLE);
            add.setText(addLabel());
            add.setOnClickListener(x -> onAdd());
        }

        String[][] options = filterOptions();
        filter = options[0][0];
        for (String[] option : options) {
            Chip chip = new Chip(requireContext());
            chip.setId(View.generateViewId());
            chip.setText(option[1]);
            chip.setTag(option[0]);
            chip.setCheckable(true);
            filters.addView(chip);
        }
        filters.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            filter = (String) group.findViewById(ids.get(0)).getTag();
            render();
        });
        if (getArguments() != null && getArguments().getString(ARG_FILTER) != null) {
            filter = getArguments().getString(ARG_FILTER);
        }
        checkFilterChip();
    }

    @Override
    public void applyArgs(Bundle args) {
        if (args.getString(ARG_FILTER) == null) return;
        filter = args.getString(ARG_FILTER);
        if (filters != null) checkFilterChip();
    }

    private void checkFilterChip() {
        for (int i = 0; i < filters.getChildCount(); i++) {
            Chip chip = (Chip) filters.getChildAt(i);
            if (filter.equals(chip.getTag())) chip.setChecked(true);
        }
    }

    @Override
    protected void load() {
        if (!loaded) state.loading();
        fetch(new Result<T>() {
            @Override
            public void ok(List<T> items) {
                if (!alive()) return;
                loaded = true;
                all = items;
                render();
            }

            @Override
            public void fail(String error) {
                if (!alive()) return;
                board.setVisibility(View.GONE);
                state.error(error, AdminListFragment.this::load);
            }
        });
    }

    protected void render() {
        String q = search.getText() == null ? "" : search.getText().toString().trim().toLowerCase(Locale.US);
        List<T> shown = new ArrayList<>();
        for (T item : all) {
            if (matches(item, filter, q)) shown.add(item);
        }
        show(shown);
        count.setText(shown.size() == all.size() ? String.valueOf(all.size()) : shown.size() + " / " + all.size());
        board.setVisibility(shown.isEmpty() ? View.GONE : View.VISIBLE);
        if (shown.isEmpty() && loaded) {
            state.empty(R.drawable.ic_magnifying_glass, emptyText(), null, null, null);
        } else {
            state.hide();
        }
    }
}
