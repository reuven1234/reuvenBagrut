package com.example.reuvenbagrut.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.databinding.ActivityEditProfileBinding;
import com.example.reuvenbagrut.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        setupToolbar();
        setupImagePicker();
        loadUserData();
        setupSaveButton();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.edit_profile);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this)
                        .load(uri)
                        .circleCrop()
                        .into(binding.profileImage);
                }
            }
        );

        binding.profileImage.setOnClickListener(v -> 
            imagePickerLauncher.launch("image/*")
        );
    }

    private void loadUserData() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    binding.usernameInput.setText(user.getDisplayName());
                    binding.bioInput.setText(user.getBio());
                    if (user.getPhotoUrl() != null) {
                        Glide.with(this)
                            .load(user.getPhotoUrl())
                            .circleCrop()
                            .into(binding.profileImage);
                    }
                }
            });
    }

    private void setupSaveButton() {
        binding.saveButton.setOnClickListener(v -> {
            String username = binding.usernameInput.getText().toString().trim();
            String bio = binding.bioInput.getText().toString().trim();

            if (username.isEmpty()) {
                binding.usernameInput.setError(getString(R.string.username_required));
                return;
            }

            binding.saveButton.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);

            if (selectedImageUri != null) {
                uploadImageAndSaveProfile(username, bio);
            } else {
                saveProfile(username, bio, null);
            }
        });
    }

    private void uploadImageAndSaveProfile(String username, String bio) {
        String userId = auth.getCurrentUser().getUid();
        StorageReference imageRef = storage.getReference()
            .child("profile_images")
            .child(userId + ".jpg");

        imageRef.putFile(selectedImageUri)
            .continueWithTask(task -> imageRef.getDownloadUrl())
            .addOnSuccessListener(uri -> saveProfile(username, bio, uri.toString()))
            .addOnFailureListener(e -> {
                Toast.makeText(this, R.string.error_uploading_image, Toast.LENGTH_SHORT).show();
                binding.saveButton.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
            });
    }

    private void saveProfile(String username, String bio, String imageUrl) {
        String userId = auth.getCurrentUser().getUid();
        User user = new User();
        user.setId(userId);
        user.setDisplayName(username);
        user.setBio(bio);
        if (imageUrl != null) user.setPhotoUrl(imageUrl);
        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, R.string.error_updating_profile, Toast.LENGTH_SHORT).show();
                binding.saveButton.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
            });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 