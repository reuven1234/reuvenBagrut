package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
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
    private static final String TAG = "SignUpActivity";
    private static final int MIN_PASSWORD_LENGTH = 6;

    private TextView goBack;
    private MaterialButton signUpButton;
    private TextInputEditText editTextName, editTextPassword, editTextEmail;
    private TextInputLayout nameLayout, emailLayout, passwordLayout;
    private FirebaseAuth mAuth;
    private UserRepository repository;
    private ProgressBar progressBar;

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
        repository = new UserRepository();
        
        nameLayout = findViewById(R.id.nameInputLayout);
        emailLayout = findViewById(R.id.emailInputLayout);
        passwordLayout = findViewById(R.id.passwordInputLayout);
        editTextName = findViewById(R.id.Name);
        editTextPassword = findViewById(R.id.Password);
        editTextEmail = findViewById(R.id.Email);
        goBack = findViewById(R.id.GoBack);
        signUpButton = findViewById(R.id.SignUp);
        progressBar = findViewById(R.id.progressBar);

        // Apply styles
        signUpButton.setBackgroundTintList(getColorStateList(R.color.primary_color));
    }

    private void setupClickableSpan() {
        SpannableString spannableString = new SpannableString(getString(R.string.already_have_account));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(SignUp.this, Login.class));
            }
        };

        int startIndex = spannableString.toString().indexOf("Login");
        int endIndex = startIndex + "Login".length();
        
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(
            new ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary_color)),
            startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        
        goBack.setText(spannableString);
        goBack.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> performSignUp());
    }

    private void performSignUp() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String name = editTextName.getText().toString().trim();

        if (!validateInputs(email, password, name)) {
            return;
        }

        showLoading(true);

        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user.setEmail(email);

        repository.addUser(user, new FirebaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                navigateToHome();
            }

            @Override
            public void onError(String message) {
                showError(message);
                showLoading(false);
            }
        });
    }

    private boolean validateInputs(String email, String password, String name) {
        boolean isValid = true;

        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);

        if (TextUtils.isEmpty(name)) {
            nameLayout.setError(getString(R.string.name_required));
            isValid = false;
        }

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
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordLayout.setError(getString(R.string.password_min_length));
            isValid = false;
        }

        return isValid;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        signUpButton.setEnabled(!show);
        editTextName.setEnabled(!show);
        editTextEmail.setEnabled(!show);
        editTextPassword.setEnabled(!show);
        nameLayout.setEnabled(!show);
        emailLayout.setEnabled(!show);
        passwordLayout.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(SignUp.this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToHome() {
        Intent intent = new Intent(SignUp.this, Home.class);
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