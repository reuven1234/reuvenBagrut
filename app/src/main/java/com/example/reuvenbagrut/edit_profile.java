package com.example.reuvenbagrut;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reuvenbagrut.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class edit_profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private ImageView profileImage;
    private Uri imageUri;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileImage = findViewById(R.id.profileImage);
        Button uploadGalleryButton = findViewById(R.id.uploadGalleryButton);
        Button takePictureButton = findViewById(R.id.takePictureButton);

        // Firebase setup
        storageReference = FirebaseStorage.getInstance().getReference("profile_pictures");
        firestore = FirebaseFirestore.getInstance();

        uploadGalleryButton.setOnClickListener(v -> openGallery());
        takePictureButton.setOnClickListener(v -> openCamera());
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

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                imageUri = data.getData();
                profileImage.setImageURI(imageUri);
                uploadImageToFirebase();
            } else if (requestCode == CAMERA_REQUEST && data != null && data.getExtras() != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                profileImage.setImageBitmap(photo);
                imageUri = getImageUri(photo); // Convert bitmap to URI
                uploadImageToFirebase();
            }
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        // You need to save the image and get a URI, but this is a placeholder
        return null;
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveImageUrlToFirestore(imageUrl);
                    }))
                    .addOnFailureListener(e -> {
                        // Handle failure
                    });
        }
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        firestore.collection("users").document("USER_ID")
                .update("profilePicture", imageUrl);
    }
}
