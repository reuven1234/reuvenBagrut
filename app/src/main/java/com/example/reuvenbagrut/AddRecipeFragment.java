package com.example.reuvenbagrut;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import android.util.Base64;
import android.util.Log;
import com.example.reuvenbagrut.models.Recipe;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class AddRecipeFragment extends Fragment {
    private static final String TAG = "AddRecipeFragment";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MAX_IMAGE_DIMENSION = 1024;
    private static final int JPEG_QUALITY = 85;
    
    // State
    private boolean isImageChanged = false;
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    
    // Views
    private View rootView;
    private EditText recipeNameInput;
    private EditText ingredientsInput;
    private EditText instructionsInput;
    private Button submitButton;
    private Button addPhotoButton;
    private ImageView recipeImage;
    private ProgressBar progressBar;
    
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    public AddRecipeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFirebase();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
        initializeViews(view);
        setupClickListeners();
        checkAuthenticationState();
    }

    private void initializeViews(View view) {
        recipeNameInput = view.findViewById(R.id.recipeNameInput);
        ingredientsInput = view.findViewById(R.id.ingredientsInput);
        instructionsInput = view.findViewById(R.id.instructionsInput);
        submitButton = view.findViewById(R.id.submitButton);
        recipeImage = view.findViewById(R.id.recipeImage);
        addPhotoButton = view.findViewById(R.id.addPhotoButton);
        progressBar = view.findViewById(R.id.progressIndicator);
    }

    private void checkAuthenticationState() {
        if (currentUser == null && isAdded()) {
            Snackbar.make(requireView(), R.string.error_not_authenticated, Snackbar.LENGTH_LONG).show();
            navigateToLogin();
        }
    }

    private void setupClickListeners() {
        submitButton.setOnClickListener(v -> validateAndSubmitRecipe());
        addPhotoButton.setOnClickListener(v -> selectImageFromGallery());
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            try {
                processSelectedImage(data.getData());
            } catch (Exception e) {
                Log.e(TAG, "Error loading image", e);
                Snackbar.make(requireView(), R.string.error_loading_image, Snackbar.LENGTH_SHORT).show();
            }
        }
    }
    
    private void processSelectedImage(Uri imageUri) {
        try {
            selectedImageUri = imageUri;
            selectedImageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
            
            // Check file size
            if (isImageFileTooLarge(selectedImageUri)) {
                Snackbar.make(requireView(), R.string.error_loading_image, Snackbar.LENGTH_LONG).show();
                return;
            }
            
            selectedImageBitmap = compressImage(selectedImageBitmap);
            recipeImage.setImageBitmap(selectedImageBitmap);
            isImageChanged = true;
            
            // Update add photo button text
            addPhotoButton.setText(R.string.change_photo);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
            Snackbar.make(requireView(), R.string.error_loading_image, Snackbar.LENGTH_SHORT).show();
        }
    }
    
    private boolean isImageFileTooLarge(Uri imageUri) {
        try {
            return requireActivity().getContentResolver().openFileDescriptor(imageUri, "r").getStatSize() > 5 * 1024 * 1024;
        } catch (Exception e) {
            Log.e(TAG, "Error checking file size", e);
            return false;
        }
    }

    private Bitmap compressImage(Bitmap original) {
        if (original == null) return null;

        int width = original.getWidth();
        int height = original.getHeight();
        float ratio = Math.min((float) MAX_IMAGE_DIMENSION / width, (float) MAX_IMAGE_DIMENSION / height);

        if (ratio < 1) {
            width = Math.round(width * ratio);
            height = Math.round(height * ratio);
            return Bitmap.createScaledBitmap(original, width, height, true);
        }
        return original;
    }

    private void validateAndSubmitRecipe() {
        if (!isAdded() || currentUser == null) {
            Snackbar.make(requireView(), R.string.error_not_authenticated, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Get recipe name
        String recipeName = recipeNameInput.getText().toString().trim();
        if (TextUtils.isEmpty(recipeName)) {
            recipeNameInput.setError(getString(R.string.error_fill_all_fields));
            recipeNameInput.requestFocus();
            return;
        }

        // Get ingredients
        String ingredientsString = ingredientsInput.getText().toString().trim();
        if (TextUtils.isEmpty(ingredientsString)) {
            ingredientsInput.setError(getString(R.string.error_fill_all_fields));
            ingredientsInput.requestFocus();
            return;
        }

        // Split ingredients string into a list and set on recipe object
        List<String> ingredientsList = new ArrayList<>(Arrays.asList(ingredientsString.split("\n")));
        // Remove empty lines from the ingredients list
        ingredientsList.removeIf(String::isEmpty);

        // Get instructions
        String instructions = instructionsInput.getText().toString().trim();
        if (TextUtils.isEmpty(instructions)) {
            instructionsInput.setError(getString(R.string.error_fill_all_fields));
            instructionsInput.requestFocus();
            return;
        }

        showLoading(true);

        // Convert bitmap to Base64 string
        String imageBase64 = null;
        if (selectedImageBitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            imageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
            Log.d(TAG, "Generated Base64 image string: " + imageBase64.substring(0, Math.min(imageBase64.length(), 100)) + "..."); // Log first 100 chars
        }

        saveRecipe(recipeName, ingredientsString, instructions, imageBase64, ingredientsList);
    }

    private void saveRecipe(String recipeName, String ingredientsString, String instructions, String imageBase64, List<String> ingredientsList) {
        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName(); // Assuming display name is set on user

        // Get user image (Base64) from current user profile if available
        String userImageBase64 = null;
        if (currentUser.getPhotoUrl() != null) {
            userImageBase64 = currentUser.getPhotoUrl().toString();
        }

        Recipe recipe = new Recipe();
        recipe.setStrMeal(recipeName);
        recipe.setIngredients(ingredientsList);
        recipe.setIngredientsString(ingredientsString);
        recipe.setStrInstructions(instructions);
        recipe.setUserId(userId);
        recipe.setUserName(userName);
        recipe.setUserImage(userImageBase64);
        recipe.setTimestamp(System.currentTimeMillis());
        recipe.setStrMealThumb(imageBase64);

        db.collection("recipes").add(recipe)
                .addOnSuccessListener(documentReference -> {
                    // Update the recipe with its generated ID
                    String recipeId = documentReference.getId();
                    documentReference.update("id", recipeId)
                            .addOnSuccessListener(aVoid -> {
                                showLoading(false);
                                Snackbar.make(rootView, R.string.recipe_added_successfully, Snackbar.LENGTH_SHORT).show();
                                navigateBack();
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                handleError(e, R.string.error_adding_recipe);
                            });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    handleError(e, R.string.error_adding_recipe);
                });
    }

    private void handleError(Exception e, int messageResId) {
        showLoading(false);
        if (isAdded() && getContext() != null) {
            Snackbar.make(requireView(), messageResId, Snackbar.LENGTH_LONG).show();
            Log.e(TAG, "Error: ", e);
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (submitButton != null) {
            submitButton.setEnabled(!show);
        }
    }

    private void navigateBack() {
        if (isAdded() && getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack();
        }
    }

    private void navigateToLogin() {
        if (isAdded() && getActivity() != null) {
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (selectedImageBitmap != null) {
            selectedImageBitmap.recycle();
            selectedImageBitmap = null;
        }
        // Nullify views to prevent memory leaks
        recipeNameInput = null;
        ingredientsInput = null;
        instructionsInput = null;
        submitButton = null;
        addPhotoButton = null;
        recipeImage = null;
        progressBar = null;
        rootView = null;
    }
} 