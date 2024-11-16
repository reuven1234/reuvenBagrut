package com.example.reuvenbagrut;

import android.os.Bundle;
import android.content.Intent;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.SpannableString;
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        TextView GoBack = findViewById(R.id.GoBack);
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


    }
}