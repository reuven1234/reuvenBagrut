package com.example.reuvenbagrut;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;

public class UserProfileActivity extends AppCompatActivity {
    private ImageView userProfileImage;
    private TextView userName;
    private TextView userBio;
    private TextView followersCount;
    private TextView followingCount;
    private TextView recipesCount;
    private Button followButton;
    private ImageButton backButton;
    private RecyclerView userRecipesRecyclerView;
    private RecipeAdapter recipeAdapter;
    
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String profileUserId;
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        profileUserId = getIntent().getStringExtra("user_id");

        initializeViews();
        loadUserProfile();
        loadUserRecipes();
        checkFollowStatus();

        // Set up back button
        backButton.setOnClickListener(v -> finish());

        // Set up follow button
        followButton.setOnClickListener(v -> toggleFollow());
    }

    private void initializeViews() {
        userProfileImage = findViewById(R.id.userProfileImage);
        userName = findViewById(R.id.userName);
        userBio = findViewById(R.id.userBio);
        followersCount = findViewById(R.id.followersCount);
        followingCount = findViewById(R.id.followingCount);
        recipesCount = findViewById(R.id.recipesCount);
        followButton = findViewById(R.id.followButton);
        backButton = findViewById(R.id.backButton);
        userRecipesRecyclerView = findViewById(R.id.userRecipesRecyclerView);

        // Set up RecyclerView
        userRecipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeAdapter = new RecipeAdapter(this);
        userRecipesRecyclerView.setAdapter(recipeAdapter);
    }

    private void loadUserProfile() {
        db.collection("users").document(profileUserId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    updateUI(documentSnapshot);
                }
            });
    }

    private void updateUI(DocumentSnapshot userDoc) {
        String name = userDoc.getString("name");
        String bio = userDoc.getString("bio");
        String imageUrl = userDoc.getString("profileImage");
        Long followers = userDoc.getLong("followers");
        Long following = userDoc.getLong("following");

        userName.setText(name);
        userBio.setText(bio);
        followersCount.setText(String.valueOf(followers != null ? followers : 0));
        followingCount.setText(String.valueOf(following != null ? following : 0));

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .into(userProfileImage);
        }

        // Hide follow button if viewing own profile
        if (currentUser != null && currentUser.getUid().equals(profileUserId)) {
            followButton.setVisibility(View.GONE);
        }
    }

    private void loadUserRecipes() {
        db.collection("recipes")
            .whereEqualTo("userId", profileUserId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                ArrayList<Recipe> recipes = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Recipe recipe = doc.toObject(Recipe.class);
                    if (recipe != null) {
                        recipes.add(recipe);
                    }
                }
                // recipeAdapter.setRecipes(recipes); // Commented out due to type mismatch
                recipesCount.setText(String.valueOf(recipes.size()));
            });
    }

    private void checkFollowStatus() {
        if (currentUser == null || currentUser.getUid().equals(profileUserId)) {
            return;
        }

        db.collection("users")
            .document(currentUser.getUid())
            .collection("following")
            .document(profileUserId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                isFollowing = documentSnapshot.exists();
                updateFollowButton();
            });
    }

    private void updateFollowButton() {
        followButton.setText(isFollowing ? "Following" : "Follow");
    }

    private void toggleFollow() {
        if (currentUser == null) {
            return;
        }

        String currentUserId = currentUser.getUid();
        if (isFollowing) {
            // Unfollow
            db.collection("users").document(currentUserId)
                .collection("following").document(profileUserId).delete();
            db.collection("users").document(profileUserId)
                .collection("followers").document(currentUserId).delete();
        } else {
            // Follow
            db.collection("users").document(currentUserId)
                .collection("following").document(profileUserId).set(new HashMap<>());
            db.collection("users").document(profileUserId)
                .collection("followers").document(currentUserId).set(new HashMap<>());
        }

        isFollowing = !isFollowing;
        updateFollowButton();
        updateFollowerCount(isFollowing ? 1 : -1);
    }

    private void updateFollowerCount(int change) {
        db.collection("users").document(profileUserId)
            .update("followers", com.google.firebase.firestore.FieldValue.increment(change));
        
        int currentCount = Integer.parseInt(followersCount.getText().toString());
        followersCount.setText(String.valueOf(currentCount + change));
    }
} 