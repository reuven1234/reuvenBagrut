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
import com.example.reuvenbagrut.models.Recipe;
import com.example.reuvenbagrut.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

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
    private User author;
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
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            recipe = document.toObject(Recipe.class);
                            if (recipe != null) {
                                loadAuthorData();
                                loadComments();
                                updateUI();
                                checkIfAuthor();
                            }
                        }
                    }
                });
    }

    private void loadAuthorData() {
        db.collection("users").document(recipe.getAuthorId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            author = document.toObject(User.class);
                            updateAuthorUI();
                        }
                    }
                });
    }

    private void loadComments() {
        db.collection("recipes").document(recipeId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    comments.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            comment.setId(doc.getId());
                            comments.add(comment);
                        }
                    }
                    commentAdapter.updateComments(comments);
                });
    }

    private void updateUI() {
        // Set recipe image
        Glide.with(this)
                .load(recipe.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(recipeImage);

        // Set recipe details
        getSupportActionBar().setTitle(recipe.getTitle());
        description.setText(recipe.getDescription());

        // Format ingredients list
        StringBuilder ingredientsBuilder = new StringBuilder();
        for (String ingredient : recipe.getIngredients()) {
            ingredientsBuilder.append("• ").append(ingredient).append("\n");
        }
        recipeIngredients.setText(ingredientsBuilder.toString());

        // Format instructions
        StringBuilder instructionsBuilder = new StringBuilder();
        String[] instructions = recipe.getInstructions().split("\n");
        for (int i = 0; i < instructions.length; i++) {
            instructionsBuilder.append(i + 1).append(". ").append(instructions[i]).append("\n");
        }
        recipeInstructions.setText(instructionsBuilder.toString());

        // Update favorite button state
        updateFavoriteButtonState();
    }

    private void updateAuthorUI() {
        if (author != null) {
            userName.setText(author.getDisplayName());
            Glide.with(this)
                    .load(author.getProfileImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .circleCrop()
                    .into(userProfileImage);
        }
    }

    private void checkIfAuthor() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        isAuthor = currentUserId.equals(recipe.getAuthorId());
        invalidateOptionsMenu();
    }

    private void setupFavoriteButton() {
        favoriteButton.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (recipe.isLiked()) {
                // Remove from favorites
                db.collection("users").document(userId)
                        .collection("favorites")
                        .document(recipeId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            recipe.setLiked(false);
                            updateFavoriteButtonState();
                        });
            } else {
                // Add to favorites
                db.collection("users").document(userId)
                        .collection("favorites")
                        .document(recipeId)
                        .set(recipe)
                        .addOnSuccessListener(aVoid -> {
                            recipe.setLiked(true);
                            updateFavoriteButtonState();
                        });
            }
        });
    }

    private void updateFavoriteButtonState() {
        if (recipe.isLiked()) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_24);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border_24);
        }
    }

    private void setupCommentInput() {
        findViewById(R.id.postCommentButton).setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Comment comment = new Comment(userId, commentText, System.currentTimeMillis());
                
                db.collection("recipes").document(recipeId)
                        .collection("comments")
                        .add(comment)
                        .addOnSuccessListener(documentReference -> {
                            commentInput.setText("");
                        });
            }
        });
    }

    private void shareRecipe() {
        String shareText = String.format("%s\n\n%s\n\n%s\n\n%s",
                recipe.getTitle(),
                recipe.getDescription(),
                getString(R.string.ingredients) + ":\n" + recipeIngredients.getText(),
                getString(R.string.instructions) + ":\n" + recipeInstructions.getText());

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, recipe.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_recipe)));
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
} 