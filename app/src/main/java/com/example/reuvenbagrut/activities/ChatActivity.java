package com.example.reuvenbagrut.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.reuvenbagrut.fragments.ChatFragment;
import com.example.reuvenbagrut.R;
import android.view.MenuItem;
import android.widget.TextView;
import android.util.Base64;

import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;
import android.util.Log;

public class ChatActivity extends AppCompatActivity {
    private String chatId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserProfileImage;

    // Firebase
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();

        // Get data from intent
        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");
        otherUserProfileImage = getIntent().getStringExtra("otherUserProfileImage");

        if (chatId == null || otherUserId == null) {
            finish();
            return;
        }

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
            getSupportActionBar().setTitle(""); // Remove default title
        }

        // Set custom views in Toolbar
        TextView toolbarUserName = findViewById(R.id.toolbarUserName);
        CircleImageView toolbarProfileImage = findViewById(R.id.toolbarProfileImage);

        toolbarUserName.setText(otherUserName);

        // Load other user's profile image using the passed Base64 string
        if (otherUserProfileImage != null && !otherUserProfileImage.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(otherUserProfileImage, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Glide.with(this).load(decodedByte).circleCrop().into(toolbarProfileImage);
            } catch (IllegalArgumentException e) {
                Log.e("ChatActivity", "Error decoding Base64 image from Intent: " + e.getMessage());
                // Fallback to fetching from Firestore if decoding fails
                fetchImageFromFirestore(otherUserId, toolbarProfileImage);
            }
        } else {
            // If Base64 string is null or empty from Intent, fetch from Firestore
            fetchImageFromFirestore(otherUserId, toolbarProfileImage);
        }

        // Add ChatFragment
        if (savedInstanceState == null) {
            ChatFragment fragment = ChatFragment.newInstance(chatId, otherUserId, otherUserName);
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        }
    }

    private void fetchImageFromFirestore(String userId, CircleImageView imageView) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User otherUser = documentSnapshot.toObject(User.class);
                    if (otherUser != null && otherUser.getProfileImageUrl() != null && !otherUser.getProfileImageUrl().isEmpty()) {
                        try {
                            byte[] decodedString = Base64.decode(otherUser.getProfileImageUrl(), Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            Glide.with(this).load(decodedByte).circleCrop().into(imageView);
                        } catch (IllegalArgumentException e) {
                            Log.e("ChatActivity", "Error decoding fetched Base64 image: " + e.getMessage());
                            imageView.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    } else {
                        imageView.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatActivity", "Error fetching user profile image from Firestore: " + e.getMessage());
                    imageView.setImageResource(R.drawable.ic_profile_placeholder);
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle back button click
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
