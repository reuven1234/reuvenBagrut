package com.example.reuvenbagrut.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends AppCompatActivity {
    private ImageView profileImage;
    private TextView userName;
    private TextView userEmail;
    private TextView recipeCount;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize views
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        recipeCount = findViewById(R.id.recipeCount);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get user ID from intent
        String userId = getIntent().getStringExtra("userId");
        if (userId != null) {
            loadUserProfile(userId);
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
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .error(R.drawable.ic_profile_placeholder)
                                .into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                        }

                        // Load recipe count
                        db.collection("recipes")
                            .whereEqualTo("userId", userId)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                recipeCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                            });
                    }
                }
            });
    }
} 