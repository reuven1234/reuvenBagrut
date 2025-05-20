package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private MaterialButton signUpButton;
    private MaterialButton loginButton;
    private FirebaseAuth mAuth;
    private boolean keepSplashScreen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        
        // Keep the splash screen visible for a bit longer
        splashScreen.setKeepOnScreenCondition(() -> keepSplashScreen);
        
        // Check if user is already signed in
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        // Delay to show splash screen
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            keepSplashScreen = false;
            
            // If user is already signed in, go directly to Home
            if (currentUser != null) {
                navigateToHome();
                finish();
                return;
            }
            
            // Otherwise, show the welcome screen
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);
            initializeViews();
            setupClickListeners();
        }, 1000);
    }

    private void initializeViews() {
        signUpButton = findViewById(R.id.SignUp);
        loginButton = findViewById(R.id.Login);

        // Apply button styles
        signUpButton.setBackgroundTintList(getColorStateList(R.color.primary_color));
        loginButton.setBackgroundTintList(getColorStateList(R.color.secondary_color));
    }

    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> navigateToSignUp());
        loginButton.setOnClickListener(v -> navigateToLogin());
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(MainActivity.this, SignUp.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                navController.navigate(R.id.navigation_home);
                return true;
            } else if (itemId == R.id.navigation_add_recipe) {
                navController.navigate(R.id.navigation_add_recipe);
                return true;
            } else if (itemId == R.id.navigation_chat) {
                navController.navigate(R.id.navigation_chat);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                navController.navigate(R.id.navigation_profile);
                return true;
            }
            return false;
        });
    }
}