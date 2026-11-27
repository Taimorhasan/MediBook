package com.example.medibook.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.utils.PassView;
import com.google.android.material.card.MaterialCardView;
import com.example.medibook.utils.RoleRouter;
import com.example.medibook.utils.Ui;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/** Email or Google sign-in for every role. New Google users become patients. */
public class UnifiedLoginActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "message";

    private final AuthRepository auth = new AuthRepository();
    private EditText email, password;
    private TextInputLayout emailLayout, passwordLayout;
    private TextView message;
    private View overlay;
    private GoogleSignInClient google;
    private ActivityResultLauncher<Intent> googleLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unified_login);
        email = findViewById(R.id.email_edit_text);
        password = findViewById(R.id.password_edit_text);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        message = findViewById(R.id.login_message);
        overlay = findViewById(R.id.progress_overlay);
        showMessage(getIntent().getStringExtra(EXTRA_MESSAGE));
        MaterialCardView brand = findViewById(R.id.brand);
        PassView.cutNotches(brand, brand.findViewById(R.id.brand_head));
        if (savedInstanceState == null) Ui.rise(brand);
        findViewById(R.id.forgot_password).setOnClickListener(v -> resetPassword());

        google = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build());
        googleLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> onGoogleResult(result.getData()));

        findViewById(R.id.login_button).setOnClickListener(v -> signIn());
        password.setOnEditorActionListener((v, id, e) -> {
            signIn();
            return true;
        });
        findViewById(R.id.google_login_button).setOnClickListener(v -> {
            loading(true);
            googleLauncher.launch(google.getSignInIntent());
        });
        findViewById(R.id.signup_redirect).setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }

    private void signIn() {
        String e = email.getText() == null ? "" : email.getText().toString().trim();
        String p = password.getText() == null ? "" : password.getText().toString();
        emailLayout.setError(Patterns.EMAIL_ADDRESS.matcher(e).matches() ? null : getString(R.string.error_email));
        passwordLayout.setError(p.isEmpty() ? getString(R.string.error_required) : null);
        if (emailLayout.getError() != null || passwordLayout.getError() != null) return;

        loading(true);
        showMessage(null);
        auth.signIn(e, p, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                route(user);
            }

            @Override
            public void onFailure(String error) {
                loading(false);
                showMessage(getString(R.string.login_failed, error));
            }
        });
    }

    private void resetPassword() {
        String e = email.getText() == null ? "" : email.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            emailLayout.setError(getString(R.string.reset_enter_email));
            email.requestFocus();
            return;
        }
        emailLayout.setError(null);
        loading(true);
        auth.sendPasswordReset(e, new AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                loading(false);
                showMessage(getString(R.string.reset_sent, e));
            }

            @Override
            public void onFailure(String error) {
                loading(false);
                showMessage(getString(R.string.error_generic, error));
            }
        });
    }

    private void onGoogleResult(Intent data) {
        try {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
            auth.signInWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null), new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    auth.checkUserExists(user.getUid(), exists -> {
                        if (exists) {
                            route(user);
                            return;
                        }
                        String name = !Ui.blank(account.getDisplayName()) ? account.getDisplayName() : getString(R.string.role_patient);
                        auth.createPatientProfile(user.getUid(), name, user.getEmail(), "", new AuthRepository.VoidCallback() {
                            @Override public void onSuccess() { route(user); }
                            @Override public void onFailure(String error) {
                                loading(false);
                                showMessage(getString(R.string.signup_failed, error));
                            }
                        });
                    });
                }

                @Override
                public void onFailure(String error) {
                    loading(false);
                    showMessage(getString(R.string.login_failed, error));
                }
            });
        } catch (ApiException e) {
            loading(false);
            if (e.getStatusCode() != GoogleSignInStatusCodes.SIGN_IN_CANCELLED) showMessage(getString(R.string.google_failed));
        }
    }

    private void route(FirebaseUser user) {
        RoleRouter.route(this, user.getUid(), blocked -> {
            loading(false);
            showMessage(blocked);
        });
    }

    private void showMessage(String text) {
        message.setText(text);
        message.setVisibility(Ui.blank(text) ? View.GONE : View.VISIBLE);
    }

    private void loading(boolean on) {
        overlay.setVisibility(on ? View.VISIBLE : View.GONE);
    }
}
