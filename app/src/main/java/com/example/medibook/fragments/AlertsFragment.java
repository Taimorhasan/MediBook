package com.example.medibook.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.activities.common.ChatActivity;
import com.example.medibook.adapters.AlertsAdapter;
import com.example.medibook.adapters.RowAdapter;
import com.example.medibook.models.Chat;
import com.example.medibook.models.Notification;
import com.example.medibook.repositories.ChatRepository;
import com.example.medibook.repositories.NotificationRepository;
import com.example.medibook.utils.StateView;
import com.example.medibook.utils.Ui;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

/** Inbox for patients and doctors: conversations, and booking alerts newest first. */
public class AlertsFragment extends TabFragment {
    private final NotificationRepository repo = new NotificationRepository();
    private MaterialButtonToggleGroup toggle;
    private View alertsBoard, chatsBoard, readAll;
    private StateView state;
    private AlertsAdapter alertsAdapter;
    private RowAdapter<Chat> chatsAdapter;
    private List<Notification> alerts = new ArrayList<>();
    private List<Chat> chats = new ArrayList<>();
    private boolean amPatient, alertsLoaded, chatsLoaded;
    private String error;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saved) {
        return inflater.inflate(R.layout.fragment_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle saved) {
        amPatient = !"doctor".equals(shell().getRole());
        toggle = v.findViewById(R.id.inbox_toggle);
        alertsBoard = v.findViewById(R.id.alerts_board);
        chatsBoard = v.findViewById(R.id.inbox_chats);
        readAll = v.findViewById(R.id.alerts_read_all);
        state = new StateView(v.findViewById(R.id.alerts_state));

        alertsAdapter = new AlertsAdapter(this::open);
        Ui.board((RecyclerView) alertsBoard.findViewById(R.id.board_list), alertsAdapter);
        String me = FirebaseAuth.getInstance().getUid();
        chatsAdapter = new RowAdapter<>((c, row) -> {
            String other = amPatient ? Ui.doctorName(c.getDoctorName()) : Ui.orDash(c.getPatientName());
            row.title.setText(other);
            String last = Ui.blank(c.getLastMessage()) ? getString(R.string.chat_empty_title) : c.getLastMessage();
            row.sub.setText(me != null && me.equals(c.getLastSenderId()) ? getString(R.string.you_prefix, last) : last);
            Ui.avatar(row.avatar.findViewById(R.id.avatar_image), row.avatar.findViewById(R.id.avatar_initials), null, other);
            row.tag.setVisibility(View.VISIBLE);
            Ui.pill(row.tag, Ui.relativeTime(requireContext(), c.getLastAt()), R.color.mb_mute, R.color.mb_panel, R.color.mb_panel);
            long unread = unread(c);
            if (unread > 0) {
                row.tag2.setVisibility(View.VISIBLE);
                Ui.pill(row.tag2, String.valueOf(unread), R.color.mb_on_hot, R.color.mb_hot, R.color.mb_hot);
            }
        }, c -> ChatActivity.open(requireContext(), c.getChatId()));
        Ui.board((RecyclerView) chatsBoard.findViewById(R.id.board_list), chatsAdapter);

        toggle.addOnButtonCheckedListener((group, id, checked) -> {
            if (checked) render();
        });
        readAll.setOnClickListener(x -> markAllRead());
    }

    private long unread(Chat c) {
        return amPatient ? c.getUnreadPatient() : c.getUnreadDoctor();
    }

    @Override
    protected void load() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        if (!alertsLoaded || !chatsLoaded) state.loading();
        error = null;
        repo.getUserNotifications(uid, new NotificationRepository.NotificationsCallback() {
            @Override
            public void onSuccess(List<Notification> list) {
                if (!alive()) return;
                alertsLoaded = true;
                alerts = list;
                render();
            }

            @Override
            public void onFailure(String e) {
                fail(e);
            }
        });
        new ChatRepository().myChats(uid, amPatient, new ChatRepository.ChatsCallback() {
            @Override
            public void onSuccess(List<Chat> list) {
                if (!alive()) return;
                chatsLoaded = true;
                chats = list;
                render();
            }

            @Override
            public void onFailure(String e) {
                fail(e);
            }
        });
    }

    private void fail(String e) {
        if (!alive()) return;
        error = e;
        render();
    }

    private void render() {
        boolean showChats = toggle.getCheckedButtonId() == R.id.inbox_messages;
        alertsAdapter.submit(alerts);
        chatsAdapter.submit(chats);

        int unread = 0;
        for (Notification n : alerts) {
            if (!n.isRead()) unread++;
        }
        long unreadChats = 0;
        for (Chat c : chats) unreadChats += unread(c);
        shell().setAlertsBadge((int) (unread + unreadChats));

        boolean loaded = showChats ? chatsLoaded : alertsLoaded;
        boolean empty = showChats ? chats.isEmpty() : alerts.isEmpty();
        readAll.setVisibility(!showChats && unread > 0 ? View.VISIBLE : View.GONE);
        chatsBoard.setVisibility(showChats && loaded && !empty ? View.VISIBLE : View.GONE);
        alertsBoard.setVisibility(!showChats && loaded && !empty ? View.VISIBLE : View.GONE);

        if (error != null && !loaded) {
            state.error(error, this::load);
        } else if (!loaded) {
            state.loading();
        } else if (empty && showChats) {
            state.empty(R.drawable.ic_chat_circle_text, getString(R.string.no_chats_title),
                    getString(amPatient ? R.string.no_chats_patient : R.string.no_chats_doctor), null, null);
        } else if (empty) {
            state.empty(R.drawable.ic_bell, getString(R.string.no_alerts_title), getString(R.string.no_alerts_body), null, null);
        } else {
            state.hide();
        }
    }

    private void open(Notification n) {
        if (!n.isRead()) markRead(n);
        String type = n.getType() == null ? "" : n.getType();
        if (!type.startsWith("appointment")) return;
        boolean doctor = "doctor".equals(shell().getRole());
        shell().selectTab(doctor ? (type.endsWith("request") ? R.id.nav_today : R.id.nav_schedule) : R.id.nav_bookings, null);
    }

    private void markRead(Notification n) {
        n.setRead(true);
        render();
        repo.markAsRead(n.getNotificationId(), new NotificationRepository.NotificationCallback() {
            @Override public void onSuccess() {}

            @Override
            public void onFailure(String e) {
                if (!alive()) return;
                n.setRead(false);
                render();
                Ui.snack(requireView(), getString(R.string.error_generic, e));
            }
        });
    }

    private void markAllRead() {
        for (Notification n : new ArrayList<>(alerts)) {
            if (!n.isRead()) markRead(n);
        }
    }
}
