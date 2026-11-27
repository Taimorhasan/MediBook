package com.example.medibook.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.medibook.MedibookApp;
import com.example.medibook.R;
import com.example.medibook.activities.admin.ManageHospitalsActivity;
import com.example.medibook.activities.admin.ManageRolesActivity;
import com.example.medibook.activities.doctor.EditDoctorProfileActivity;
import com.example.medibook.activities.user.EditPatientProfileActivity;
import com.example.medibook.models.Doctor;
import com.example.medibook.models.User;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.utils.RoleRouter;
import com.example.medibook.utils.Ui;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

/** Profile for patients and doctors; for admins it is the More tab with management links. */
public class ProfileFragment extends TabFragment {
    private String role;
    private View avatar;
    private TextView name, email, roleTag, verified;
    private LinearLayout details;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saved) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        role = shell().getRole();
        avatar = v.findViewById(R.id.profile_avatar);
        name = v.findViewById(R.id.profile_name);
        email = v.findViewById(R.id.profile_email);
        roleTag = v.findViewById(R.id.profile_role);
        verified = v.findViewById(R.id.profile_verified);
        details = v.findViewById(R.id.profile_details);

        boolean admin = "admin".equals(role);
        ((TextView) v.findViewById(R.id.profile_title)).setText(admin ? R.string.more_title : R.string.profile_title);
        int roleLabel = admin ? R.string.role_admin : "doctor".equals(role) ? R.string.role_doctor : R.string.role_patient;
        Ui.pill(roleTag, getString(roleLabel), R.color.mb_ink, R.color.mb_panel_alt, R.color.mb_line);

        if (admin) {
            v.findViewById(R.id.profile_manage_card).setVisibility(View.VISIBLE);
            LinearLayout manage = v.findViewById(R.id.profile_manage);
            Ui.linkRow(manage, R.drawable.ic_buildings, getString(R.string.manage_hospitals),
                    getString(R.string.manage_hospitals_desc), null,
                    () -> startActivity(new Intent(requireContext(), ManageHospitalsActivity.class)));
            Ui.linkRow(manage, R.drawable.ic_key, getString(R.string.manage_roles),
                    getString(R.string.manage_roles_desc), null,
                    () -> startActivity(new Intent(requireContext(), ManageRolesActivity.class)));
        }

        v.findViewById(R.id.profile_edit).setOnClickListener(x -> startActivity(new Intent(requireContext(),
                "doctor".equals(role) ? EditDoctorProfileActivity.class : EditPatientProfileActivity.class)));

        MaterialButtonToggleGroup theme = v.findViewById(R.id.profile_theme);
        int mode = MedibookApp.getThemeMode(requireContext());
        theme.check(mode == AppCompatDelegate.MODE_NIGHT_NO ? R.id.theme_light
                : mode == AppCompatDelegate.MODE_NIGHT_YES ? R.id.theme_dark : R.id.theme_system);
        theme.addOnButtonCheckedListener((group, id, checked) -> {
            if (!checked) return;
            int next = id == R.id.theme_light ? AppCompatDelegate.MODE_NIGHT_NO
                    : id == R.id.theme_dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (next != MedibookApp.getThemeMode(requireContext())) MedibookApp.setThemeMode(requireContext(), next);
        });

        v.findViewById(R.id.profile_sign_out).setOnClickListener(x -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sign_out_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.sign_out, (d, w) -> RoleRouter.signOut(requireActivity()))
                .show());
    }

    @Override
    protected void load() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        new UserRepository().getUser(uid, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (!alive()) return;
                String display = "doctor".equals(role) ? Ui.doctorName(user.getName()) : Ui.orDash(user.getName());
                name.setText(display);
                email.setText(user.getEmail());
                Ui.avatar(avatar.findViewById(R.id.avatar_image), avatar.findViewById(R.id.avatar_initials),
                        user.getProfileImage(), user.getName());
                if ("doctor".equals(role)) {
                    loadDoctor(uid, user);
                } else {
                    details.removeAllViews();
                    Ui.detailRow(details, getString(R.string.phone), user.getPhone());
                    if (!"admin".equals(role)) {
                        Ui.detailRow(details, getString(R.string.age), user.getAge());
                        Ui.detailRow(details, getString(R.string.gender), user.getGender());
                        Ui.detailRow(details, getString(R.string.blood_group), user.getBloodGroup());
                        Ui.detailRow(details, getString(R.string.bio), user.getBio());
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                if (alive()) Ui.snack(requireView(), getString(R.string.error_generic, error));
            }
        });
    }

    private void loadDoctor(String uid, User user) {
        new DoctorRepository().getDoctor(uid, new DoctorRepository.DoctorCallback() {
            @Override
            public void onSuccess(Doctor d) {
                if (!alive()) return;
                if (!Ui.blank(d.getProfileImage())) {
                    Ui.avatar(avatar.findViewById(R.id.avatar_image), avatar.findViewById(R.id.avatar_initials),
                            d.getProfileImage(), d.getName());
                }
                verified.setVisibility(View.VISIBLE);
                if (d.isVerified()) {
                    Ui.pill(verified, getString(R.string.verified_doctor), R.color.mb_ok, R.color.mb_ok_bg, R.color.mb_ok_bg);
                } else {
                    Ui.pill(verified, getString(R.string.awaiting_verification), R.color.mb_hot, R.color.mb_panel, R.color.mb_hot);
                }
                details.removeAllViews();
                Ui.detailRow(details, getString(R.string.specialty), d.getSpecialty());
                Ui.detailRow(details, getString(R.string.experience_years), d.getExperience());
                Ui.detailRow(details, getString(R.string.label_fee),
                        Ui.blank(d.getConsultationFee()) ? null : getString(R.string.fee_pkr, d.getConsultationFee()));
                Ui.detailRow(details, getString(R.string.working_days),
                        d.getAvailableDays() == null || d.getAvailableDays().isEmpty()
                                ? getString(R.string.any_day) : TextUtils.join(", ", d.getAvailableDays()));
                Ui.detailRow(details, getString(R.string.hospital), d.getHospitalName());
                Ui.detailRow(details, getString(R.string.degree), d.getDegree());
                Ui.detailRow(details, getString(R.string.license), d.getLicenseNumber());
                Ui.detailRow(details, getString(R.string.phone), Ui.blank(d.getPhone()) ? user.getPhone() : d.getPhone());
            }

            @Override
            public void onFailure(String error) {
                if (alive()) Ui.snack(requireView(), getString(R.string.error_generic, error));
            }
        });
    }
}
