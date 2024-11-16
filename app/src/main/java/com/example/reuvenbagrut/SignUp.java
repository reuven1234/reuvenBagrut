package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);


        TextView GoBack = findViewById(R.id.GoBack);
        SpannableString spannableString = new SpannableString("Already have an account? Login");

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

    }
}