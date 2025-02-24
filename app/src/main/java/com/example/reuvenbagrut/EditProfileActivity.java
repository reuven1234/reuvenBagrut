package com.example.reuvenbagrut;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    // Constants for image selection
    private static final int GALLERY = 1;
    private static final int CAMERA = 2;

    // Views
    private EditText nameEditText, bioEditText, newPasswordEditText, currentPasswordEditText;
    private ShapeableImageView profileImageView;
    private Button changePhotoBtn, saveProfileBtn;

    // Bitmap to store selected image
    private Bitmap selectedBitmap;

    // Firebase objects
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize views – ensure your layout contains these EditTexts:
        // - editName, editBio, editPassword (for new password) and editCurrentPassword (for current password)
        profileImageView = findViewById(R.id.editProfileImage);
        nameEditText = findViewById(R.id.editName);
        bioEditText = findViewById(R.id.editBio);
        newPasswordEditText = findViewById(R.id.editPassword);
        currentPasswordEditText = findViewById(R.id.editCurrentPassword);
        changePhotoBtn = findViewById(R.id.changePhotoBtn);
        saveProfileBtn = findViewById(R.id.saveProfileBtn);

        // Initialize Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // Load current user data
        loadUserData();

        // Set up button listeners
        changePhotoBtn.setOnClickListener(v -> showPictureDialog());
        saveProfileBtn.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String bio = documentSnapshot.getString("bio");
                            String imageBase64 = documentSnapshot.getString("profilePicture");

                            if (name != null) {
                                nameEditText.setText(name);
                            }
                            if (bio != null) {
                                bioEditText.setText(bio);
                            }
                            if (imageBase64 != null && !imageBase64.isEmpty()) {
                                try {
                                    byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                    profileImageView.setImageBitmap(decodedBitmap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }
    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Please choose an option:");
        String[] pictureDialogItems = {"From Gallery", "From Camera"};
        pictureDialog.setItems(pictureDialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    choosePhotoFromGallery();
                } else if (which == 1) {
                    takePhotoFromCamera();
                }
            }
        });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    public void takePhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            return;
        }
        if(requestCode == GALLERY) {
            if(data != null && data.getData() != null) {
                try {
                    selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    profileImageView.setImageBitmap(selectedBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image!", Toast.LENGTH_SHORT).show();
                }
            }
        } else if(requestCode == CAMERA) {
            if(data != null && data.getExtras() != null) {
                // This returns a thumbnail image
                selectedBitmap = (Bitmap) data.getExtras().get("data");
                profileImageView.setImageBitmap(selectedBitmap);
            }
        }
    }

    private void saveProfile() {
        String newName = nameEditText.getText().toString().trim();
        String newBio = bioEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String currentPassword = currentPasswordEditText.getText().toString().trim();

        if(newName.isEmpty()){
            nameEditText.setError("Name required");
            nameEditText.requestFocus();
            return;
        }

        // Convert the selected image to Base64 if available
        String base64Image;
        if(selectedBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } else {
            base64Image = null;
        }

        // If a new password is provided, perform reauthentication first
        if(!newPassword.isEmpty()){
            if(currentPassword.isEmpty()){
                currentPasswordEditText.setError("Current password required");
                currentPasswordEditText.requestFocus();
                return;
            }
            String email = user.getEmail();
            AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    // Update password after successful reauthentication
                    user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            // After password update, update the Firestore data
                            updateFirestoreData(newName, newBio, base64Image);
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Password update failed: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(EditProfileActivity.this, "Reauthentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // No password update required, just update Firestore
            updateFirestoreData(newName, newBio, base64Image);
        }
    }

    private void updateFirestoreData(String name, String bio, String imageBase64) {
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("name", name);
        updates.put("bio", bio);
        if(imageBase64 != null){
            updates.put("profilePicture", imageBase64);
        }
        db.collection("users").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
