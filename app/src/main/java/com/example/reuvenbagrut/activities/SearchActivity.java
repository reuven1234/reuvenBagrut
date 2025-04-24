package com.example.reuvenbagrut.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.RecipeAdapter;
import com.example.reuvenbagrut.models.Recipe;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {
    private TextInputEditText searchInput;
    private RecyclerView recipesRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipes;
    private FirebaseFirestore db;
    private ChipGroup filterChipGroup;
    private String selectedDifficulty = null;
    private String selectedTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.search_recipes);

        searchInput = findViewById(R.id.searchInput);
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);
        filterChipGroup = findViewById(R.id.filterChipGroup);

        // Setup RecyclerView
        recipes = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(recipes, this);
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipesRecyclerView.setAdapter(recipeAdapter);

        // Setup filter chips
        setupFilterChips();

        // Setup search input listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchQuery = s.toString().trim();
                if (searchQuery.length() >= 2) {
                    searchRecipes(searchQuery);
                } else {
                    recipes.clear();
                    recipeAdapter.updateRecipes(recipes);
                    updateEmptyState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterChips() {
        // Difficulty filter
        String[] difficulties = {"Easy", "Medium", "Hard"};
        for (String difficulty : difficulties) {
            Chip chip = new Chip(this);
            chip.setText(difficulty);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedDifficulty = difficulty;
                    // Uncheck other difficulty chips
                    for (int i = 0; i < filterChipGroup.getChildCount(); i++) {
                        View child = filterChipGroup.getChildAt(i);
                        if (child instanceof Chip && child != buttonView) {
                            ((Chip) child).setChecked(false);
                        }
                    }
                } else {
                    selectedDifficulty = null;
                }
                performSearch();
            });
            filterChipGroup.addView(chip);
        }

        // Time filter
        String[] times = {"< 30 min", "30-60 min", "> 60 min"};
        for (String time : times) {
            Chip chip = new Chip(this);
            chip.setText(time);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedTime = time;
                    // Uncheck other time chips
                    for (int i = 0; i < filterChipGroup.getChildCount(); i++) {
                        View child = filterChipGroup.getChildAt(i);
                        if (child instanceof Chip && child != buttonView) {
                            ((Chip) child).setChecked(false);
                        }
                    }
                } else {
                    selectedTime = null;
                }
                performSearch();
            });
            filterChipGroup.addView(chip);
        }
    }

    private void performSearch() {
        String searchQuery = searchInput.getText().toString().trim();
        if (searchQuery.length() >= 2) {
            searchRecipes(searchQuery);
        }
    }

    private void searchRecipes(String query) {
        showLoading(true);
        
        Query firestoreQuery = db.collection("recipes")
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff");

        // Apply difficulty filter
        if (selectedDifficulty != null) {
            firestoreQuery = firestoreQuery.whereEqualTo("difficultyLevel", selectedDifficulty);
        }

        // Apply time filter
        if (selectedTime != null) {
            switch (selectedTime) {
                case "< 30 min":
                    firestoreQuery = firestoreQuery.whereLessThan("cookingTime", 30);
                    break;
                case "30-60 min":
                    firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo("cookingTime", 30)
                            .whereLessThanOrEqualTo("cookingTime", 60);
                    break;
                case "> 60 min":
                    firestoreQuery = firestoreQuery.whereGreaterThan("cookingTime", 60);
                    break;
            }
        }

        firestoreQuery.limit(20)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        recipes.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Recipe recipe = document.toObject(Recipe.class);
                            recipe.setId(document.getId());
                            recipes.add(recipe);
                        }
                        recipeAdapter.updateRecipes(recipes);
                        updateEmptyState();
                    } else {
                        showError(task.getException());
                    }
                });
    }

    private void showError(Exception exception) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.error_title)
                .setMessage(exception != null ? exception.getMessage() : getString(R.string.unknown_error))
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recipesRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        if (recipes.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recipesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recipesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipe.getId());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_filters) {
            clearFilters();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearFilters() {
        selectedDifficulty = null;
        selectedTime = null;
        for (int i = 0; i < filterChipGroup.getChildCount(); i++) {
            View child = filterChipGroup.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setChecked(false);
            }
        }
        performSearch();
    }
} 