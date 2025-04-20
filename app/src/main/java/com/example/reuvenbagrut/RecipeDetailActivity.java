package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

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
    private RecyclerView commentsRecyclerView;
    private TextInputEditText commentInput;
    private MaterialButton postCommentButton;
    private FloatingActionButton favoriteButton;
    
    private String recipeId;
    private String authorId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private CollapsingToolbarLayout collapsingToolbar;
    private boolean isLiked = false;
    private CommentAdapter commentAdapter;
    private List<Comment> comments = new ArrayList<>();

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
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Initialize views
        initializeViews();
        setupComments();
        setupLikeButton();

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

    @Override
    protected void onStart() {
        super.onStart();
        // Update current user when activity starts
        currentUser = auth.getCurrentUser();
        updateUIForAuthState();
    }

    private void updateUIForAuthState() {
        if (currentUser == null) {
            // User is not signed in
            postCommentButton.setEnabled(false);
            commentInput.setEnabled(false);
            commentInput.setHint(R.string.sign_in_to_comment);
            favoriteButton.setVisibility(View.GONE);
        } else {
            // User is signed in
            postCommentButton.setEnabled(true);
            commentInput.setEnabled(true);
            commentInput.setHint(R.string.add_comment);
            favoriteButton.setVisibility(View.VISIBLE);
            setupLikeButton();
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recipeImage = findViewById(R.id.recipeImage);
        recipeCategory = findViewById(R.id.description);
        recipeIngredients = findViewById(R.id.recipeIngredients);
        recipeInstructions = findViewById(R.id.recipeInstructions);
        authorImage = findViewById(R.id.userProfileImage);
        authorName = findViewById(R.id.userName);
        progressIndicator = findViewById(R.id.progressIndicator);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentInput = findViewById(R.id.commentInput);
        postCommentButton = findViewById(R.id.postCommentButton);
        favoriteButton = findViewById(R.id.favoriteButton);
    }

    private void setupComments() {
        commentAdapter = new CommentAdapter(comments);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);
        
        postCommentButton.setOnClickListener(v -> postComment());
        
        // Load comments
        loadComments();
    }

    private void setupLikeButton() {
        if (currentUser == null) {
            favoriteButton.setVisibility(View.GONE);
            return;
        }

        // Check if recipe is already liked
        db.collection("recipes").document(recipeId)
            .collection("likes")
            .document(currentUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                isLiked = documentSnapshot.exists();
                updateLikeButton();
            });

        favoriteButton.setOnClickListener(v -> toggleLike());
    }

    private void updateLikeButton() {
        favoriteButton.setImageResource(isLiked ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
    }

    private void toggleLike() {
        if (currentUser == null) return;

        if (isLiked) {
            // Unlike
            db.collection("recipes").document(recipeId)
                .collection("likes")
                .document(currentUser.getUid())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    isLiked = false;
                    updateLikeButton();
                    Snackbar.make(findViewById(android.R.id.content), "Recipe unliked", Snackbar.LENGTH_SHORT).show();
                });
        } else {
            // Like
            Map<String, Object> like = new HashMap<>();
            like.put("timestamp", System.currentTimeMillis());
            
            db.collection("recipes").document(recipeId)
                .collection("likes")
                .document(currentUser.getUid())
                .set(like)
                .addOnSuccessListener(aVoid -> {
                    isLiked = true;
                    updateLikeButton();
                    Snackbar.make(findViewById(android.R.id.content), "Recipe liked", Snackbar.LENGTH_SHORT).show();
                });
        }
    }

    private void loadComments() {
        if (recipeId == null) {
            Log.e(TAG, "Recipe ID is null");
            return;
        }

        // Initialize comments list if null
        if (comments == null) {
            comments = new ArrayList<>();
        }

        try {
            db.collection("recipes").document(recipeId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        if (error.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            // Handle permission denied error
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Please sign in to view comments",
                                    Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Error loading comments",
                                    Snackbar.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        comments.clear();
                        for (QueryDocumentSnapshot document : snapshots) {
                            try {
                                Comment comment = document.toObject(Comment.class);
                                comment.setId(document.getId());
                                comments.add(comment);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing comment: " + document.getId(), e);
                            }
                        }
                        commentAdapter.notifyDataSetChanged();
                    } else {
                        comments.clear();
                        commentAdapter.notifyDataSetChanged();
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up comments listener", e);
            Snackbar.make(findViewById(android.R.id.content),
                    "Error loading comments",
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void postComment() {
        if (currentUser == null) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please sign in to comment",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        String commentText = commentInput.getText().toString().trim();
        if (commentText.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please enter a comment",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Disable the post button while comment is being posted
        postCommentButton.setEnabled(false);

        Comment comment = new Comment(
            currentUser.getUid(),
            currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous",
            currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "",
            commentText
        );

        db.collection("recipes").document(recipeId)
            .collection("comments")
            .add(comment)
            .addOnSuccessListener(documentReference -> {
                commentInput.setText("");
                Snackbar.make(findViewById(android.R.id.content),
                        "Comment posted",
                        Snackbar.LENGTH_SHORT).show();
                postCommentButton.setEnabled(true);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error posting comment", e);
                Snackbar.make(findViewById(android.R.id.content),
                        "Error posting comment",
                        Snackbar.LENGTH_SHORT).show();
                postCommentButton.setEnabled(true);
            });
    }

    private void loadRecipeData() {
        if (recipeId == null) {
            showError("Recipe ID not found");
            finish();
            return;
        }

        showLoading(true);

        try {
            db.collection("recipes").document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    
                    if (!documentSnapshot.exists()) {
                        showError("Recipe not found");
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
                        
                        if (title == null || title.isEmpty()) {
                            showError("Invalid recipe data");
                            finish();
                            return;
                        }
                        
                        // Update UI
                        collapsingToolbar.setTitle(title);
                        recipeCategory.setText(category != null ? category : "Uncategorized");
                        recipeInstructions.setText(instructions != null ? instructions : "No instructions available");
                        
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
                        } else {
                            recipeImage.setImageResource(R.drawable.placeholder_image);
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
                        } else {
                            authorImage.setImageResource(R.drawable.avatar_placeholder);
                        }
                        
                        authorName.setText(authorNameText != null ? authorNameText : "Anonymous");
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing recipe data", e);
                        showError("Error loading recipe details");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading recipe", e);
                    showError("Error loading recipe");
                    finish();
                });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firestore", e);
            showError("Error connecting to database");
            finish();
        }
    }

    private void showLoading(boolean show) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        showLoading(false);
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setAction("Retry", v -> loadRecipeData())
            .show();
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