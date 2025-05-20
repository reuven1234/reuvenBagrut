package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    private MaterialButton signUpButton;
    private MaterialButton loginButton;
    private FirebaseAuth mAuth;
    private boolean keepSplashScreen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splash = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Keep splash longer
        splash.setKeepOnScreenCondition(() -> keepSplashScreen);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            keepSplashScreen = false;
            EdgeToEdge.enable(this);

            // Always inflate the same layout; decide content by login state
            setContentView(R.layout.activity_main);

            if (currentUser != null) {
                setupNavigation();
            } else {
                initializeViews();
                setupClickListeners();
            }
        }, 1000);
    }

    private void initializeViews() {
        signUpButton = findViewById(R.id.SignUp);
        loginButton  = findViewById(R.id.Login);
        signUpButton .setBackgroundTintList(getColorStateList(R.color.primary_color));
        loginButton  .setBackgroundTintList(getColorStateList(R.color.secondary_color));
    }

    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> startActivity(new Intent(this, SignUp.class)));
        loginButton .setOnClickListener(v -> startActivity(new Intent(this, Login.class)));
    }

    private void setupNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                navController.navigate(R.id.navigation_home);
            } else if (id == R.id.navigation_add_recipe) {
                navController.navigate(R.id.navigation_add_recipe);
            } else if (id == R.id.navigation_chat) {
                navController.navigate(R.id.navigation_chat);
            } else if (id == R.id.navigation_profile) {
                navController.navigate(R.id.navigation_profile);
            } else {
                return false;
            }
            return true;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // If user got signed in during welcome flow
        if (mAuth.getCurrentUser() != null && !keepSplashScreen) {
            setupNavigation();
        }
    }
}
