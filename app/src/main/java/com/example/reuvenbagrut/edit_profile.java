package com.example.reuvenbagrut;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class edit_profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;

    // Views for image handling
    private ShapeableImageView profileImage;
    private Uri imageUri;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;

    // Views for text fields
    private EditText editTextName, editTextBio, editTextPassword;
    private Button buttonSaveChanges;
    private ImageButton buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize image views and buttons
        profileImage = findViewById(R.id.profileImage);
        Button uploadGalleryButton = findViewById(R.id.uploadGalleryButton);
        Button takePictureButton = findViewById(R.id.takePictureButton);

        // Initialize text fields and save button
        editTextName = findViewById(R.id.editTextName);
        editTextBio = findViewById(R.id.editTextBio);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);
        buttonBack = findViewById(R.id.buttonBack);

        // Firebase setup
        storageReference = FirebaseStorage.getInstance().getReference("profile_pictures");
        firestore = FirebaseFirestore.getInstance();

        // Set click listeners for image update buttons
        uploadGalleryButton.setOnClickListener(v -> openGallery());
        takePictureButton.setOnClickListener(v -> openCamera());

        // Set click listener for saving profile changes
        buttonSaveChanges.setOnClickListener(v -> updateUserProfile());

        // Set click listener for back button to cancel editing
        buttonBack.setOnClickListener(v -> finish());
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST && data.getData() != null) {
                imageUri = data.getData();
                profileImage.setImageURI(imageUri);
                uploadImageToFirebase();
            } else if (requestCode == CAMERA_REQUEST && data.getExtras() != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                profileImage.setImageBitmap(photo);
                imageUri = getImageUri(photo); // Convert Bitmap to URI
                uploadImageToFirebase();
            }
        }
    }

    // Placeholder method – in practice, convert Bitmap to a URI by saving the file locally
    private Uri getImageUri(Bitmap bitmap) {
        // Implement conversion here (e.g., using a FileProvider)
        return null;
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                saveImageUrlToFirestore(imageUrl);
                            }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(edit_profile.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        // Replace "USER_ID" with the current user's ID
        firestore.collection("users").document("USER_ID")
                .update("profilePicture", imageUrl);
    }

    private void updateUserProfile() {
        String name = editTextName.getText().toString().trim();
        String bio = editTextBio.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (name.isEmpty() || bio.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a map of fields to update
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("bio", bio);
        updates.put("password", password);  // Note: Do not store plain-text passwords in production!

        // Replace "USER_ID" with the current user's ID
        firestore.collection("users").document("USER_ID")
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(edit_profile.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    // Finish the activity so the user goes back to the Profile fragment
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(edit_profile.this, "Failed to update profile", Toast.LENGTH_SHORT).show());
    }
}
