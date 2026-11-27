package com.example.medibook.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.example.medibook.R;
import com.example.medibook.activities.admin.ManageHospitalsActivity;
import com.example.medibook.models.Appointment;
import com.example.medibook.models.Doctor;
import com.example.medibook.models.Hospital;
import com.example.medibook.models.User;
import com.example.medibook.repositories.AppointmentRepository;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.repositories.HospitalRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.utils.StateView;
import com.example.medibook.utils.Ui;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** The admin's first look: what needs a decision, then the size of the system. */
public class AdminOverviewFragment extends TabFragment {
    private LinearLayout attention, system;
    private View content;
    private StateView state;
    private List<Doctor> doctors;
    private List<Appointment> appointments;
    private List<User> users;
    private List<Hospital> hospitals;
    private String error;
    private int pending;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saved) {
        return inflater.inflate(R.layout.fragment_admin_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        ((TextView) v.findViewById(R.id.overview_date)).setText(
                new SimpleDateFormat("EEEE, d MMMM", Locale.US).format(new Date()));
        attention = v.findViewById(R.id.overview_attention);
        system = v.findViewById(R.id.overview_system);
        content = v.findViewById(R.id.overview_content);
        state = new StateView(v.findViewById(R.id.overview_state));
    }

    @Override
    protected void load() {
        if (pending > 0) return; // a load is already in flight
        if (doctors == null) state.loading();
        pending = 4;
        error = null;
        new DoctorRepository().getAllDoctorsAdmin(new DoctorRepository.DoctorsCallback() {
            @Override public void onSuccess(List<Doctor> list) { doctors = list; done(); }
            @Override public void onFailure(String e) { fail(e); }
        });
        new AppointmentRepository().getAllAppointments(new AppointmentRepository.AppointmentsCallback() {
            @Override public void onSuccess(List<Appointment> list) { appointments = list; done(); }
            @Override public void onFailure(String e) { fail(e); }
        });
        new UserRepository().getAllUsers(new UserRepository.UsersCallback() {
            @Override public void onSuccess(List<User> list) { users = list; done(); }
            @Override public void onFailure(String e) { fail(e); }
        });
        new HospitalRepository().getAllHospitals(new HospitalRepository.HospitalsCallback() {
            @Override public void onSuccess(List<Hospital> list) { hospitals = list; done(); }
            @Override public void onFailure(String e) { fail(e); }
        });
    }

    private void fail(String e) {
        error = e;
        done();
    }

    private void done() {
        if (--pending > 0 || !alive()) return;
        if (error != null) {
            content.setVisibility(View.GONE);
            state.error(error, this::load);
            return;
        }
        state.hide();
        content.setVisibility(View.VISIBLE);
        render();
    }

    private void render() {
        int unverified = 0;
        for (Doctor d : doctors) {
            if (!d.isVerified()) unverified++;
        }
        int waiting = 0;
        for (Appointment a : appointments) {
            if ("pending".equalsIgnoreCase(a.getStatus()) && Ui.isUpcoming(a)) waiting++;
        }

        attention.removeAllViews();
        hot(Ui.linkRow(attention, R.drawable.ic_shield_check, getString(R.string.doctors_awaiting), null,
                String.valueOf(unverified), () -> openTab(R.id.nav_admin_doctors, AdminDoctorsFragment.FILTER_UNVERIFIED)), unverified);
        hot(Ui.linkRow(attention, R.drawable.ic_hourglass_medium, getString(R.string.pending_bookings), null,
                String.valueOf(waiting), () -> openTab(R.id.nav_admin_appointments, AdminBookingsFragment.FILTER_PENDING)), waiting);

        system.removeAllViews();
        Ui.linkRow(system, R.drawable.ic_users, getString(R.string.total_users), null,
                String.valueOf(users.size()), () -> openTab(R.id.nav_users, null));
        Ui.linkRow(system, R.drawable.ic_stethoscope, getString(R.string.total_doctors), null,
                String.valueOf(doctors.size()), () -> openTab(R.id.nav_admin_doctors, "all"));
        Ui.linkRow(system, R.drawable.ic_calendar_blank, getString(R.string.total_bookings), null,
                String.valueOf(appointments.size()), () -> openTab(R.id.nav_admin_appointments, "all"));
        Ui.linkRow(system, R.drawable.ic_buildings, getString(R.string.hospitals), null,
                String.valueOf(hospitals.size()), () -> startActivity(new Intent(requireContext(), ManageHospitalsActivity.class)));
    }

    /** A count that needs action reads in the change colour. */
    private void hot(View row, int count) {
        if (count > 0) {
            ((TextView) row.findViewById(R.id.link_value)).setTextColor(ContextCompat.getColor(requireContext(), R.color.mb_hot));
        }
    }

    private void openTab(int tab, @Nullable String filter) {
        Bundle args = null;
        if (filter != null) {
            args = new Bundle();
            args.putString(AdminListFragment.ARG_FILTER, filter);
        }
        shell().selectTab(tab, args);
    }
}
