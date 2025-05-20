package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
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

public class SignUp extends AppCompatActivity {
    private MaterialButton signUpButton;
    private TextInputEditText editTextName, editTextEmail, editTextPassword;
    private TextInputLayout nameLayout, emailLayout, passwordLayout;
    private TextView goBack;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        initializeViews();
        setupClickableSpan();
        setupClickListeners();
    }

    private void initializeViews() {
        mAuth = FirebaseAuth.getInstance();

        nameLayout     = findViewById(R.id.nameInputLayout);
        emailLayout    = findViewById(R.id.emailInputLayout);
        passwordLayout = findViewById(R.id.passwordInputLayout);
        editTextName     = findViewById(R.id.Name);
        editTextEmail    = findViewById(R.id.Email);
        editTextPassword = findViewById(R.id.Password);
        goBack         = findViewById(R.id.GoBack);
        signUpButton   = findViewById(R.id.SignUp);
        progressBar    = findViewById(R.id.progressBar);

        signUpButton.setBackgroundTintList(getColorStateList(R.color.primary_color));
    }

    private void setupClickableSpan() {
        SpannableString ss = new SpannableString(getString(R.string.already_have_account));
        ClickableSpan cs = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(SignUp.this, Login.class));
            }
        };

        String text = ss.toString();
        int start = text.indexOf("Login");
        int end   = start + "Login".length();

        ss.setSpan(cs, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_color)),
                start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        goBack.setText(ss);
        goBack.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> performSignUp());
    }

    private void performSignUp() {
        String name     = editTextName.getText().toString().trim();
        String email    = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!validateInputs(name, email, password)) return;

        showLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
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

    private boolean validateInputs(String name, String email, String password) {
        boolean valid = true;
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);

        if (TextUtils.isEmpty(name)) {
            nameLayout.setError(getString(R.string.name_required));
            valid = false;
        }
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
        signUpButton.setEnabled(!show);
        editTextName    .setEnabled(!show);
        editTextEmail   .setEnabled(!show);
        editTextPassword.setEnabled(!show);
        nameLayout      .setEnabled(!show);
        emailLayout     .setEnabled(!show);
        passwordLayout  .setEnabled(!show);
    }

    private void navigateToHome() {
        Intent intent = new Intent(SignUp.this, MainActivity.class);
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
