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
import com.example.medibook.models.Doctor;
import com.example.medibook.repositories.AppointmentRepository;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.utils.BookingSheet;
import com.example.medibook.utils.SessionManager;
import com.example.medibook.utils.StateView;
import com.example.medibook.utils.Ui;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** A doctor's day: requests waiting for an answer, then today's confirmed visits. */
public class DoctorTodayFragment extends TabFragment {
    private View avatar, content, requestsBoard, requestsEmpty, visitsBoard, visitsEmpty;
    private TextView greeting, requestsCount;
    private StateView state;
    private AppointmentAdapter requests, visits;
    private boolean loaded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saved) {
        return inflater.inflate(R.layout.fragment_doctor_today, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        ((TextView) v.findViewById(R.id.today_date)).setText(
                new SimpleDateFormat("EEEE, d MMMM", Locale.US).format(new Date()));
        greeting = v.findViewById(R.id.today_greeting);
        avatar = v.findViewById(R.id.today_avatar);
        content = v.findViewById(R.id.today_content);
        requestsBoard = v.findViewById(R.id.today_requests);
        requestsEmpty = v.findViewById(R.id.today_requests_empty);
        visitsBoard = v.findViewById(R.id.today_visits);
        visitsEmpty = v.findViewById(R.id.today_visits_empty);
        requestsCount = v.findViewById(R.id.today_requests_count);
        state = new StateView(v.findViewById(R.id.today_state));
        avatar.setOnClickListener(x -> shell().selectTab(R.id.nav_profile, null));

        requests = new AppointmentAdapter(Viewer.DOCTOR, this::open, (a, primary, secondary) -> {
            primary.setVisibility(View.VISIBLE);
            primary.setText(R.string.confirm);
            primary.setOnClickListener(x -> BookingSheet.change(v, a, "confirmed", this::load));
            secondary.setVisibility(View.VISIBLE);
            secondary.setText(R.string.decline);
            secondary.setOnClickListener(x -> BookingSheet.change(v, a, "cancelled", this::load));
        });
        visits = new AppointmentAdapter(Viewer.DOCTOR, this::open, (a, primary, secondary) -> {
            primary.setVisibility(View.VISIBLE);
            primary.setText(R.string.mark_completed);
            primary.setOnClickListener(x -> BookingSheet.change(v, a, "completed", this::load));
        });
        Ui.board((RecyclerView) requestsBoard.findViewById(R.id.board_list), requests);
        Ui.board((RecyclerView) visitsBoard.findViewById(R.id.board_list), visits);
    }

    private void open(Appointment a) {
        BookingSheet.show(requireView(), a, Viewer.DOCTOR, this::load);
    }

    @Override
    protected void load() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        new DoctorRepository().getDoctor(uid, new DoctorRepository.DoctorCallback() {
            @Override
            public void onSuccess(Doctor d) {
                if (!alive()) return;
                greeting.setText(getString(R.string.greeting_name, Ui.doctorName(d.getName())));
                Ui.avatar(avatar.findViewById(R.id.avatar_image), avatar.findViewById(R.id.avatar_initials),
                        d.getProfileImage(), d.getName());
            }

            @Override
            public void onFailure(String error) {}
        });

        if (!loaded) {
            state.loading();
            content.setVisibility(View.GONE);
        }
        new AppointmentRepository().getDoctorAppointments(uid, new AppointmentRepository.AppointmentsCallback() {
            @Override
            public void onSuccess(List<Appointment> list) {
                if (!alive()) return;
                loaded = true;
                state.hide();
                content.setVisibility(View.VISIBLE);
                render(list);
            }

            @Override
            public void onFailure(String error) {
                if (!alive()) return;
                content.setVisibility(View.GONE);
                state.error(error, DoctorTodayFragment.this::load);
            }
        });
    }

    private void render(List<Appointment> all) {
        String today = Ui.today();
        List<Appointment> pending = new ArrayList<>();
        List<Appointment> todays = new ArrayList<>();
        for (Appointment a : all) {
            if (!Ui.isUpcoming(a)) continue;
            if ("pending".equalsIgnoreCase(a.getStatus())) pending.add(a);
            else if (today.equals(a.getDate())) todays.add(a);
        }
        Collections.sort(pending, (x, y) -> Long.compare(Ui.when(x), Ui.when(y)));
        Collections.sort(todays, (x, y) -> Long.compare(Ui.when(x), Ui.when(y)));
        long seen = new SessionManager(requireContext()).getLastSeen();
        requests.submit(pending, seen);
        visits.submit(todays, seen);

        requestsBoard.setVisibility(pending.isEmpty() ? View.GONE : View.VISIBLE);
        requestsEmpty.setVisibility(pending.isEmpty() ? View.VISIBLE : View.GONE);
        requestsCount.setVisibility(pending.isEmpty() ? View.GONE : View.VISIBLE);
        Ui.pill(requestsCount, String.valueOf(pending.size()), R.color.mb_on_hot, R.color.mb_hot, R.color.mb_hot);
        visitsBoard.setVisibility(todays.isEmpty() ? View.GONE : View.VISIBLE);
        visitsEmpty.setVisibility(todays.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden && loaded) new SessionManager(requireContext()).markSeen();
    }
}
