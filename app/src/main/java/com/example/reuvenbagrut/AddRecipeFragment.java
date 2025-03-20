package com.example.reuvenbagrut;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import android.util.Log;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.UploadTask;

public class AddRecipeFragment extends Fragment {
    private static final String TAG = "AddRecipeFragment";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MAX_IMAGE_DIMENSION = 800;
    private static final int JPEG_QUALITY = 50;
    
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
    private StorageReference storageRef;

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
        storageRef = FirebaseStorage.getInstance().getReference();
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
        String ingredientsText = ingredientsInput.getText().toString().trim();
        if (TextUtils.isEmpty(ingredientsText)) {
            ingredientsInput.setError(getString(R.string.error_fill_all_fields));
            ingredientsInput.requestFocus();
            return;
        }
        
        // Parse ingredients to a list
        List<String> ingredients = new ArrayList<>();
        for (String line : ingredientsText.split("\n")) {
            if (!TextUtils.isEmpty(line.trim())) {
                ingredients.add(line.trim());
            }
        }
        
        // Get instructions
        String instructionsText = instructionsInput.getText().toString().trim();
        if (TextUtils.isEmpty(instructionsText)) {
            instructionsInput.setError(getString(R.string.error_fill_all_fields));
            instructionsInput.requestFocus();
            return;
        }
        
        // Parse instructions to a list
        List<String> steps = new ArrayList<>();
        for (String step : instructionsText.split("(\\n+|\\. )")) {
            if (!TextUtils.isEmpty(step.trim())) {
                steps.add(step.trim());
            }
        }

        showLoading(true);
        
        // Create recipe object
        Recipe recipe = new Recipe();
        recipe.setStrMeal(recipeName);
        recipe.setStrCategory("Other"); // Default category
        recipe.setStrInstructions(instructionsText);
        recipe.setUserId(currentUser.getUid());
        recipe.setIngredients(ingredients);
        recipe.setSteps(steps);
        recipe.setTimestamp(System.currentTimeMillis());
        
        // Set author info
        recipe.setStrAuthor(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous");
        if (currentUser.getPhotoUrl() != null) {
            recipe.setStrAuthorImage(currentUser.getPhotoUrl().toString());
        }

        // Convert image to Base64 if present
        if (isImageChanged && selectedImageBitmap != null) {
            try {
                String base64Image = convertBitmapToBase64(selectedImageBitmap);
                recipe.setStrMealThumb(base64Image);
                saveRecipe(recipe);
            } catch (Exception e) {
                Log.e(TAG, "Error converting image", e);
                handleError(e, R.string.error_uploading_image);
            }
        } else {
            saveRecipe(recipe);
        }
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        try {
            // Compress bitmap to reduce size
            Bitmap compressedBitmap = compressImage(bitmap);
            
            // Convert to bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);
            byte[] imageBytes = baos.toByteArray();
            
            // Convert to Base64
            String base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
            
            // Log the size for debugging
            Log.d(TAG, "Base64 image size: " + base64Image.length() + " bytes");
            
            return "data:image/jpeg;base64," + base64Image;
        } catch (Exception e) {
            Log.e(TAG, "Error converting image to base64", e);
            throw e;
        }
    }

    private void saveRecipe(Recipe recipe) {
        if (!isAdded()) return;

        Map<String, Object> recipeMap = new HashMap<>();
        recipeMap.put("strMeal", recipe.getStrMeal());
        recipeMap.put("strCategory", recipe.getStrCategory());
        recipeMap.put("strInstructions", recipe.getStrInstructions());
        recipeMap.put("strMealThumb", recipe.getStrMealThumb());
        recipeMap.put("strAuthor", recipe.getStrAuthor());
        recipeMap.put("strAuthorImage", recipe.getStrAuthorImage());
        recipeMap.put("userId", recipe.getUserId());
        recipeMap.put("timestamp", recipe.getTimestamp());
        
        // Add lists
        recipeMap.put("ingredients", recipe.getIngredients());
        recipeMap.put("steps", recipe.getSteps());

        db.collection("recipes")
                .add(recipeMap)
                .addOnSuccessListener(documentReference -> {
                    Snackbar.make(requireView(), R.string.recipe_added_success, Snackbar.LENGTH_SHORT).show();
                    navigateBack();
                })
                .addOnFailureListener(e -> handleError(e, R.string.error_adding_recipe))
                .addOnCompleteListener(task -> showLoading(false));
    }

    private void handleError(Exception e, int messageResId) {
        Log.e(TAG, "Error: ", e);
        if (isAdded()) {
            showLoading(false);
            submitButton.setEnabled(true);
            Snackbar.make(requireView(), messageResId, Snackbar.LENGTH_SHORT).show();
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
    }
} 