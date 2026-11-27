package com.example.medibook.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.adapters.AppointmentAdapter;
import com.example.medibook.adapters.AppointmentAdapter.Viewer;
import com.example.medibook.models.Appointment;
import com.example.medibook.models.Doctor;
import com.example.medibook.models.User;
import com.example.medibook.repositories.AppointmentRepository;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.utils.BookingSheet;
import com.example.medibook.utils.PassView;
import com.example.medibook.utils.SessionManager;
import com.example.medibook.utils.StateView;
import com.example.medibook.utils.Ui;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

/** Patient home: greeting, next appointment as a pass, specialties, recent bookings. */
public class HomeFragment extends TabFragment {
    private TextView greeting;
    private View avatar, pass, empty, boardTitle, board, specialtiesTitle;
    private ChipGroup specialties;
    private StateView state;
    private AppointmentAdapter adapter;
    private final Map<String, Doctor> doctors = new HashMap<>();
    private List<Appointment> appointments = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saved) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        greeting = v.findViewById(R.id.home_greeting);
        ((TextView) v.findViewById(R.id.home_date)).setText(
                new SimpleDateFormat("EEEE, d MMMM", Locale.US).format(new Date()));
        avatar = v.findViewById(R.id.home_avatar);
        pass = v.findViewById(R.id.home_pass);
        empty = v.findViewById(R.id.home_empty);
        boardTitle = v.findViewById(R.id.home_board_title);
        board = v.findViewById(R.id.home_board);
        specialtiesTitle = v.findViewById(R.id.home_specialties_title);
        specialties = v.findViewById(R.id.home_specialties);
        state = new StateView(v.findViewById(R.id.home_state));

        adapter = new AppointmentAdapter(Viewer.PATIENT, a -> BookingSheet.show(v, a, Viewer.PATIENT, this::load), null);
        Ui.board((RecyclerView) board.findViewById(R.id.board_list), adapter);

        avatar.setOnClickListener(x -> shell().selectTab(R.id.nav_profile, null));
        v.findViewById(R.id.home_search).setOnClickListener(x -> openDoctors(null));
        v.findViewById(R.id.home_book).setOnClickListener(x -> openDoctors(null));
        v.findViewById(R.id.home_see_all).setOnClickListener(x -> shell().selectTab(R.id.nav_bookings, null));
    }

    private void openDoctors(@Nullable String specialty) {
        Bundle args = new Bundle();
        args.putString(DoctorsFragment.ARG_SPECIALTY, specialty);
        args.putBoolean(DoctorsFragment.ARG_FOCUS_SEARCH, specialty == null);
        shell().selectTab(R.id.nav_doctors, args);
    }

    @Override
    protected void load() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        new UserRepository().getUser(uid, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (!alive()) return;
                String first = Ui.firstName(user.getName());
                greeting.setText(first.isEmpty() ? getString(R.string.tab_home) : getString(R.string.greeting_name, first));
                Ui.avatar(avatar.findViewById(R.id.avatar_image), avatar.findViewById(R.id.avatar_initials),
                        user.getProfileImage(), user.getName());
            }

            @Override
            public void onFailure(String error) {
                if (alive()) greeting.setText(R.string.tab_home);
            }
        });

        new DoctorRepository().getAllDoctors(new DoctorRepository.DoctorsCallback() {
            @Override
            public void onSuccess(List<Doctor> list) {
                if (!alive()) return;
                doctors.clear();
                for (Doctor d : list) doctors.put(d.getDoctorId(), d);
                renderSpecialties();
                renderAppointments(); // doctor photos may have arrived after appointments
            }

            @Override
            public void onFailure(String error) {
                // Specialties are a shortcut; the Doctors tab reports load errors.
            }
        });

        if (appointments.isEmpty()) state.loading();
        new AppointmentRepository().getPatientAppointments(uid, new AppointmentRepository.AppointmentsCallback() {
            @Override
            public void onSuccess(List<Appointment> list) {
                if (!alive()) return;
                state.hide();
                appointments = list;
                renderAppointments();
            }

            @Override
            public void onFailure(String error) {
                if (!alive()) return;
                pass.setVisibility(View.GONE);
                empty.setVisibility(View.GONE);
                state.error(error, HomeFragment.this::load);
            }
        });
    }

    private void renderAppointments() {
        List<Appointment> upcoming = new ArrayList<>();
        List<Appointment> rest = new ArrayList<>();
        for (Appointment a : appointments) {
            (Ui.isUpcoming(a) ? upcoming : rest).add(a);
        }
        Collections.sort(upcoming, (x, y) -> Long.compare(Ui.when(x), Ui.when(y)));
        Collections.sort(rest, (x, y) -> Long.compare(Ui.when(y), Ui.when(x)));
        long seen = new SessionManager(requireContext()).getLastSeen();

        if (upcoming.isEmpty()) {
            pass.setVisibility(View.GONE);
            empty.setVisibility(state.isShowing() ? View.GONE : View.VISIBLE);
        } else {
            Appointment next = upcoming.remove(0);
            Doctor d = doctors.get(next.getDoctorId());
            PassView.bind(pass, next, false, AppointmentAdapter.changed(next, Viewer.PATIENT, seen),
                    d == null ? null : d.getProfileImage(), subtitle(next, d));
            pass.setOnClickListener(x -> BookingSheet.show(pass, next, Viewer.PATIENT, this::load));
            pass.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
        }

        List<Appointment> recent = new ArrayList<>(upcoming);
        recent.addAll(rest);
        if (recent.size() > 3) recent = recent.subList(0, 3);
        adapter.submit(recent, seen);
        boardTitle.setVisibility(recent.isEmpty() ? View.GONE : View.VISIBLE);
        board.setVisibility(recent.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private static String subtitle(Appointment a, @Nullable Doctor d) {
        String specialty = !Ui.blank(a.getDoctorSpecialty()) ? a.getDoctorSpecialty() : d == null ? null : d.getSpecialty();
        String hospital = d == null ? null : d.getHospitalName();
        if (Ui.blank(specialty)) return Ui.blank(hospital) ? null : hospital;
        return Ui.blank(hospital) ? specialty : specialty + " · " + hospital;
    }

    private void renderSpecialties() {
        TreeSet<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Doctor d : doctors.values()) {
            if (!Ui.blank(d.getSpecialty())) names.add(d.getSpecialty().trim());
        }
        specialties.removeAllViews();
        for (String name : names) {
            Chip chip = new Chip(requireContext());
            chip.setText(name);
            chip.setCheckable(false);
            chip.setOnClickListener(x -> openDoctors(name));
            specialties.addView(chip);
        }
        specialtiesTitle.setVisibility(names.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
