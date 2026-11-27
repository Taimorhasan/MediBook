package com.example.medibook.fragments;

import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.adapters.AppointmentAdapter;
import com.example.medibook.adapters.AppointmentAdapter.Viewer;
import com.example.medibook.models.Appointment;
import com.example.medibook.repositories.AppointmentRepository;
import com.example.medibook.utils.BookingSheet;
import com.example.medibook.utils.Ui;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Every appointment in the system, filterable by status; admins can set any status. */
public class AdminBookingsFragment extends AdminListFragment<Appointment> {
    public static final String FILTER_PENDING = "pending";
    private AppointmentAdapter adapter;

    @Override
    protected int title() {
        return R.string.tab_appointments;
    }

    @Override
    protected String[][] filterOptions() {
        return new String[][]{
                {"all", getString(R.string.filter_all)},
                {FILTER_PENDING, getString(R.string.status_pending)},
                {"confirmed", getString(R.string.status_confirmed)},
                {"completed", getString(R.string.status_completed)},
                {"cancelled", getString(R.string.status_cancelled)}};
    }

    @Override
    protected String searchHint() {
        return getString(R.string.search_bookings);
    }

    @Override
    protected RecyclerView.Adapter<?> createAdapter() {
        adapter = new AppointmentAdapter(Viewer.ADMIN,
                a -> BookingSheet.show(requireView(), a, Viewer.ADMIN, this::load), null);
        return adapter;
    }

    @Override
    protected void fetch(Result<Appointment> result) {
        new AppointmentRepository().getAllAppointments(new AppointmentRepository.AppointmentsCallback() {
            @Override
            public void onSuccess(List<Appointment> list) {
                Collections.sort(list, (x, y) -> Long.compare(Ui.when(y), Ui.when(x)));
                result.ok(list);
            }

            @Override
            public void onFailure(String error) {
                result.fail(error);
            }
        });
    }

    @Override
    protected boolean matches(Appointment a, String filter, String query) {
        String status = a.getStatus() == null ? "pending" : a.getStatus().toLowerCase(Locale.US);
        String hay = (Ui.orDash(a.getPatientName()) + " " + Ui.orDash(a.getDoctorName())).toLowerCase(Locale.US);
        return ("all".equals(filter) || filter.equals(status)) && (query.isEmpty() || hay.contains(query));
    }

    @Override
    protected void show(List<Appointment> items) {
        adapter.submit(items, Long.MAX_VALUE);
    }

    @Override
    protected String emptyText() {
        return getString(R.string.no_bookings_admin);
    }
}
