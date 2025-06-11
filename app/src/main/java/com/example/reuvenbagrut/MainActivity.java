package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private MaterialButton signUpButton;
    private MaterialButton loginButton;
    private FirebaseAuth   mAuth;
    private boolean        keepSplashScreen = true;
    private NavController  navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splash = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        splash.setKeepOnScreenCondition(() -> keepSplashScreen);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            keepSplashScreen = false;
            EdgeToEdge.enable(this);

            setContentView(R.layout.activity_main);

            if (currentUser != null) {
                showNavigation();
            } else {
                showLoginScreen();
            }
        }, 1000);
    }

    /* ───────── Login container (welcome screen) ───────── */

    private void showLoginScreen() {
        View loginContainer = findViewById(R.id.login_container);
        View navHostFragment = findViewById(R.id.nav_host_fragment);
        View navView = findViewById(R.id.nav_view);

        loginContainer.setVisibility(View.VISIBLE);
        navHostFragment.setVisibility(View.GONE);
        navView.setVisibility(View.GONE);

        initializeViews();
        setupClickListeners();
    }

    /* ───────── Main navigation UI ───────── */

    private void showNavigation() {
        View loginContainer = findViewById(R.id.login_container);
        View navHostFragment = findViewById(R.id.nav_host_fragment);
        View navView = findViewById(R.id.nav_view);

        loginContainer.setVisibility(View.GONE);
        navHostFragment.setVisibility(View.VISIBLE);
        navView.setVisibility(View.VISIBLE);

        setupNavigation();
    }

    private void initializeViews() {
        signUpButton = findViewById(R.id.SignUp);
        loginButton  = findViewById(R.id.Login);
        if (signUpButton != null)
            signUpButton.setBackgroundTintList(getColorStateList(R.color.primary_color));
        if (loginButton != null)
            loginButton.setBackgroundTintList(getColorStateList(R.color.secondary_color));
    }

    private void setupClickListeners() {
        if (signUpButton != null)
            signUpButton.setOnClickListener(v -> startActivity(new Intent(this, SignUp.class)));
        if (loginButton != null)
            loginButton.setOnClickListener(v -> startActivity(new Intent(this, Login.class)));
    }

    private void setupNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(navView, navController);

            /* Custom handling only for items that need special logic */
            navView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    navController.navigate(R.id.navigation_home);
                } else if (id == R.id.navigation_add_recipe) {
                    navController.navigate(R.id.navigation_add_recipe);
                } else if (id == R.id.navigation_chat) {
                    /* ← NEW: navigate to ChatListFragment inside MainActivity */
                    navController.navigate(R.id.navigation_chat);   // destination in nav_graph.xml
                } else if (id == R.id.navigation_profile) {
                    navController.navigate(R.id.navigation_profile);
                } else if (id == R.id.navigation_settings) {
                    navController.navigate(R.id.navigation_settings);
                } else {
                    return false;
                }
                return true;
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null && !keepSplashScreen) {
            showNavigation();
        }
    }
}
