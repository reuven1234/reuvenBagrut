package com.example.reuvenbagrut.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.CommentAdapter;
import com.example.reuvenbagrut.api.RetrofitClient;
import com.example.reuvenbagrut.models.Recipe;
import com.example.reuvenbagrut.models.RecipeApiResponse;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FirebaseFirestoreException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeDetailActivity extends AppCompatActivity {
    private static final String TAG = "RecipeDetailActivity";
    
    private CollapsingToolbarLayout collapsingToolbar;
    private MaterialToolbar toolbar;
    private TextView recipeCategory;
    private RecyclerView commentsRecyclerView;
    private ProgressBar progressBar;
    private FirebaseUser currentUser;
    private CommentAdapter commentAdapter;
    private com.google.android.material.imageview.ShapeableImageView recipeImage;
    private com.google.android.material.imageview.ShapeableImageView userProfileImage;
    private MaterialTextView userName;
    private MaterialTextView description;
    private MaterialTextView recipeIngredients;
    private MaterialTextView recipeInstructions;
    private TextInputEditText commentInput;
    private FloatingActionButton favoriteButton;
    private FirebaseFirestore db;
    private String recipeId;
    private boolean isAuthor;
    private RecipeApiResponse.RecipeResult loadedRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        
        // Initialize views
        initializeViews();
        
        // Get recipe from intent
        Recipe recipe = getIntent().getParcelableExtra("recipe");
        if (recipe == null) {
            showError("Recipe not found");
            return;
        }

        // Set recipe details
        collapsingToolbar.setTitle(recipe.getStrMeal());
        if (recipe.getStrCategory() != null) {
            recipeCategory.setText(recipe.getStrCategory());
        }
        if (recipe.getStrMealThumb() != null) {
            Glide.with(this)
                .load(recipe.getStrMealThumb())
                .into(recipeImage);
        }
        if (recipe.getStrInstructions() != null) {
            recipeInstructions.setText(recipe.getStrInstructions());
        }
        if (recipe.getIngredients() != null) {
            StringBuilder ingredientsText = new StringBuilder();
            for (String ingredient : recipe.getIngredients()) {
                ingredientsText.append("• ").append(ingredient).append("\n");
            }
            recipeIngredients.setText(ingredientsText.toString());
        }
        if (recipe.getUserName() != null) {
            userName.setText(recipe.getUserName());
        }
        if (recipe.getUserImage() != null) {
            Glide.with(this)
                .load(recipe.getUserImage())
                .circleCrop()
                .into(userProfileImage);
        }

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Setup favorite button
        setupFavoriteButton();

        // Setup comment input
        setupCommentInput();
    }

    private void initializeViews() {
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbar);
        recipeCategory = findViewById(R.id.recipeCategory);
        recipeImage = findViewById(R.id.recipeImage);
        userProfileImage = findViewById(R.id.userProfileImage);
        userName = findViewById(R.id.userName);
        description = findViewById(R.id.description);
        recipeIngredients = findViewById(R.id.recipeIngredients);
        recipeInstructions = findViewById(R.id.recipeInstructions);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentInput = findViewById(R.id.commentInput);
        favoriteButton = findViewById(R.id.favoriteButton);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        if (commentsRecyclerView != null) {
            commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            commentAdapter = new CommentAdapter(this, new ArrayList<>(), null);
            commentsRecyclerView.setAdapter(commentAdapter);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setupFavoriteButton() {
        if (favoriteButton != null) {
            favoriteButton.setOnClickListener(v -> {
                if (currentUser == null) {
                    Toast.makeText(this, "Please login to favorite recipes", Toast.LENGTH_SHORT).show();
                    return;
                }
                // TODO: Implement favorite functionality
            });
        }
    }

    private void setupCommentInput() {
        if (commentInput != null) {
            // TODO: Implement comment functionality
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 