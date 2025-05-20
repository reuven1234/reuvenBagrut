package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.adapters.RecipeAdapter;
import com.example.reuvenbagrut.models.Recipe;
import com.example.reuvenbagrut.models.User;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    private ImageView profileImage;
    private TextView nameText;
    private TextView bioText;
    private RecyclerView recipesRecyclerView;
    private RecipeAdapter recipeAdapter;
    private TabLayout tabLayout;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        setupToolbar();
        initializeViews();
        setupRecyclerView();
        setupTabLayout();

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = getIntent().getStringExtra("user_id");

        if (userId == null && currentUser != null) {
            userId = currentUser.getUid();
        }

        loadUserProfile();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.profile);
        }
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profileImage);
        bioText = findViewById(R.id.bioText);
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        tabLayout = findViewById(R.id.tabLayout);
    }

    private void setupRecyclerView() {
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeAdapter = new RecipeAdapter(this, new ArrayList<>(), null);
        recipesRecyclerView.setAdapter(recipeAdapter);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadUploadedRecipes();
                } else {
                    loadLikedRecipes();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadUserProfile() {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateUI(documentSnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private void updateUI(DocumentSnapshot userDoc) {
        String name = userDoc.getString("name");
        String bio = userDoc.getString("bio");
        String photoUrl = userDoc.getString("photoUrl");

        nameText.setText(name != null ? name : "Anonymous");
        bioText.setText(bio != null ? bio : "No bio yet");

        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }

    private void loadUploadedRecipes() {
        db.collection("recipes")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Recipe recipe = doc.toObject(Recipe.class);
                        if (recipe != null) {
                            recipe.setId(doc.getId());
                            recipes.add(recipe);
                        }
                    }
                    recipeAdapter.setRecipes(recipes);
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private void loadLikedRecipes() {
        db.collection("recipes")
                .whereArrayContains("likedBy", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Recipe recipe = doc.toObject(Recipe.class);
                        if (recipe != null) {
                            recipe.setId(doc.getId());
                            recipes.add(recipe);
                        }
                    }
                    recipeAdapter.setRecipes(recipes);
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
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