package com.example.reuvenbagrut.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.RecipeAdapter;
import com.example.reuvenbagrut.models.Recipe;
import com.example.reuvenbagrut.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {
    private ShapeableImageView profileImage;
    private MaterialTextView userName;
    private MaterialTextView userEmail;
    private MaterialTextView recipeCount;
    private MaterialTextView followersCount;
    private MaterialButton messageButton;
    private RecyclerView recipesRecyclerView;
    private MaterialToolbar toolbar;
    
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipes;
    private String profileUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        recipeCount = findViewById(R.id.recipeCount);
        followersCount = findViewById(R.id.followersCount);
        messageButton = findViewById(R.id.messageButton);
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize Firestore and current user
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize RecyclerView
        recipes = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(this, recipes, recipe -> {
            Intent intent = new Intent(UserProfileActivity.this, RecipeDetailActivity.class);
            intent.putExtra("recipe", recipe);
            startActivity(intent);
        });
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipesRecyclerView.setAdapter(recipeAdapter);

        // Get user ID from intent
        profileUserId = getIntent().getStringExtra("userId");
        if (profileUserId != null) {
            loadUserProfile(profileUserId);
            loadUserRecipes(profileUserId);
            loadFollowersCount(profileUserId);
            setupMessageButton();
        }
    }

    private void loadUserProfile(String userId) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        userName.setText(user.getName());
                        userEmail.setText(user.getEmail());
                        
                        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                            Glide.with(this)
                                .load(user.getProfileImageUrl())
                                .circleCrop()
                                .into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    }
                }
            });
    }

    private void loadUserRecipes(String userId) {
        db.collection("recipes")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null || snapshots == null) return;
                
                recipes.clear();
                for (var doc : snapshots.getDocuments()) {
                    Recipe recipe = doc.toObject(Recipe.class);
                    if (recipe != null) {
                        recipe.setId(doc.getId());
                        recipes.add(recipe);
                    }
                }
                recipeCount.setText(String.valueOf(recipes.size()));
                recipeAdapter.notifyDataSetChanged();
            });
    }

    private void loadFollowersCount(String userId) {
        db.collection("users")
            .document(userId)
            .collection("followers")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                followersCount.setText(String.valueOf(queryDocumentSnapshots.size()));
            });
    }

    private void setupMessageButton() {
        // Only show message button if viewing someone else's profile
        if (currentUser != null && !currentUser.getUid().equals(profileUserId)) {
            messageButton.setVisibility(View.VISIBLE);
            messageButton.setOnClickListener(v -> {
                // Open chat with the user
                Intent chatIntent = new Intent(UserProfileActivity.this, ChatActivity.class);
                chatIntent.putExtra("otherUserId", profileUserId);
                chatIntent.putExtra("otherUserName", userName.getText().toString());
                startActivity(chatIntent);
            });
        } else {
            messageButton.setVisibility(View.GONE);
        }
    }
} 