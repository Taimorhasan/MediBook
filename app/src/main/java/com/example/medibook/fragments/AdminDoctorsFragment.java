package com.example.medibook.fragments;

import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.activities.user.DoctorProfileActivity;
import com.example.medibook.adapters.DoctorAdapter;
import com.example.medibook.adapters.RowAdapter;
import com.example.medibook.models.Doctor;
import com.example.medibook.models.Hospital;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.repositories.HospitalRepository;
import com.example.medibook.utils.Ui;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Admins verify, activate and place doctors. */
public class AdminDoctorsFragment extends AdminListFragment<Doctor> {
    public static final String FILTER_UNVERIFIED = "unverified";
    private final DoctorRepository repo = new DoctorRepository();
    private RowAdapter<Doctor> adapter;

    @Override
    protected int title() {
        return R.string.tab_doctors;
    }

    @Override
    protected String[][] filterOptions() {
        return new String[][]{
                {"all", getString(R.string.filter_all)},
                {FILTER_UNVERIFIED, getString(R.string.filter_unverified)},
                {"verified", getString(R.string.filter_verified)},
                {"inactive", getString(R.string.filter_inactive)}};
    }

    @Override
    protected String searchHint() {
        return getString(R.string.search_doctors);
    }

    @Override
    protected RecyclerView.Adapter<?> createAdapter() {
        adapter = new RowAdapter<>((d, row) -> {
            String name = Ui.doctorName(d.getName());
            row.title.setText(name);
            row.sub.setText(DoctorAdapter.specialtyLine(d));
            Ui.avatar(row.avatar.findViewById(R.id.avatar_image), row.avatar.findViewById(R.id.avatar_initials),
                    d.getProfileImage(), name);
            if (d.isVerified()) {
                Ui.pill(row.tag, getString(R.string.filter_verified), R.color.mb_ok, R.color.mb_ok_bg, R.color.mb_ok_bg);
            } else {
                Ui.pill(row.tag, getString(R.string.filter_unverified), R.color.mb_hot, R.color.mb_panel, R.color.mb_hot);
            }
            if (!d.isActive()) {
                row.tag2.setVisibility(android.view.View.VISIBLE);
                Ui.pill(row.tag2, getString(R.string.filter_inactive), R.color.mb_mute, R.color.mb_panel, R.color.mb_line);
            }
        }, this::actions);
        return adapter;
    }

    @Override
    protected void fetch(Result<Doctor> result) {
        repo.getAllDoctorsAdmin(new DoctorRepository.DoctorsCallback() {
            @Override
            public void onSuccess(List<Doctor> doctors) {
                // Unverified first: they are the admin's queue.
                Collections.sort(doctors, (a, b) -> a.isVerified() != b.isVerified()
                        ? (a.isVerified() ? 1 : -1) : Ui.orDash(a.getName()).compareToIgnoreCase(Ui.orDash(b.getName())));
                result.ok(doctors);
            }

            @Override
            public void onFailure(String error) {
                result.fail(error);
            }
        });
    }

    @Override
    protected boolean matches(Doctor d, String filter, String query) {
        boolean inFilter;
        switch (filter) {
            case FILTER_UNVERIFIED: inFilter = !d.isVerified(); break;
            case "verified": inFilter = d.isVerified(); break;
            case "inactive": inFilter = !d.isActive(); break;
            default: inFilter = true;
        }
        String hay = (Ui.orDash(d.getName()) + " " + Ui.orDash(d.getSpecialty()) + " " + Ui.orDash(d.getEmail()))
                .toLowerCase(Locale.US);
        return inFilter && (query.isEmpty() || hay.contains(query));
    }

    @Override
    protected void show(List<Doctor> items) {
        adapter.submit(items);
    }

    @Override
    protected String emptyText() {
        return getString(R.string.no_doctors_admin);
    }

    private void actions(Doctor d) {
        new Ui.Sheet(requireContext(), Ui.doctorName(d.getName()), DoctorAdapter.specialtyLine(d))
                .action(d.isVerified() ? R.drawable.ic_x_circle : R.drawable.ic_shield_check,
                        getString(d.isVerified() ? R.string.unverify : R.string.verify), d.isVerified(),
                        () -> repo.verifyDoctor(d.getDoctorId(), !d.isVerified(), done()))
                .action(R.drawable.ic_arrows_clockwise, getString(d.isActive() ? R.string.deactivate : R.string.activate),
                        d.isActive(), () -> repo.setActive(d.getDoctorId(), !d.isActive(), done()))
                .action(R.drawable.ic_buildings, getString(R.string.assign_hospital), false, () -> assignHospital(d))
                .action(R.drawable.ic_stethoscope, getString(R.string.view_doctor), false,
                        () -> startActivity(new Intent(requireContext(), DoctorProfileActivity.class)
                                .putExtra(DoctorProfileActivity.EXTRA_DOCTOR_ID, d.getDoctorId())
                                .putExtra(DoctorProfileActivity.EXTRA_PREVIEW, true)))
                .show();
    }

    private void assignHospital(Doctor d) {
        new HospitalRepository().getAllHospitals(new HospitalRepository.HospitalsCallback() {
            @Override
            public void onSuccess(List<Hospital> hospitals) {
                if (!alive()) return;
                if (hospitals.isEmpty()) {
                    Ui.snack(requireView(), R.string.no_hospitals_assign);
                    return;
                }
                String[] names = new String[hospitals.size()];
                for (int i = 0; i < names.length; i++) names[i] = Ui.orDash(hospitals.get(i).getName());
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.assign_hospital)
                        .setItems(names, (dialog, which) -> {
                            Hospital h = hospitals.get(which);
                            repo.assignHospital(d.getDoctorId(), h.getHospitalId(), h.getName(), done());
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }

            @Override
            public void onFailure(String error) {
                if (alive()) Ui.snack(requireView(), getString(R.string.error_generic, error));
            }
        });
    }

    private AuthRepository.VoidCallback done() {
        return new AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                if (alive()) load();
            }

            @Override
            public void onFailure(String error) {
                if (alive()) Ui.snack(requireView(), getString(R.string.error_generic, error));
            }
        };
    }
}
