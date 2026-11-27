package com.example.medibook.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.activities.user.BookAppointmentActivity;
import com.example.medibook.activities.user.DoctorProfileActivity;
import com.example.medibook.adapters.DoctorAdapter;
import com.example.medibook.models.Doctor;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.utils.StateView;
import com.example.medibook.utils.Ui;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/** Patients search verified doctors by name, specialty or hospital and filter by specialty. */
public class DoctorsFragment extends TabFragment {
    public static final String ARG_SPECIALTY = "specialty";
    public static final String ARG_FOCUS_SEARCH = "focus_search";

    private EditText search;
    private ChipGroup filters;
    private TextView count;
    private View board;
    private StateView state;
    private DoctorAdapter adapter;
    private List<Doctor> all = new ArrayList<>();
    /** Specialty to select once chips exist; null means "All". */
    private String specialty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saved) {
        return inflater.inflate(R.layout.fragment_doctors, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        search = v.findViewById(R.id.doctors_search);
        filters = v.findViewById(R.id.doctors_filters);
        count = v.findViewById(R.id.doctors_count);
        board = v.findViewById(R.id.doctors_board);
        state = new StateView(v.findViewById(R.id.doctors_state));
        adapter = new DoctorAdapter(new DoctorAdapter.Listener() {
            @Override
            public void onOpen(Doctor doctor) {
                startActivity(new Intent(requireContext(), DoctorProfileActivity.class)
                        .putExtra(DoctorProfileActivity.EXTRA_DOCTOR_ID, doctor.getDoctorId()));
            }

            @Override
            public void onBook(Doctor doctor) {
                startActivity(new Intent(requireContext(), BookAppointmentActivity.class)
                        .putExtra(BookAppointmentActivity.EXTRA_DOCTOR_ID, doctor.getDoctorId()));
            }
        });
        Ui.board((RecyclerView) board.findViewById(R.id.board_list), adapter);

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                render();
            }
        });
        search.setOnEditorActionListener((x, actionId, e) -> {
            hideKeyboard();
            return true;
        });
        filters.setOnCheckedStateChangeListener((group, ids) -> {
            Chip chip = ids.isEmpty() ? null : group.findViewById(ids.get(0));
            specialty = chip == null || chip.getTag() == null ? null : (String) chip.getTag();
            render();
        });
        if (getArguments() != null) applyArgs(getArguments());
    }

    @Override
    public void applyArgs(Bundle args) {
        specialty = args.getString(ARG_SPECIALTY);
        if (search == null) return; // view not created yet; onViewCreated applies the arguments
        search.setText("");
        selectChip();
        if (args.getBoolean(ARG_FOCUS_SEARCH)) {
            search.requestFocus();
            search.post(() -> ((InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(search, InputMethodManager.SHOW_IMPLICIT));
        }
    }

    @Override
    protected void load() {
        if (all.isEmpty()) state.loading();
        new DoctorRepository().getAllDoctors(new DoctorRepository.DoctorsCallback() {
            @Override
            public void onSuccess(List<Doctor> doctors) {
                if (!alive()) return;
                state.hide();
                all = doctors;
                Collections.sort(all, (a, b) -> Ui.orDash(a.getName()).compareToIgnoreCase(Ui.orDash(b.getName())));
                buildChips();
                render();
            }

            @Override
            public void onFailure(String error) {
                if (!alive()) return;
                board.setVisibility(View.GONE);
                state.error(error, DoctorsFragment.this::load);
            }
        });
    }

    private void buildChips() {
        TreeSet<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Doctor d : all) {
            if (!Ui.blank(d.getSpecialty())) names.add(d.getSpecialty().trim());
        }
        filters.removeAllViews();
        addChip(getString(R.string.all_specialties), null);
        for (String name : names) addChip(name, name);
        selectChip();
    }

    private void addChip(String label, @Nullable String value) {
        Chip chip = new Chip(requireContext());
        chip.setId(View.generateViewId());
        chip.setText(label);
        chip.setTag(value);
        chip.setCheckable(true);
        filters.addView(chip);
    }

    private void selectChip() {
        for (int i = 0; i < filters.getChildCount(); i++) {
            Chip chip = (Chip) filters.getChildAt(i);
            Object tag = chip.getTag();
            boolean match = specialty == null ? tag == null : tag != null && same((String) tag, specialty);
            if (match) {
                chip.setChecked(true);
                return;
            }
        }
        if (filters.getChildCount() > 0) ((Chip) filters.getChildAt(0)).setChecked(true);
    }

    private void render() {
        if (adapter == null) return;
        String q = search.getText() == null ? "" : search.getText().toString().trim().toLowerCase(Locale.US);
        List<Doctor> shown = new ArrayList<>();
        for (Doctor d : all) {
            boolean inSpecialty = specialty == null || same(d.getSpecialty(), specialty);
            String hay = (Ui.orDash(d.getName()) + " " + Ui.orDash(d.getSpecialty()) + " " + Ui.orDash(d.getHospitalName()))
                    .toLowerCase(Locale.US);
            if (inSpecialty && (q.isEmpty() || hay.contains(q))) shown.add(d);
        }
        adapter.submit(shown);
        count.setText(all.size() == 1 ? getString(R.string.doctors_count_one) : getString(R.string.doctors_count, all.size()));
        board.setVisibility(shown.isEmpty() ? View.GONE : View.VISIBLE);
        if (shown.isEmpty() && !all.isEmpty()) {
            state.empty(R.drawable.ic_magnifying_glass, getString(R.string.no_doctors_title),
                    getString(R.string.no_doctors_body), null, null);
        } else if (all.isEmpty()) {
            state.empty(R.drawable.ic_stethoscope, getString(R.string.no_doctors_title),
                    getString(R.string.no_doctors_yet), null, null);
        } else {
            state.hide();
        }
    }

    /** "Cardiologist" and "Cardiology" are the same filter. */
    private static boolean same(@Nullable String a, @Nullable String b) {
        return a != null && b != null && stem(a).equals(stem(b));
    }

    private static String stem(String s) {
        String suffix = "(ist|ian|ics|ic|y)$"; // twice: pediatrician -> pediatric -> pediatr
        return s.trim().toLowerCase(Locale.US).replaceAll(suffix, "").replaceAll(suffix, "");
    }

    private void hideKeyboard() {
        ((InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(search.getWindowToken(), 0);
    }
}
