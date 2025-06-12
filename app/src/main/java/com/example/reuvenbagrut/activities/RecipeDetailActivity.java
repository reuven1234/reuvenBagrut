package com.example.reuvenbagrut.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.TextView;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbar;
    private MaterialToolbar         toolbar;
    private ShapeableImageView      recipeImage;
    private TextView               recipeName;
    private MaterialTextView        recipeCategory, recipeIngredients, recipeInstructions;
    private ShapeableImageView      userProfileImage;
    private MaterialTextView        userName;
    private MaterialButton          messageCreatorButton;
    private RecyclerView            commentsRecyclerView;
    private TextInputEditText       commentInput;
    private MaterialButton          postCommentButton;
    private FloatingActionButton    favoriteButton;

    private FirebaseFirestore db;
    private FirebaseUser      currentUser;
    private CommentAdapter    commentAdapter;
    private final List<Comment> comments = new ArrayList<>();

    private Recipe  recipeObj;
    private String  recipeId, recipeUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        initViews();

        recipeObj = getIntent().getParcelableExtra("recipe");
        if (recipeObj == null) { finish(); return; }

        recipeId     = recipeObj.getId();
        recipeUserId = recipeObj.getUserId();

        Log.d("RecipeDetailActivity", "Recipe object received: " + recipeObj.getStrMeal());
        Log.d("RecipeDetailActivity", "Recipe ingredients: " + recipeObj.getIngredients());

        bindRecipe();
        db          = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        setupFavoriteButton();
        setupMessageCreatorButton();
        setupCommentInput();
        setupPostCommentButton();
        loadComments();
    }

    /* ---------- UI ---------- */

    private void initViews() {
        collapsingToolbar   = findViewById(R.id.collapsingToolbar);
        toolbar             = findViewById(R.id.toolbar);
        recipeImage         = findViewById(R.id.recipeImage);
        recipeName          = findViewById(R.id.recipeName);
        recipeCategory      = findViewById(R.id.recipeCategory);
        recipeIngredients   = findViewById(R.id.recipeIngredients);
        recipeInstructions  = findViewById(R.id.recipeInstructions);
        userProfileImage    = findViewById(R.id.userProfileImage);
        userName            = findViewById(R.id.userName);
        messageCreatorButton= findViewById(R.id.messageCreatorButton);
        commentsRecyclerView= findViewById(R.id.commentsRecyclerView);
        commentInput        = findViewById(R.id.commentInput);
        postCommentButton   = findViewById(R.id.postCommentButton);
        favoriteButton      = findViewById(R.id.favoriteButton);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize RecyclerView
        commentsRecyclerView.setHasFixedSize(false);  // Changed to false to allow dynamic content
        commentsRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        commentsRecyclerView.setLayoutManager(layoutManager);
        
        commentAdapter = new CommentAdapter(this, comments,
                c -> startActivity(new Intent(this, UserProfileActivity.class)
                        .putExtra("userId", c.getUserId())));
        commentsRecyclerView.setAdapter(commentAdapter);
        
        Log.d("RecipeDetailActivity", "Views initialized, RecyclerView setup complete");
    }

    private void bindRecipe() {
        // Set the recipe name below the photo
        String recipeNameText = recipeObj.getStrMeal();
        Log.d("RecipeDetailActivity", "Setting recipe name: " + recipeNameText);
        
        recipeName.setText(recipeNameText);
        
        recipeCategory.setText(recipeObj.getStrCategory());
        Glide.with(this).load(recipeObj.getStrMealThumb()).into(recipeImage);
        recipeInstructions.setText(recipeObj.getStrInstructions());

        // Log ingredients for debugging
        Log.d("RecipeDetailActivity", "Ingredients array: " + Arrays.toString(recipeObj.getIngredients().toArray()));
        Log.d("RecipeDetailActivity", "Ingredients string: " + recipeObj.getIngredientsString());

        StringBuilder sb = new StringBuilder();
        List<String> ingredients = recipeObj.getIngredients();
        
        // First try to use the ingredients list
        if (ingredients != null && !ingredients.isEmpty()) {
            for (String ing : ingredients) {
                if (ing != null && !ing.trim().isEmpty()) {
                    sb.append("• ").append(ing.trim()).append("\n");
                }
            }
        }
        
        // If no ingredients in list, try to use ingredientsString
        if (sb.length() == 0 && recipeObj.getIngredientsString() != null && !recipeObj.getIngredientsString().trim().isEmpty()) {
            String[] ingredientsArray = recipeObj.getIngredientsString().split(",");
            for (String ing : ingredientsArray) {
                if (ing != null && !ing.trim().isEmpty()) {
                    sb.append("• ").append(ing.trim()).append("\n");
                }
            }
        }
        
        // If still no ingredients, show message
        if (sb.length() == 0) {
            sb.append("No ingredients listed");
        }
        
        String ingredientsText = sb.toString().trim();
        Log.d("RecipeDetailActivity", "Formatted ingredients text: " + ingredientsText);
        recipeIngredients.setText(ingredientsText);

        userName.setText(recipeObj.getUserName());
        Glide.with(this).load(recipeObj.getUserImage()).circleCrop().into(userProfileImage);

        userName.setOnClickListener(v -> openProfile());
        userProfileImage.setOnClickListener(v -> openProfile());
    }

    private void openProfile() {
        startActivity(new Intent(this, UserProfileActivity.class)
                .putExtra("userId", recipeUserId));
    }

    /* ---------- Like ---------- */

    private void setupFavoriteButton() {
        if (currentUser == null) {
            favoriteButton.setOnClickListener(v ->
                    Toast.makeText(this, R.string.error_login_to_favorite, Toast.LENGTH_SHORT).show());
            return;
        }
        String uid = currentUser.getUid();
        updateLikeIcon(uid);

        favoriteButton.setOnClickListener(v -> {
            boolean liked = recipeObj.isLikedByUser(uid);

            // 1. Update the recipe document (likedBy array)
            db.collection("recipes").document(recipeId)
                    .update("likedBy",
                            liked ? FieldValue.arrayRemove(uid)
                                    : FieldValue.arrayUnion(uid))
                    .addOnSuccessListener(a -> {
                        Log.d("RecipeDetailActivity", "Recipe likedBy updated successfully.");
                        if (liked) {
                            recipeObj.removeLike(uid);
                            // 2. Also remove from user's likedRecipes list
                            db.collection("users").document(uid)
                                    .update("likedRecipes", FieldValue.arrayRemove(recipeId));
                        } else {
                            recipeObj.addLike(uid);
                            // 2. Also add to user's likedRecipes list
                            db.collection("users").document(uid)
                                    .update("likedRecipes", FieldValue.arrayUnion(recipeId))
                                    .addOnSuccessListener(aVoid -> Log.d("RecipeDetailActivity", "User likedRecipes updated successfully (added)."))
                                    .addOnFailureListener(e -> Log.e("RecipeDetailActivity", "Error updating user likedRecipes (add): " + e.getMessage()));
                        }
                        updateLikeIcon(uid);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show());
        });
    }
    private void updateLikeIcon(String uid) {
        favoriteButton.setImageResource(
                recipeObj.isLikedByUser(uid) ? R.drawable.ic_favorite_24
                        : R.drawable.ic_favorite_border_24);
    }

    /* ---------- Chat ---------- */

    private void setupMessageCreatorButton() {
        if (currentUser == null || recipeUserId == null || currentUser.getUid().equals(recipeUserId)) {
            messageCreatorButton.setVisibility(android.view.View.GONE);
            return;
        }
        messageCreatorButton.setVisibility(android.view.View.VISIBLE);
        messageCreatorButton.setOnClickListener(v -> openOrCreateChat());
    }

    private void openOrCreateChat() {
        String myUid    = currentUser.getUid();
        String otherUid = recipeUserId;
        if (myUid.equals(otherUid)) {
            Toast.makeText(this, R.string.cannot_chat_with_self, Toast.LENGTH_SHORT).show();
            return;
        }
        String chatId = createChatId(myUid, otherUid);

        // First, get the other user's name from the 'users' collection
        db.collection("users").document(otherUid).get()
                .addOnSuccessListener(userDoc -> {
                    String fetchedOtherUserName = userDoc.getString("name");
                    final String finalOtherUserName = (fetchedOtherUserName == null || fetchedOtherUserName.trim().isEmpty())
                                                        ? "Unknown User" : fetchedOtherUserName; // Make it effectively final here

                    db.collection("chats").document(chatId).get()
                            .addOnSuccessListener(chatDoc -> {
                                if (!chatDoc.exists()) {
                                    HashMap<String,Object> data = new HashMap<>();
                                    data.put("participants", Arrays.asList(myUid, otherUid));
                                    data.put("lastMessage", "");
                                    data.put("lastMessageTime", 0L);
                                    data.put("otherName", finalOtherUserName); // Use final variable
                                    db.collection("chats").document(chatId).set(data);
                                }
                                startActivity(new Intent(this, ChatActivity.class)
                                        .putExtra("chatId",        chatId)
                                        .putExtra("otherUserId",   otherUid)
                                        .putExtra("otherUserName", finalOtherUserName)); // Pass final variable
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, R.string.error_open_chat, Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String createChatId(String uid1, String uid2) {
        String[] u = {uid1, uid2};
        Arrays.sort(u);
        return u[0] + "_" + u[1];
    }

    /* ---------- Comments ---------- */

    private void setupCommentInput() {
        commentInput.setOnEditorActionListener((v, id, ev) -> { send(); return true; });
    }

    private void setupPostCommentButton() { postCommentButton.setOnClickListener(v -> send()); }

    private void send() {
        String txt = commentInput.getText().toString().trim();
        if (txt.isEmpty() || currentUser == null) return;

        // Create a new document reference to get the ID
        db.collection("recipes").document(recipeId)
                .collection("comments").add(new Comment(
                        null, // ID will be set by Firestore
                        recipeId,
                        currentUser.getUid(),
                        currentUser.getDisplayName(),
                        currentUser.getPhotoUrl() != null
                                ? currentUser.getPhotoUrl().toString()
                                : null,
                        txt))
                .addOnSuccessListener(documentReference -> {
                    // Update the comment with its document ID
                    documentReference.update("id", documentReference.getId())
                            .addOnSuccessListener(aVoid -> {
                                commentInput.setText("");
                                Toast.makeText(this, "Comment posted successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update comment ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("RecipeDetailActivity", "Error updating comment ID", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to post comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("RecipeDetailActivity", "Error posting comment", e);
                });
    }

    private void loadComments() {
        Log.d("RecipeDetailActivity", "Starting to load comments for recipe: " + recipeId);
        db.collection("recipes").document(recipeId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        Log.e("RecipeDetailActivity", "Error loading comments: " + err.getMessage());
                        Toast.makeText(this, "Error loading comments: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snap == null) {
                        Log.e("RecipeDetailActivity", "Snapshot is null");
                        return;
                    }
                    
                    Log.d("RecipeDetailActivity", "Received " + snap.getDocuments().size() + " comments");
                    comments.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Comment c = d.toObject(Comment.class);
                        if (c != null) {
                            // Set the ID from the document
                            c.setId(d.getId());
                            comments.add(c);
                            Log.d("RecipeDetailActivity", "Added comment: " + c.getContent());
                        } else {
                            Log.e("RecipeDetailActivity", "Failed to convert document to Comment object");
                        }
                    }
                    commentAdapter.notifyDataSetChanged();
                    Log.d("RecipeDetailActivity", "Comments list size: " + comments.size());
                });
    }
}
