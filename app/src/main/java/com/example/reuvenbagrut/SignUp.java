package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class SignUp extends AppCompatActivity {
    TextView GoBack;
    Button SignUp;
    EditText editTextName,editTextPassword,editTextEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        SpannableString spannableString = new SpannableString("Already have an account? Login");
        editTextName= findViewById(R.id.Name);
        editTextPassword= findViewById(R.id.Password);
        editTextEmail= findViewById(R.id.Email);
        GoBack = findViewById(R.id.GoBack);
        SignUp = findViewById(R.id.SignUp);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
            }
        };

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this,Home.class);
                startActivity(intent);

                String email,password,name;
                email = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();
                name = editTextName.getText().toString();

                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(com.example.reuvenbagrut.SignUp.this,"Enter email",Toast.LENGTH_SHORT).show();
                }
            }
        });

        spannableString.setSpan(clickableSpan, 25, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        GoBack.setText(spannableString);
        GoBack.setMovementMethod(LinkMovementMethod.getInstance());




    }
}