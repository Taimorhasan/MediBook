package com.example.medibook.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import com.example.medibook.R;
import com.google.android.material.button.MaterialButton;

/** Drives an included view_state layout: loading, empty, error, or hidden. */
public final class StateView {
    private final View root;
    private final View progress;
    private final View iconBox;
    private final ImageView icon;
    private final TextView title;
    private final TextView body;
    private final MaterialButton action;

    public StateView(View root) {
        this.root = root;
        progress = root.findViewById(R.id.state_progress);
        iconBox = root.findViewById(R.id.state_icon_box);
        icon = root.findViewById(R.id.state_icon);
        title = root.findViewById(R.id.state_title);
        body = root.findViewById(R.id.state_body);
        action = root.findViewById(R.id.state_action);
    }

    public void loading() {
        root.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        iconBox.setVisibility(View.GONE);
        title.setVisibility(View.GONE);
        body.setVisibility(View.GONE);
        action.setVisibility(View.GONE);
    }

    public void empty(@DrawableRes int iconRes, String titleText, @Nullable String bodyText,
                      @Nullable String actionText, @Nullable Runnable onAction) {
        root.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        iconBox.setVisibility(View.VISIBLE);
        icon.setImageResource(iconRes);
        title.setVisibility(View.VISIBLE);
        title.setText(titleText);
        body.setVisibility(Ui.blank(bodyText) ? View.GONE : View.VISIBLE);
        body.setText(bodyText);
        boolean hasAction = actionText != null && onAction != null;
        action.setVisibility(hasAction ? View.VISIBLE : View.GONE);
        if (hasAction) {
            action.setText(actionText);
            action.setOnClickListener(v -> onAction.run());
        }
    }

    public void error(String message, Runnable retry) {
        empty(R.drawable.ic_warning_circle, root.getContext().getString(R.string.loading_failed), message,
                root.getContext().getString(R.string.retry), retry);
    }

    public void hide() {
        root.setVisibility(View.GONE);
    }

    public boolean isShowing() {
        return root.getVisibility() == View.VISIBLE;
    }
}
