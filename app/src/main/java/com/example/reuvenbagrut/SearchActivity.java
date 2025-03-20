package com.example.reuvenbagrut;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private EditText searchEditText;
    private TextView emptyStateText;
    private View progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerView);
        
        // These views might not exist in the layout, so we'll handle them safely
        try {
            emptyStateText = findViewById(R.id.emptyStateText);
            progressIndicator = findViewById(R.id.progressIndicator);
        } catch (Exception e) {
            Log.w(TAG, "Some UI elements were not found in the layout", e);
        }
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recipeAdapter = new RecipeAdapter();
        recyclerView.setAdapter(recipeAdapter);
        
        // Set click listener for recipes
        recipeAdapter.setOnRecipeClickListener((recipe, position) -> {
            if (recipe != null) {
                // Navigate to recipe detail
                navigateToRecipeDetail(recipe);
            }
        });

        // Set up a TextWatcher to detect changes in the EditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // No need to implement this method
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // When the user types, we trigger the search
                String query = charSequence.toString().trim();
                if (query.length() >= 2) {
                    searchRecipes(query);
                } else if (query.isEmpty()) {
                    // If the query is empty, clear the RecyclerView
                    recipeAdapter.setRecipes(new ArrayList<>());
                    showEmptyState(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No need to implement this method
            }
        });
    }

    private void searchRecipes(String query) {
        showLoading(true);
        
        FirebaseFirestore.getInstance().collection("recipes")
            .get()
            .addOnCompleteListener(task -> {
                showLoading(false);
                
                if (task.isSuccessful()) {
                    List<Recipe> matchingRecipes = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            Recipe recipe = new Recipe();
                            recipe.setIdMeal(document.getId());
                            recipe.setStrMeal(document.getString("strMeal"));
                            recipe.setStrCategory(document.getString("strCategory"));
                            recipe.setStrInstructions(document.getString("strInstructions"));
                            recipe.setStrMealThumb(document.getString("strMealThumb"));
                            recipe.setStrAuthor(document.getString("strAuthor"));
                            recipe.setStrAuthorImage(document.getString("strAuthorImage"));
                            recipe.setUserId(document.getString("userId"));
                            
                            // Check if recipe matches search query
                            if (recipe.getStrMeal() != null && 
                                recipe.getStrMeal().toLowerCase().contains(query.toLowerCase())) {
                                matchingRecipes.add(recipe);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing recipe", e);
                        }
                    }
                    
                    recipeAdapter.setRecipes(matchingRecipes);
                    showEmptyState(matchingRecipes.isEmpty());
                } else {
                    Snackbar.make(findViewById(android.R.id.content), 
                                  "Error searching recipes", Snackbar.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Log.e(TAG, "Error searching recipes", e);
                Snackbar.make(findViewById(android.R.id.content), 
                              "Failed to search recipes", Snackbar.LENGTH_SHORT).show();
                showEmptyState(true);
            });
    }
    
    private void navigateToRecipeDetail(Recipe recipe) {
        android.content.Intent intent = new android.content.Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipe.getIdMeal());
        startActivity(intent);
    }
    
    private void showLoading(boolean show) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
            if (show) {
                recyclerView.setVisibility(View.GONE);
                if (emptyStateText != null) {
                    emptyStateText.setVisibility(View.GONE);
                }
            } else {
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void showEmptyState(boolean show) {
        if (emptyStateText != null) {
            emptyStateText.setVisibility(show ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
