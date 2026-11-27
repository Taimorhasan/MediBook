package com.example.medibook.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.adapters.AppointmentAdapter;
import com.example.medibook.adapters.AppointmentAdapter.Viewer;
import com.example.medibook.models.Appointment;
import com.example.medibook.repositories.AppointmentRepository;
import com.example.medibook.utils.BookingSheet;
import com.example.medibook.utils.SessionManager;
import com.example.medibook.utils.StateView;
import com.example.medibook.utils.Ui;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Upcoming and past appointments: the patient's Bookings tab and the doctor's Schedule tab. */
public class BookingsFragment extends TabFragment {
    private MaterialButtonToggleGroup toggle;
    private View board;
    private StateView state;
    private AppointmentAdapter adapter;
    private Viewer viewer;
    private List<Appointment> all = new ArrayList<>();
    private boolean loaded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saved) {
        return inflater.inflate(R.layout.fragment_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        viewer = "doctor".equals(shell().getRole()) ? Viewer.DOCTOR : Viewer.PATIENT;
        ((TextView) v.findViewById(R.id.bookings_title))
                .setText(viewer == Viewer.DOCTOR ? R.string.schedule_title : R.string.bookings_title);
        toggle = v.findViewById(R.id.bookings_toggle);
        board = v.findViewById(R.id.bookings_board);
        state = new StateView(v.findViewById(R.id.bookings_state));
        adapter = new AppointmentAdapter(viewer, a -> BookingSheet.show(v, a, viewer, this::load), null);
        Ui.board((RecyclerView) board.findViewById(R.id.board_list), adapter);
        toggle.addOnButtonCheckedListener((group, id, checked) -> {
            if (checked) render();
        });
    }

    @Override
    protected void load() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        if (!loaded) state.loading();
        AppointmentRepository.AppointmentsCallback callback = new AppointmentRepository.AppointmentsCallback() {
            @Override
            public void onSuccess(List<Appointment> list) {
                if (!alive()) return;
                loaded = true;
                all = list;
                render();
            }

            @Override
            public void onFailure(String error) {
                if (!alive()) return;
                board.setVisibility(View.GONE);
                state.error(error, BookingsFragment.this::load);
            }
        };
        AppointmentRepository repo = new AppointmentRepository();
        if (viewer == Viewer.DOCTOR) {
            repo.getDoctorAppointments(uid, callback);
        } else {
            repo.getPatientAppointments(uid, callback);
        }
    }

    private void render() {
        boolean upcoming = toggle.getCheckedButtonId() == R.id.bookings_upcoming;
        List<Appointment> shown = new ArrayList<>();
        for (Appointment a : all) {
            if (Ui.isUpcoming(a) == upcoming) shown.add(a);
        }
        // Upcoming soonest first; past most recent first.
        Collections.sort(shown, (x, y) -> upcoming ? Long.compare(Ui.when(x), Ui.when(y)) : Long.compare(Ui.when(y), Ui.when(x)));
        adapter.submit(shown, new SessionManager(requireContext()).getLastSeen());
        board.setVisibility(shown.isEmpty() ? View.GONE : View.VISIBLE);
        if (shown.isEmpty()) {
            String body = viewer == Viewer.DOCTOR
                    ? getString(upcoming ? R.string.no_schedule_upcoming : R.string.no_schedule_past)
                    : getString(upcoming ? R.string.no_upcoming_bookings : R.string.no_past_bookings);
            boolean offerBooking = viewer == Viewer.PATIENT && upcoming;
            state.empty(R.drawable.ic_calendar_blank, getString(R.string.no_bookings_title), body,
                    offerBooking ? getString(R.string.find_a_doctor) : null,
                    offerBooking ? () -> shell().selectTab(R.id.nav_doctors, null) : null);
        } else {
            state.hide();
        }
    }

    /** Leaving the tab counts as having seen its changes. */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden && loaded) new SessionManager(requireContext()).markSeen();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isHidden() && loaded) new SessionManager(requireContext()).markSeen();
    }
}
