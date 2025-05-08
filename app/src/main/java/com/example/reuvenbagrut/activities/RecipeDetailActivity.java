package com.example.reuvenbagrut.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.CommentAdapter;
import com.example.reuvenbagrut.models.Comment;
import com.example.reuvenbagrut.Recipe;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeDetailActivity extends AppCompatActivity {
    private com.google.android.material.imageview.ShapeableImageView recipeImage;
    private com.google.android.material.imageview.ShapeableImageView userProfileImage;
    private com.google.android.material.textview.MaterialTextView userName;
    private com.google.android.material.textview.MaterialTextView description;
    private com.google.android.material.textview.MaterialTextView recipeIngredients;
    private com.google.android.material.textview.MaterialTextView recipeInstructions;
    private RecyclerView commentsRecyclerView;
    private TextInputEditText commentInput;
    private FloatingActionButton favoriteButton;
    private FirebaseFirestore db;
    private String recipeId;
    private Recipe recipe;
    private CommentAdapter commentAdapter;
    private List<Comment> comments;
    private boolean isAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Get recipe ID from intent
        recipeId = getIntent().getStringExtra("recipe_id");
        if (recipeId == null) {
            finish();
            return;
        }

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recipeImage = findViewById(R.id.recipeImage);
        userProfileImage = findViewById(R.id.userProfileImage);
        userName = findViewById(R.id.userName);
        description = findViewById(R.id.description);
        recipeIngredients = findViewById(R.id.recipeIngredients);
        recipeInstructions = findViewById(R.id.recipeInstructions);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentInput = findViewById(R.id.commentInput);
        favoriteButton = findViewById(R.id.favoriteButton);

        // Setup comments RecyclerView
        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(comments);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);

        // Load recipe data
        loadRecipeData();

        // Setup favorite button
        setupFavoriteButton();

        // Setup comment input
        setupCommentInput();
    }

    private void loadRecipeData() {
        db.collection("recipes").document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    recipe = documentSnapshot.toObject(Recipe.class);
                    if (recipe != null) {
                        // Set recipe data
                        getSupportActionBar().setTitle(recipe.getStrMeal());
                        description.setText(recipe.getStrInstructions());
                        
                        // Format ingredients list
                        StringBuilder ingredientsBuilder = new StringBuilder();
                        for (String ingredient : recipe.getIngredients()) {
                            ingredientsBuilder.append("• ").append(ingredient).append("\n");
                        }
                        recipeIngredients.setText(ingredientsBuilder.toString());
                        
                        // Format instructions
                        StringBuilder instructionsBuilder = new StringBuilder();
                        String[] instructions = recipe.getStrInstructions().split("\n");
                        for (int i = 0; i < instructions.length; i++) {
                            instructionsBuilder.append(i + 1).append(". ").append(instructions[i]).append("\n");
                        }
                        recipeInstructions.setText(instructionsBuilder.toString());

                        // Load recipe image
                        if (recipe.getStrMealThumb() != null && !recipe.getStrMealThumb().isEmpty()) {
                            Glide.with(this)
                                    .load(recipe.getStrMealThumb())
                                    .centerCrop()
                                    .into(recipeImage);
                        }

                        // Load author info
                        if (recipe.getStrAuthor() != null) {
                            userName.setText(recipe.getStrAuthor());
                        }

                        // Load author image if available
                        if (recipe.getStrAuthorImage() != null && !recipe.getStrAuthorImage().isEmpty()) {
                            Glide.with(this)
                                    .load(recipe.getStrAuthorImage())
                                    .centerCrop()
                                    .into(userProfileImage);
                        }

                        // Check if current user is the author
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        if (auth.getCurrentUser() != null) {
                            isAuthor = auth.getCurrentUser().getUid().equals(recipe.getUserId());
                        }

                        // Load comments
                        loadComments();
                    }
                });
    }

    private void loadComments() {
        db.collection("recipes").document(recipeId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        comments.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Comment comment = new Comment();
                            comment.setId(doc.getId());
                            comment.setUserId(doc.getString("userId"));
                            comment.setText(doc.getString("text"));
                            comment.setTimestamp(doc.getTimestamp("timestamp").toDate());
                            comments.add(comment);
                        }
                        commentAdapter.updateComments(comments);
                    }
                });
    }

    private void setupCommentInput() {
        findViewById(R.id.postCommentButton).setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() != null) {
                    String userId = auth.getCurrentUser().getUid();
                    
                    Map<String, Object> comment = new HashMap<>();
                    comment.put("userId", userId);
                    comment.put("text", commentText);
                    comment.put("timestamp", new Timestamp(new Date()));
                    
                    db.collection("recipes").document(recipeId)
                            .collection("comments")
                            .add(comment)
                            .addOnSuccessListener(documentReference -> {
                                commentInput.setText("");
                                Toast.makeText(this, "Comment posted successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error posting comment", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "Please sign in to comment", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                            } else {
                                // Add to favorites
                                documentSnapshot.getReference().set(recipe)
                                        .addOnSuccessListener(aVoid -> {
                                            favoriteButton.setImageResource(R.drawable.ic_favorite);
                                            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        });
            });
        }
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
        if (recipe != null) {
            String shareText = recipe.getStrMeal() + "\n\n" +
                    "Ingredients:\n" + String.join("\n", recipe.getIngredients()) + "\n\n" +
                    "Instructions:\n" + recipe.getStrInstructions();

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