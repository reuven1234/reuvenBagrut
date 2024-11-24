package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class Home extends AppCompatActivity {

    FirebaseAuth auth;
    Button logout;
    TextView logoutTxt;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        logout = findViewById(R.id.LogOut);
        logoutTxt = findViewById(R.id.LogOutTxt);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        if(user == null)
        {
            Intent intent = new Intent(Home.this,Login.class);
            startActivity(intent);
            finish();
        }
        else
        {
            logoutTxt.setText(user.getEmail());
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Home.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}