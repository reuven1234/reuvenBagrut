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
    private static final String TAG = "LoginActivity";
    
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
        
        emailLayout = findViewById(R.id.emailInputLayout);
        passwordLayout = findViewById(R.id.passwordInputLayout);
        editTextPassword = findViewById(R.id.Password);
        editTextEmail = findViewById(R.id.Email);
        goBack = findViewById(R.id.GoBack);
        loginButton = findViewById(R.id.Login);
        progressBar = findViewById(R.id.progressBar);

        // Apply styles
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

        int startIndex = spannableString.toString().indexOf("Sign Up");
        int endIndex = startIndex + "Sign Up".length();
        
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(
            new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_color)),
            startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        
        goBack.setText(spannableString);
        goBack.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        showLoading(true);
        
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        navigateToHome();
                    }
                } else {
                    String errorMessage = task.getException() != null ? 
                        task.getException().getMessage() : 
                        getString(R.string.auth_failed);
                    showError(errorMessage);
                }
                showLoading(false);
            });
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;

        emailLayout.setError(null);
        passwordLayout.setError(null);

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError(getString(R.string.email_required));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.invalid_email));
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.password_required));
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError(getString(R.string.password_min_length));
            isValid = false;
        }

        return isValid;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        editTextEmail.setEnabled(!show);
        editTextPassword.setEnabled(!show);
        emailLayout.setEnabled(!show);
        passwordLayout.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToHome() {
        Intent intent = new Intent(Login.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToHome();
        }
    }
}
