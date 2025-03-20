package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private static final String TAG = "HomeActivity";
    private static final String KEY_SELECTED_ITEM = "selected_item";
    
    private BottomNavigationView bottomNavigationView;
    private MaterialToolbar topAppBar;
    private FloatingActionButton fabSearch;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private int selectedItemId = R.id.nav_home;

    private final HomeFragment homeFragment = new HomeFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();
    private final AddRecipeFragment addRecipeFragment = new AddRecipeFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_home);

        initializeAuth();
        initializeViews();
        setupViews(savedInstanceState);
    }

    private void initializeAuth() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        
        if (user == null) {
            navigateToMain();
            return;
        }
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        topAppBar = findViewById(R.id.topAppBar);
        fabSearch = findViewById(R.id.fabSearch);
    }

    private void setupViews(Bundle savedInstanceState) {
        // Setup TopAppBar
        setSupportActionBar(topAppBar);
        topAppBar.setNavigationOnClickListener(v -> {
            // Handle navigation icon press
            showSnackbar(getString(R.string.coming_soon));
        });

        // Setup BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(this);
        
        // Restore selected item
        if (savedInstanceState != null) {
            selectedItemId = savedInstanceState.getInt(KEY_SELECTED_ITEM, R.id.nav_home);
        }
        bottomNavigationView.setSelectedItemId(selectedItemId);

        // Setup FAB
        fabSearch.setOnClickListener(v -> {
            // Handle search FAB click
            showSnackbar(getString(R.string.coming_soon));
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        selectedItemId = itemId;
        
        Fragment selectedFragment = null;
        String tag = null;
        String title = getString(R.string.app_name);

        if (itemId == R.id.nav_home) {
            selectedFragment = homeFragment;
            tag = "home";
            title = getString(R.string.nav_home);
        } else if (itemId == R.id.nav_add) {
            selectedFragment = addRecipeFragment;
            tag = "add";
            title = getString(R.string.nav_add);
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = profileFragment;
            tag = "profile";
            title = getString(R.string.nav_profile);
        } else if (itemId == R.id.nav_settings) {
            selectedFragment = settingsFragment;
            tag = "settings";
            title = getString(R.string.nav_settings);
        }

        if (selectedFragment != null) {
            topAppBar.setTitle(title);
            replaceFragment(selectedFragment, tag);
            return true;
        }

        return false;
    }

    private void replaceFragment(Fragment fragment, String tag) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            
            transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            );
            
            transaction.replace(R.id.fragmentContainer, fragment, tag);
            transaction.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error replacing fragment", e);
            showSnackbar(getString(R.string.error_switching_fragments));
        }
    }

    private void showSnackbar(String message) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_SHORT
        ).show();
    }

    private void navigateToMain() {
        Intent intent = new Intent(Home.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_ITEM, selectedItemId);
    }

    @Override
    public void onBackPressed() {
        if (selectedItemId != R.id.nav_home) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else {
            super.onBackPressed();
        }
    }
}