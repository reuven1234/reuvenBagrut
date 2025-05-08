package com.example.reuvenbagrut.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.Recipe;
import com.example.reuvenbagrut.adapters.RecipeAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {
    private TextInputEditText searchInput;
    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private CircularProgressIndicator progressIndicator;
    private FirebaseFirestore db;
    private List<Recipe> searchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.search_recipes);

        searchInput = findViewById(R.id.searchInput);
        recyclerView = findViewById(R.id.searchResultsRecyclerView);
        progressIndicator = findViewById(R.id.progressIndicator);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup search button
        findViewById(R.id.searchButton).setOnClickListener(v -> performSearch());
    }

    private void setupRecyclerView() {
        searchResults = new ArrayList<>();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recipeAdapter = new RecipeAdapter(searchResults, this);
        recyclerView.setAdapter(recipeAdapter);
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            searchInput.setError(getString(R.string.error_empty_search));
            return;
        }

        showProgress(true);
        db.collection("recipes")
                .orderBy("strMeal")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Recipe> recipes = new ArrayList<>();
                        task.getResult().forEach(document -> {
                            Recipe recipe = document.toObject(Recipe.class);
                            recipe.setId(document.getId());
                            recipes.add(recipe);
                        });
                        recipeAdapter.updateRecipes(recipes);
                        showEmptyState(recipes.isEmpty());
                    } else {
                        Toast.makeText(this, R.string.error_searching, Toast.LENGTH_SHORT).show();
                    }
                    showProgress(false);
                });
    }

    private void showProgress(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        findViewById(R.id.emptyStateView).setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipe.getId());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 