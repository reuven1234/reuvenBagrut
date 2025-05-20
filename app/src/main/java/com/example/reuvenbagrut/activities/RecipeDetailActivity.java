package com.example.reuvenbagrut.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.CommentAdapter;
import com.example.reuvenbagrut.models.Comment;
import com.example.reuvenbagrut.models.Recipe;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {
    private CollapsingToolbarLayout collapsingToolbar;
    private MaterialToolbar toolbar;
    private ShapeableImageView recipeImage;
    private MaterialTextView recipeCategory;
    private MaterialTextView recipeIngredients;
    private MaterialTextView recipeInstructions;

    private ShapeableImageView userProfileImage;
    private MaterialTextView userName;

    private RecyclerView commentsRecyclerView;
    private TextInputEditText commentInput;
    private MaterialButton postCommentButton;
    private FloatingActionButton favoriteButton;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private CommentAdapter commentAdapter;
    private List<Comment> comments;

    private String recipeId;
    private String recipeUserId;    // ← the UID of who created this recipe

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        initializeViews();

        // Grab the Recipe object passed in
        Recipe recipe = getIntent().getParcelableExtra("recipe");
        if (recipe == null) {
            Toast.makeText(this,
                    R.string.error_recipe_not_found,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        recipeId     = recipe.getId();
        recipeUserId = recipe.getUserId();  // make sure your Recipe model has this!

        // Populate recipe fields
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
            StringBuilder sb = new StringBuilder();
            for (String ing : recipe.getIngredients()) {
                sb.append("• ").append(ing).append("\n");
            }
            recipeIngredients.setText(sb.toString());
        }

        // Show creator’s name & photo
        if (recipe.getUserName() != null) {
            userName.setText(recipe.getUserName());
        }
        if (recipe.getUserImage() != null) {
            Glide.with(this)
                    .load(recipe.getUserImage())
                    .circleCrop()
                    .into(userProfileImage);
        }

        // ➜ Here’s the new bit: clicking name or image opens their profile
        userName.setOnClickListener(v -> openUserProfile());
        userProfileImage.setOnClickListener(v -> openUserProfile());

        // Firestore & UI setup
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        setupFavoriteButton();
        setupCommentInput();
        setupPostCommentButton();
        loadComments();
    }

    private void initializeViews() {
        collapsingToolbar    = findViewById(R.id.collapsingToolbar);
        toolbar              = findViewById(R.id.toolbar);
        recipeImage          = findViewById(R.id.recipeImage);
        recipeCategory       = findViewById(R.id.recipeCategory);
        recipeIngredients    = findViewById(R.id.recipeIngredients);
        recipeInstructions   = findViewById(R.id.recipeInstructions);
        userProfileImage     = findViewById(R.id.userProfileImage);
        userName             = findViewById(R.id.userName);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentInput         = findViewById(R.id.commentInput);
        postCommentButton    = findViewById(R.id.postCommentButton);
        favoriteButton       = findViewById(R.id.favoriteButton);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, comments, comment -> {
            Intent i = new Intent(
                    RecipeDetailActivity.this,
                    UserProfileActivity.class
            );
            i.putExtra("userId", comment.getUserId());
            startActivity(i);
        });
        commentsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );
        commentsRecyclerView.setAdapter(commentAdapter);
    }

    private void openUserProfile() {
        Intent intent = new Intent(
                RecipeDetailActivity.this,
                UserProfileActivity.class
        );
        intent.putExtra("userId", recipeUserId);
        startActivity(intent);
    }

    private void setupFavoriteButton() {
        favoriteButton.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(this,
                        R.string.error_login_to_favorite,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: implement favorite toggling
        });
    }

    private void setupCommentInput() {
        commentInput.setOnEditorActionListener((v, actionId, event) -> {
            String text = commentInput.getText().toString().trim();
            if (!text.isEmpty() && currentUser != null) {
                postComment(text);
            }
            return true;
        });
    }

    private void setupPostCommentButton() {
        postCommentButton.setOnClickListener(v -> {
            String text = commentInput.getText().toString().trim();
            if (!text.isEmpty() && currentUser != null) {
                postComment(text);
            }
        });
    }

    private void postComment(String text) {
        Comment comment = new Comment();
        comment.setRecipeId(recipeId);
        comment.setUserId(currentUser.getUid());
        comment.setUserName(currentUser.getDisplayName());
        comment.setUserImage(
                currentUser.getPhotoUrl() != null
                        ? currentUser.getPhotoUrl().toString()
                        : null
        );
        comment.setContent(text);
        comment.setTimestamp(System.currentTimeMillis());

        db.collection("recipes")
                .document(recipeId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(docRef -> commentInput.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(
                                RecipeDetailActivity.this,
                                R.string.error_posting_comment,
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void loadComments() {
        db.collection("recipes")
                .document(recipeId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, error) -> {
                    if (error != null || snap == null) return;
                    comments.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Comment c = doc.toObject(Comment.class);
                        if (c != null) {
                            c.setId(doc.getId());
                            comments.add(c);
                        }
                    }
                    commentAdapter.notifyDataSetChanged();
                });
    }
}
