package com.example.reuvenbagrut;

import android.os.Bundle;
import android.content.Intent;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private TextView goBack;
    private MaterialButton loginButton;
    private TextInputEditText editTextPassword, editTextEmail;
    private TextInputLayout passwordLayout, emailLayout;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupClickableSpan();
        setupClickListeners();
    }

    private void initializeViews() {
        mAuth = FirebaseAuth.getInstance();

        emailLayout    = findViewById(R.id.emailInputLayout);
        passwordLayout = findViewById(R.id.passwordInputLayout);
        editTextPassword = findViewById(R.id.Password);
        editTextEmail    = findViewById(R.id.Email);
        goBack           = findViewById(R.id.GoBack);
        loginButton      = findViewById(R.id.Login);
        progressBar      = findViewById(R.id.progressBar);

        loginButton.setBackgroundTintList(getColorStateList(R.color.primary_color));
    }

    private void setupClickableSpan() {
        SpannableString spannableString = new SpannableString(getString(R.string.dont_have_account));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(Login.this, SignUp.class));
            }
        };

        String text = spannableString.toString();
        int start = text.indexOf("Sign Up");
        int end   = start + "Sign Up".length();

        spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_color)),
                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        goBack.setText(spannableString);
        goBack.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email    = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        navigateToHome();
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.auth_failed);
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
        boolean valid = true;
        emailLayout.setError(null);
        passwordLayout.setError(null);

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError(getString(R.string.email_required));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.invalid_email));
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.password_required));
            valid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError(getString(R.string.password_min_length));
            valid = false;
        }

        return valid;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton .setEnabled(!show);
        editTextEmail .setEnabled(!show);
        editTextPassword.setEnabled(!show);
        emailLayout   .setEnabled(!show);
        passwordLayout.setEnabled(!show);
    }

    private void navigateToHome() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            navigateToHome();
        }
    }
}
