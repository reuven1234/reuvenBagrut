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
    private TextView recipeCategory;
    private RecyclerView commentsRecyclerView;
    private ProgressBar progressBar;
    private FirebaseUser currentUser;
    private CommentAdapter commentAdapter;
    private com.google.android.material.imageview.ShapeableImageView recipeImage;
    private com.google.android.material.imageview.ShapeableImageView userProfileImage;
    private com.google.android.material.textview.MaterialTextView userName;
    private com.google.android.material.textview.MaterialTextView description;
    private com.google.android.material.textview.MaterialTextView recipeIngredients;
    private com.google.android.material.textview.MaterialTextView recipeInstructions;
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

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Log Firebase initialization
        Log.d("RecipeDetailActivity", "Firebase initialized");
        Log.d("RecipeDetailActivity", "Firebase app name: " + db.getApp().getName());
        Log.d("RecipeDetailActivity", "Firebase project ID: " + db.getApp().getOptions().getProjectId());

        // Initialize views
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        recipeCategory = findViewById(R.id.recipeCategory);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        recipeImage = findViewById(R.id.recipeImage);
        userProfileImage = findViewById(R.id.userProfileImage);
        userName = findViewById(R.id.userName);
        description = findViewById(R.id.description);
        recipeIngredients = findViewById(R.id.recipeIngredients);
        recipeInstructions = findViewById(R.id.recipeInstructions);
        commentInput = findViewById(R.id.commentInput);
        favoriteButton = findViewById(R.id.favoriteButton);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup comments RecyclerView
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(new ArrayList<>());
        commentsRecyclerView.setAdapter(commentAdapter);

        // Get recipe ID from intent
        recipeId = getIntent().getStringExtra("recipe_id");
        if (recipeId == null) {
            showError("Recipe ID not found");
            return;
        }

        // Load recipe details
        loadRecipeDetails(recipeId);

        // Setup favorite button
        setupFavoriteButton();

        // Setup comment input
        setupCommentInput();
    }

    private void loadRecipeDetails(String recipeId) {
        showLoading(true);
        RetrofitClient.getInstance()
            .getApiService()
            .getRecipeById(recipeId)
            .enqueue(new Callback<RecipeApiResponse>() {
                @Override
                public void onResponse(Call<RecipeApiResponse> call, Response<RecipeApiResponse> response) {
                    showLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        loadedRecipe = response.body().getMeals().get(0);
                        if (loadedRecipe != null) {
                            // Update UI with recipe details
                            collapsingToolbar.setTitle(loadedRecipe.getStrMeal());
                            recipeCategory.setText(loadedRecipe.getStrCategory() != null ? loadedRecipe.getStrCategory() : "Uncategorized");
                            
                            // Load recipe image
                            Glide.with(RecipeDetailActivity.this)
                                .load(loadedRecipe.getStrMealThumb())
                                .into(recipeImage);

                            // Load comments
                            loadComments(recipeId);
                        } else {
                            showError("Recipe not found");
                        }
                    } else {
                        showError("Error loading recipe");
                    }
                }

                @Override
                public void onFailure(Call<RecipeApiResponse> call, Throwable t) {
                    showLoading(false);
                    showError("Error loading recipe");
                }
            });
    }

    private void loadComments(String recipeId) {
        // TODO: Implement comment loading from Firestore
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupFavoriteButton() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            
            // Check if recipe is already favorited
            db.collection("users").document(userId)
                    .collection("favorites")
                    .document(recipeId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        boolean isFavorited = documentSnapshot.exists();
                        favoriteButton.setImageResource(isFavorited ? 
                            R.drawable.ic_favorite : R.drawable.ic_favorite_border);
                    });

            // Setup click listener
            favoriteButton.setOnClickListener(v -> {
                db.collection("users").document(userId)
                        .collection("favorites")
                        .document(recipeId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                // Remove from favorites
                                documentSnapshot.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            favoriteButton.setImageResource(R.drawable.ic_favorite_border);
                                            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                                        });
                            } else if (loadedRecipe != null) {
                                // Add to favorites
                                documentSnapshot.getReference().set(loadedRecipe)
                                        .addOnSuccessListener(aVoid -> {
                                            favoriteButton.setImageResource(R.drawable.ic_favorite);
                                            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        });
            });
        }
    }

    private void setupCommentInput() {
        findViewById(R.id.postCommentButton).setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null) {
                Log.e("RecipeDetailActivity", "User is not authenticated");
                Toast.makeText(this, "Please sign in to comment", Toast.LENGTH_SHORT).show();
                return;
            }

            // Log authentication details
            Log.d("RecipeDetailActivity", "Auth state: " + auth.getCurrentUser().toString());
            Log.d("RecipeDetailActivity", "User ID: " + auth.getCurrentUser().getUid());
            Log.d("RecipeDetailActivity", "User email: " + auth.getCurrentUser().getEmail());
            Log.d("RecipeDetailActivity", "User display name: " + auth.getCurrentUser().getDisplayName());
            Log.d("RecipeDetailActivity", "User is email verified: " + auth.getCurrentUser().isEmailVerified());

            try {
                String token = auth.getCurrentUser().getIdToken(false).getResult().getToken();
                Log.d("RecipeDetailActivity", "Auth token: " + token);
            } catch (Exception e) {
                Log.e("RecipeDetailActivity", "Error getting auth token: " + e.getMessage(), e);
            }

            String commentText = commentInput.getText().toString().trim();
            if (commentText.isEmpty()) {
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            String userName = auth.getCurrentUser().getDisplayName();
            if (userName == null || userName.isEmpty()) {
                userName = "Anonymous User";
            }

            // Debug logging
            Log.d("RecipeDetailActivity", "Current user ID: " + userId);
            Log.d("RecipeDetailActivity", "Recipe ID: " + recipeId);

            // Create comment document with required fields
            Map<String, Object> comment = new HashMap<>();
            comment.put("userId", userId);  // Must match auth.uid
            comment.put("userName", userName);
            comment.put("text", commentText);
            comment.put("timestamp", new Timestamp(new Date()));
            comment.put("recipeId", recipeId);  // Must be a string
            
            // Debug logging
            Log.d("RecipeDetailActivity", "Attempting to add comment with data: " + comment.toString());
            
            // Add comment to the comments collection
            db.collection("comments")
                    .add(comment)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("RecipeDetailActivity", "Comment added successfully with ID: " + documentReference.getId());
                        commentInput.setText("");
                        Toast.makeText(this, "Comment posted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("RecipeDetailActivity", "Error adding comment: " + e.getMessage(), e);
                        if (e instanceof FirebaseFirestoreException) {
                            FirebaseFirestoreException fe = (FirebaseFirestoreException) e;
                            Log.e("RecipeDetailActivity", "Error code: " + fe.getCode());
                        }
                        String errorMessage = "Error posting comment: " + e.getMessage();
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_detail, menu);
        menu.findItem(R.id.action_edit).setVisible(isAuthor);
        menu.findItem(R.id.action_delete).setVisible(isAuthor);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_share) {
            shareRecipe();
            return true;
        } else if (id == R.id.action_edit) {
            editRecipe();
            return true;
        } else if (id == R.id.action_delete) {
            deleteRecipe();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareRecipe() {
        if (loadedRecipe != null) {
            String shareText = loadedRecipe.getStrMeal() + "\n\n" +
                    "Instructions:\n" + loadedRecipe.getStrInstructions();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share Recipe"));
        }
    }

    private void editRecipe() {
        Intent intent = new Intent(this, EditRecipeActivity.class);
        intent.putExtra("recipe_id", recipeId);
        startActivity(intent);
    }

    private void deleteRecipe() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_recipe)
                .setMessage(R.string.delete_recipe_confirmation)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    db.collection("recipes").document(recipeId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, R.string.recipe_deleted, Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
} 