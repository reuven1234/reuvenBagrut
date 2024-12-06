package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.reuvenbagrut.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;


public class Home extends AppCompatActivity
    implements BottomNavigationView
            .OnNavigationItemSelectedListener {
        BottomNavigationView bottomNavigationView;
    ActivityMainBinding binding;
    FirebaseAuth auth;
    Button logout;
    TextView logoutTxt;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        logout = findViewById(R.id.LogOut);
        logoutTxt = findViewById(R.id.LogOutTxt);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView
                .setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.home);



            /*
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
             */




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
    Home_nav_Fragment firstFragment = new Home_nav_Fragment();
    Profile_nav_Fragment secondFragment = new Profile_nav_Fragment();
    Settings_nav_Fragment thirdFragment = new Settings_nav_Fragment();

    @Override
    public boolean
    onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.home) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, firstFragment)
                    .commit();
            return true;

        } else if (item.getItemId() == R.id.profile) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, secondFragment)
                    .commit();
            return true;

        } else if (item.getItemId() == R.id.settings) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, thirdFragment)
                    .commit();
            return true;
        }

        return false;
    }

    }