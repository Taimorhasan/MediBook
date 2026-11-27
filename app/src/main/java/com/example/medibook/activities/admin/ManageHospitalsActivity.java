package com.example.medibook.activities.admin;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.adapters.RowAdapter;
import com.example.medibook.models.Hospital;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.HospitalRepository;
import com.example.medibook.utils.StateView;
import com.example.medibook.utils.Ui;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Admins add, edit and delete the hospitals doctors are assigned to. */
public class ManageHospitalsActivity extends AppCompatActivity {
    private final HospitalRepository repo = new HospitalRepository();
    private StateView state;
    private View board;
    private RowAdapter<Hospital> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_list);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.manage_hospitals);
        toolbar.setNavigationOnClickListener(v -> finish());
        state = new StateView(findViewById(R.id.manage_state));
        board = findViewById(R.id.manage_board);
        adapter = new RowAdapter<>((h, row) -> {
            row.title.setText(Ui.orDash(h.getName()));
            row.sub.setText(Ui.blank(h.getAddress()) ? Ui.orDash(h.getPhone()) : h.getAddress());
            Ui.avatar(row.avatar.findViewById(R.id.avatar_image), row.avatar.findViewById(R.id.avatar_initials), null, h.getName());
            row.tag.setVisibility(View.GONE);
        }, this::actions);
        Ui.board((RecyclerView) board.findViewById(R.id.board_list), adapter);

        MaterialButton add = findViewById(R.id.manage_bar).findViewById(R.id.bar_button);
        add.setText(R.string.add_hospital);
        add.setIconResource(R.drawable.ic_plus);
        add.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        add.setOnClickListener(v -> edit(null));
        load();
    }

    private void load() {
        state.loading();
        board.setVisibility(View.GONE);
        repo.getAllHospitals(new HospitalRepository.HospitalsCallback() {
            @Override
            public void onSuccess(List<Hospital> hospitals) {
                if (isFinishing()) return;
                Collections.sort(hospitals, (a, b) -> Ui.orDash(a.getName()).compareToIgnoreCase(Ui.orDash(b.getName())));
                adapter.submit(hospitals);
                board.setVisibility(hospitals.isEmpty() ? View.GONE : View.VISIBLE);
                if (hospitals.isEmpty()) {
                    state.empty(R.drawable.ic_buildings, getString(R.string.no_hospitals), null, null, null);
                } else {
                    state.hide();
                }
            }

            @Override
            public void onFailure(String error) {
                if (!isFinishing()) state.error(error, ManageHospitalsActivity.this::load);
            }
        });
    }

    private void actions(Hospital h) {
        new Ui.Sheet(this, Ui.orDash(h.getName()), h.getAddress())
                .action(R.drawable.ic_pencil_simple, getString(R.string.edit), false, () -> edit(h))
                .action(R.drawable.ic_trash, getString(R.string.delete), true, () -> new MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.delete_hospital_confirm, Ui.orDash(h.getName())))
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.delete, (d, w) -> repo.deleteHospital(h.getHospitalId(), reload()))
                        .show())
                .show();
    }

    private void edit(@Nullable Hospital existing) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_hospital, null);
        EditText name = form.findViewById(R.id.hospital_name), address = form.findViewById(R.id.hospital_address);
        EditText phone = form.findViewById(R.id.hospital_phone), email = form.findViewById(R.id.hospital_email);
        if (existing != null) {
            name.setText(existing.getName());
            address.setText(existing.getAddress());
            phone.setText(existing.getPhone());
            email.setText(existing.getEmail());
        }
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(existing == null ? R.string.add_hospital : R.string.edit_hospital)
                .setView(form)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String n = text(name), e = text(email);
            TextInputLayout nameLayout = form.findViewById(R.id.hospital_name_layout);
            TextInputLayout emailLayout = form.findViewById(R.id.hospital_email_layout);
            nameLayout.setError(n.isEmpty() ? getString(R.string.error_required) : null);
            emailLayout.setError(e.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(e).matches() ? null : getString(R.string.error_email));
            if (nameLayout.getError() != null || emailLayout.getError() != null) return;

            Hospital h = existing != null ? existing : new Hospital();
            if (h.getHospitalId() == null) h.setHospitalId(UUID.randomUUID().toString());
            h.setName(n);
            h.setAddress(text(address));
            h.setPhone(text(phone));
            h.setEmail(e);
            dialog.dismiss();
            if (existing == null) {
                repo.addHospital(h, new HospitalRepository.HospitalCallback() {
                    @Override public void onSuccess(Hospital hospital) { load(); }
                    @Override public void onFailure(String error) { Ui.snack(board, getString(R.string.error_generic, error)); }
                });
            } else {
                repo.updateHospital(h, reload());
            }
        });
    }

    private AuthRepository.VoidCallback reload() {
        return new AuthRepository.VoidCallback() {
            @Override public void onSuccess() { load(); }
            @Override public void onFailure(String error) { Ui.snack(board, getString(R.string.error_generic, error)); }
        };
    }

    private static String text(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
