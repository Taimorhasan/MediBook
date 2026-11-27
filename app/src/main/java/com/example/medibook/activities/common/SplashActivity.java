package com.example.medibook.activities.common;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.activities.auth.UnifiedLoginActivity;
import com.example.medibook.utils.RoleRouter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/** Sends a signed-in user to their shell, everyone else to sign-in. */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            toLogin(null);
        } else {
            RoleRouter.route(this, user.getUid(), this::toLogin);
        }
    }

    private void toLogin(String message) {
        startActivity(new Intent(this, UnifiedLoginActivity.class)
                .putExtra(UnifiedLoginActivity.EXTRA_MESSAGE, message)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
