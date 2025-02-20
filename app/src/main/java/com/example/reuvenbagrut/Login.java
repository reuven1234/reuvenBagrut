package com.example.reuvenbagrut;

import android.os.Bundle;
import android.content.Intent;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.SpannableString;
import androidx.annotation.NonNull;

import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    TextView GoBack;
    Button Login;
    EditText editTextPassword, editTextEmail;
    FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;
    ImageButton show;
    UserRepository repo;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(Login.this, Home.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        editTextPassword = findViewById(R.id.Password);
        editTextEmail = findViewById(R.id.Email);
        GoBack = findViewById(R.id.GoBack);  // Initialize inside onCreate
        show = findViewById(R.id.toggleButton);
        repo = new UserRepository();

        // Create clickable span for "SignUp"
        SpannableString spannableString = new SpannableString("Don't have an account? SignUp");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        };

        spannableString.setSpan(clickableSpan, 23, 29, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        GoBack.setText(spannableString);
        GoBack.setMovementMethod(LinkMovementMethod.getInstance());

        // Set Login button click listener
        Login = findViewById(R.id.Login);
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                // Validate email and password fields
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Login.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                User u = new User();
                u.setEmail(email);
                u.setPassword(password);
                repo.getUser(u, new FirebaseCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        Intent i = new Intent(Login.this, Home.class);
                        startActivity(i);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });
    }
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            // Show password
            editTextPassword.setTransformationMethod(null);
            show.setImageResource(R.drawable.ic_visibility_off);
        } else {
            // Hide password
            editTextPassword.setTransformationMethod(new PasswordTransformationMethod());
            show.setImageResource(R.drawable.ic_visibility);
        }

        // Maintain cursor position
        editTextPassword.setSelection(editTextPassword.getText().length());
    }
}
