package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {
    private static final String TAG = "RecipeDetailActivity";

    private MaterialToolbar toolbar;
    private ImageView recipeImage;
    private TextView recipeCategory;
    private TextView recipeIngredients;
    private TextView recipeInstructions;
    private ImageView authorImage;
    private TextView authorName;
    private CircularProgressIndicator progressIndicator;
    
    private String recipeId;
    private String authorId;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private CollapsingToolbarLayout collapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        
        // Get recipeId from intent
        if (getIntent() != null && getIntent().hasExtra("recipe_id")) {
            recipeId = getIntent().getStringExtra("recipe_id");
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Recipe not found", Snackbar.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize views
        initializeViews();

        // Set up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Load recipe data
        loadRecipeData();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recipeImage = findViewById(R.id.recipeImage);
        recipeCategory = findViewById(R.id.recipeCategory);
        recipeIngredients = findViewById(R.id.recipeIngredients);
        recipeInstructions = findViewById(R.id.recipeInstructions);
        authorImage = findViewById(R.id.authorImage);
        authorName = findViewById(R.id.authorName);
        progressIndicator = findViewById(R.id.progressIndicator);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
    }
    
    private void loadRecipeData() {
        if (recipeId == null) {
            finish();
            return;
        }

        showLoading(true);

        db.collection("recipes").document(recipeId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                showLoading(false);
                
                if (!documentSnapshot.exists()) {
                    Snackbar.make(findViewById(android.R.id.content), "Recipe not found", Snackbar.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                try {
                    // Extract recipe data
                    String title = documentSnapshot.getString("strMeal");
                    String category = documentSnapshot.getString("strCategory");
                    String instructions = documentSnapshot.getString("strInstructions");
                    String imageUrl = documentSnapshot.getString("strMealThumb");
                    String authorNameText = documentSnapshot.getString("strAuthor");
                    String authorImageUrl = documentSnapshot.getString("strAuthorImage");
                    authorId = documentSnapshot.getString("userId");
                    
                    // Update UI
                    collapsingToolbar.setTitle(title);
                    recipeCategory.setText(category);
                    recipeInstructions.setText(instructions);
                    
                    // Format ingredients from list
                    if (documentSnapshot.contains("ingredients")) {
                        List<String> ingredientsList = (List<String>) documentSnapshot.get("ingredients");
                        if (ingredientsList != null && !ingredientsList.isEmpty()) {
                            StringBuilder ingredientsBuilder = new StringBuilder();
                            for (String ingredient : ingredientsList) {
                                ingredientsBuilder.append("• ").append(ingredient).append("\n");
                            }
                            recipeIngredients.setText(ingredientsBuilder.toString());
                        } else {
                            recipeIngredients.setText("No ingredients listed");
                        }
                    } else {
                        recipeIngredients.setText("No ingredients listed");
                    }
                    
                    // Load recipe image
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .apply(new RequestOptions()
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.placeholder_image))
                            .into(recipeImage);
                    }
                    
                    // Load author image
                    if (authorImageUrl != null && !authorImageUrl.isEmpty()) {
                        Glide.with(this)
                            .load(authorImageUrl)
                            .apply(new RequestOptions()
                                .placeholder(R.drawable.avatar_placeholder)
                                .error(R.drawable.avatar_placeholder)
                                .circleCrop())
                            .into(authorImage);
                    }
                    
                    this.authorName.setText(authorNameText);
                } catch (Exception e) {
                    Snackbar.make(findViewById(android.R.id.content), "Error loading recipe details", Snackbar.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Snackbar.make(findViewById(android.R.id.content), "Error loading recipe", Snackbar.LENGTH_SHORT).show();
                finish();
            });
    }

    private void showLoading(boolean show) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        }
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