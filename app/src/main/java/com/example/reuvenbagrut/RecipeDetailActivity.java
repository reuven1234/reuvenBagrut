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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private CollapsingToolbarLayout collapsingToolbar;

    private String recipeId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private boolean isLiked = false;
    private CommentAdapter commentAdapter;
    private List<Comment> comments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        initializeViews();

        // Get recipeId and details from intent
        recipeId = getIntent().getStringExtra("recipe_id");
        if (recipeId == null) {
            Snackbar.make(findViewById(android.R.id.content), "Recipe not found", Snackbar.LENGTH_SHORT).show();
            finish();
            return;
        }
        bindFromExtras();

        // Init Firebase for comments/likes
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        auth.addAuthStateListener(firebaseAuth -> {
            currentUser = firebaseAuth.getCurrentUser();
            updateUIForAuthState();
            if (currentUser != null) loadComments();
        });
        setupComments();
        setupLikeButton();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Binds recipe details passed via Intent extras into the UI.
     */
    private void bindFromExtras() {
        // Title
        String title = getIntent().getStringExtra("recipe_title");
        collapsingToolbar.setTitle(title != null ? title : "Recipe Details");
        // Category
        String category = getIntent().getStringExtra("recipe_category");
        recipeCategory.setText(category != null ? category : "");
        // Ingredients (new)
        String ingredients = getIntent().getStringExtra("recipe_ingredients");
        recipeIngredients.setText(ingredients != null ? ingredients : "No ingredients listed");
        // Instructions
        String instructions = getIntent().getStringExtra("recipe_instructions");
        recipeInstructions.setText(instructions != null ? instructions : "");
        // Image
        String imageUrl = getIntent().getStringExtra("recipe_image");
        Glide.with(this)
                .load(imageUrl)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image))
                .into(recipeImage);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = auth.getCurrentUser();
        updateUIForAuthState();
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

    private void updateUIForAuthState() {
        boolean signedIn = currentUser != null;
        commentInput.setEnabled(signedIn);
        postCommentButton.setEnabled(signedIn);
        commentInput.setHint(signedIn ? R.string.add_comment : R.string.sign_in_to_comment);
        favoriteButton.setVisibility(signedIn ? View.VISIBLE : View.GONE);
    }

    private void setupComments() {
        commentAdapter = new CommentAdapter(comments);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);
        postCommentButton.setOnClickListener(v -> postComment());
    }

    private void loadComments() {
        comments.clear(); commentAdapter.notifyDataSetChanged(); showLoading(true);
        db.collection("recipes").document(recipeId)
                .collection("comments").orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    showLoading(false);
                    if (error != null) { handleCommentError(error); return; }
                    comments.clear();
                    for (QueryDocumentSnapshot d : snapshot) {
                        Comment c = d.toObject(Comment.class);
                        c.setId(d.getId()); comments.add(c);
                    }
                    commentAdapter.notifyDataSetChanged();
                });
    }

    private void handleCommentError(FirebaseFirestoreException e) {
        Log.e(TAG, "Error loading comments", e);
        String msg = currentUser == null ? "Please sign in to view comments" : "Error loading comments.";
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
                .setAction(currentUser == null ? "Sign In" : null, v -> {
                    if (currentUser == null) startActivity(new Intent(this, Login.class));
                }).show();
    }

    private void postComment() {
        if (currentUser == null) {
            Snackbar.make(findViewById(android.R.id.content), "Please sign in to comment", Snackbar.LENGTH_LONG)
                    .setAction("Sign In", v -> startActivity(new Intent(this, Login.class))).show();
            return;
        }
        String text = commentInput.getText().toString().trim();
        if (text.isEmpty()) { commentInput.setError("Cannot be empty"); return; }
        postCommentButton.setEnabled(false);
        Map<String,Object> data = new HashMap<>();
        data.put("text", text);
        data.put("userId", currentUser.getUid());
        data.put("userName", currentUser.getDisplayName()!=null?currentUser.getDisplayName():"Anonymous");
        data.put("userPhotoUrl", currentUser.getPhotoUrl()!=null?currentUser.getPhotoUrl().toString():null);
        data.put("timestamp", System.currentTimeMillis());
        db.collection("recipes").document(recipeId).collection("comments")
                .add(data).addOnSuccessListener(docRef -> {
                    commentInput.setText(""); postCommentButton.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content), "Comment posted", Snackbar.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    postCommentButton.setEnabled(true);
                    if (e instanceof FirebaseFirestoreException && ((FirebaseFirestoreException)e).getCode()==FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Snackbar.make(findViewById(android.R.id.content), "Error posting comment", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void setupLikeButton() {
        if (currentUser == null) { favoriteButton.setVisibility(View.GONE); return; }
        db.collection("recipes").document(recipeId).collection("likes")
                .document(currentUser.getUid()).get()
                .addOnSuccessListener(doc -> { isLiked = doc.exists(); updateLikeButton(); });
        favoriteButton.setOnClickListener(v -> toggleLike());
    }

    private void updateLikeButton() {
        favoriteButton.setImageResource(isLiked?R.drawable.ic_favorite:R.drawable.ic_favorite_border);
    }

    private void toggleLike() {
        if (currentUser == null) return;
        Map<String,Object> d = new HashMap<>(); d.put("timestamp",System.currentTimeMillis());
        if (isLiked) {
            db.collection("recipes").document(recipeId).collection("likes").document(currentUser.getUid())
                    .delete().addOnSuccessListener(a-> {isLiked=false; updateLikeButton(); Snackbar.make(findViewById(android.R.id.content), "Recipe unliked", Snackbar.LENGTH_SHORT).show();});
        } else {
            db.collection("recipes").document(recipeId).collection("likes").document(currentUser.getUid())
                    .set(d).addOnSuccessListener(a-> {isLiked=true; updateLikeButton(); Snackbar.make(findViewById(android.R.id.content), "Recipe liked", Snackbar.LENGTH_SHORT).show();});
        }
    }

    private void showLoading(boolean show) { if (progressIndicator != null) progressIndicator.setVisibility(show?View.VISIBLE:View.GONE); }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { if (item.getItemId()==android.R.id.home) { onBackPressed(); return true;} return super.onOptionsItemSelected(item);}
}
