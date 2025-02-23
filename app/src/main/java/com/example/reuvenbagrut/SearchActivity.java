package com.example.reuvenbagrut;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recipeAdapter = new RecipeAdapter();
        recyclerView.setAdapter(recipeAdapter);

        // Set up a TextWatcher to detect changes in the EditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // No need to implement this method
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // When the user types, we trigger the search
                String query = charSequence.toString();
                if (!query.isEmpty()) {
                    fetchRecipeData(query);
                } else {
                    // If the query is empty, clear the RecyclerView
                    recipeAdapter.setRecipeList(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No need to implement this method
            }
        });
    }

    private void fetchRecipeData(String recipeName) {
        // Call the API using Retrofit
        RecipeApiService apiService = RecipeApiClient.getRecipeApiService();
        Call<RecipeApiResponse> call = apiService.searchRecipe(recipeName);

        call.enqueue(new Callback<RecipeApiResponse>() {
            @Override
            public void onResponse(Call<RecipeApiResponse> call, Response<RecipeApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recipeAdapter.setRecipeList(response.body().getMeals());
                } else {
                    Toast.makeText(SearchActivity.this, "No recipes found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RecipeApiResponse> call, Throwable t) {
                Log.d("SEARCH_ACTIVITY", t.getMessage());
                Toast.makeText(SearchActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
