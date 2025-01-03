package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUp extends AppCompatActivity {
    TextView GoBack;
    Button SignUp;
    EditText editTextName,editTextPassword,editTextEmail;
    FirebaseAuth mAuth;
    ImageButton show;
    private boolean isPasswordVisible = false;

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(SignUp.this,Home.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        SpannableString spannableString = new SpannableString("Already have an account? Login");
        editTextName= findViewById(R.id.Name);
        editTextPassword= findViewById(R.id.Password);
        editTextEmail= findViewById(R.id.Email);
        GoBack = findViewById(R.id.GoBack);
        SignUp = findViewById(R.id.SignUp);
        show = findViewById(R.id.toggleButton);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
            }
        };
        spannableString.setSpan(clickableSpan, 25, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        GoBack.setText(spannableString);
        GoBack.setMovementMethod(LinkMovementMethod.getInstance());

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email,password,name;
                email = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();
                name = editTextName.getText().toString();


                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(com.example.reuvenbagrut.SignUp.this,"Enter email",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(password))
                {
                    Toast.makeText(com.example.reuvenbagrut.SignUp.this,"Enter password",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(name))
                {
                    Toast.makeText(com.example.reuvenbagrut.SignUp.this,"Enter name",Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (user != null) {
                                        // Update the user's profile with the name
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build();

                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(SignUp.this, "Account created.", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(SignUp.this, Home.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(SignUp.this, "Failed to save name: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(SignUp.this, "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
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