package com.example.reuvenbagrut.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.models.Recipe;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class EditRecipeActivity extends AppCompatActivity {
    private TextInputEditText titleInput;
    private TextInputEditText descriptionInput;
    private TextInputEditText ingredientsInput;
    private TextInputEditText instructionsInput;
    private TextInputEditText cookingTimeInput;
    private AutoCompleteTextView difficultyInput;
    private MaterialButton saveButton;
    private CircularProgressIndicator progressIndicator;
    private FirebaseFirestore db;
    private String recipeId;
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Get recipe ID from intent
        recipeId = getIntent().getStringExtra("recipe_id");
        if (recipeId == null) {
            finish();
            return;
        }

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.edit_recipe);

        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        ingredientsInput = findViewById(R.id.ingredientsInput);
        instructionsInput = findViewById(R.id.instructionsInput);
        cookingTimeInput = findViewById(R.id.cookingTimeInput);
        difficultyInput = findViewById(R.id.difficultyInput);
        saveButton = findViewById(R.id.saveButton);
        progressIndicator = findViewById(R.id.progressIndicator);

        // Setup difficulty dropdown
        String[] difficultyLevels = getResources().getStringArray(R.array.difficulty_levels);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_dropdown, difficultyLevels);
        difficultyInput.setAdapter(adapter);

        // Load recipe data
        loadRecipeData();

        // Setup save button
        saveButton.setOnClickListener(v -> saveRecipe());
    }

    private void loadRecipeData() {
        showProgress(true);
        db.collection("recipes").document(recipeId)
                .get()
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        recipe = task.getResult().toObject(Recipe.class);
                        if (recipe != null) {
                            updateUI();
                        }
                    } else {
                        Toast.makeText(this, R.string.error_loading_recipe, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void updateUI() {
        titleInput.setText(recipe.getTitle());
        descriptionInput.setText(recipe.getDescription());
        
        // Format ingredients list
        StringBuilder ingredientsBuilder = new StringBuilder();
        for (String ingredient : recipe.getIngredients()) {
            ingredientsBuilder.append(ingredient).append("\n");
        }
        ingredientsInput.setText(ingredientsBuilder.toString());
        
        instructionsInput.setText(recipe.getInstructions());
        cookingTimeInput.setText(String.valueOf(recipe.getCookingTime()));
        difficultyInput.setText(recipe.getDifficultyLevel(), false);
    }

    private void saveRecipe() {
        // Validate inputs
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String ingredientsText = ingredientsInput.getText().toString().trim();
        String instructions = instructionsInput.getText().toString().trim();
        String cookingTimeStr = cookingTimeInput.getText().toString().trim();
        String difficulty = difficultyInput.getText().toString().trim();

        if (title.isEmpty()) {
            titleInput.setError(getString(R.string.error_required));
            return;
        }
        if (description.isEmpty()) {
            descriptionInput.setError(getString(R.string.error_required));
            return;
        }
        if (ingredientsText.isEmpty()) {
            ingredientsInput.setError(getString(R.string.error_required));
            return;
        }
        if (instructions.isEmpty()) {
            instructionsInput.setError(getString(R.string.error_required));
            return;
        }
        if (cookingTimeStr.isEmpty()) {
            cookingTimeInput.setError(getString(R.string.error_required));
            return;
        }
        if (difficulty.isEmpty()) {
            difficultyInput.setError(getString(R.string.error_required));
            return;
        }

        // Parse ingredients list
        List<String> ingredients = Arrays.asList(ingredientsText.split("\n"));

        // Parse cooking time
        int cookingTime;
        try {
            cookingTime = Integer.parseInt(cookingTimeStr);
        } catch (NumberFormatException e) {
            cookingTimeInput.setError(getString(R.string.error_invalid_number));
            return;
        }

        // Update recipe
        recipe.setTitle(title);
        recipe.setDescription(description);
        recipe.setIngredients(ingredients);
        recipe.setInstructions(instructions);
        recipe.setCookingTime(String.valueOf(cookingTime));
        recipe.setDifficultyLevel(difficulty);

        showProgress(true);
        db.collection("recipes").document(recipeId)
                .set(recipe)
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.recipe_updated, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, R.string.error_updating_recipe, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showProgress(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!show);
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