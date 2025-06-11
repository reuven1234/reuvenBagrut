package com.example.reuvenbagrut.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

        commentAdapter = new CommentAdapter(this, comments,
                c -> startActivity(new Intent(this, UserProfileActivity.class)
                        .putExtra("userId", c.getUserId())));
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);
    }

    private void bindRecipe() {
        collapsingToolbar.setTitle(recipeObj.getStrMeal());
        recipeCategory.setText(recipeObj.getStrCategory());
        Glide.with(this).load(recipeObj.getStrMealThumb()).into(recipeImage);
        recipeInstructions.setText(recipeObj.getStrInstructions());

        StringBuilder sb = new StringBuilder();
        for (String ing : recipeObj.getIngredients()) sb.append("• ").append(ing).append("\n");
        recipeIngredients.setText(sb.toString().trim());

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

        Comment c = new Comment(
                recipeId,
                currentUser.getUid(),
                currentUser.getDisplayName(),
                currentUser.getPhotoUrl() != null
                        ? currentUser.getPhotoUrl().toString()
                        : null,
                txt,
                String.valueOf(System.currentTimeMillis())
        );

        commentInput.setText("");

        db.collection("recipes").document(recipeId)
                .collection("comments").add(c);
    }

    private void loadComments() {
        db.collection("recipes").document(recipeId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null) return;
                    comments.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Comment c = d.toObject(Comment.class);
                        if (c != null) comments.add(c);
                    }
                    commentAdapter.notifyDataSetChanged();
                });
    }
}
