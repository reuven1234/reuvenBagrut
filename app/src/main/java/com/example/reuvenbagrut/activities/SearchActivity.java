package com.example.reuvenbagrut.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.RecipeAdapter;
import com.example.reuvenbagrut.Recipe;
import com.example.reuvenbagrut.RecipeDetailActivity;
import com.example.reuvenbagrut.api.RetrofitClient;
import com.example.reuvenbagrut.models.RecipeApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private static final String API_KEY = "07194345ea6d4e2eaf7f93b9d974d285"; // Replace this with your actual Spoonacular API key

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private SearchView searchView;
    private ShimmerFrameLayout shimmerLayout;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setupToolbar();
        initializeViews();
        setupRecyclerView();
        setupSearchView();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.search_recipes);
        }
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.searchResultsRecyclerView);
        searchView = findViewById(R.id.searchView);
        shimmerLayout = findViewById(R.id.shimmerLayout);
        emptyStateText = findViewById(R.id.emptyStateText);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeAdapter = new RecipeAdapter(this);
        recyclerView.setAdapter(recipeAdapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    searchRecipes(query.trim());
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && newText.length() >= 3) {
                    searchRecipes(newText.trim());
                }
                return true;
            }
        });
    }

    private void searchRecipes(String query) {
        showLoading(true);

        RetrofitClient.getInstance()
                .getApiService()
                .searchRecipes(query)
                .enqueue(new Callback<RecipeApiResponse>() {
                    @Override
                    public void onResponse(Call<RecipeApiResponse> call, Response<RecipeApiResponse> response) {
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            recipeAdapter.setRecipes(response.body().getMeals());
                            if (response.body().getMeals() == null || response.body().getMeals().isEmpty()) {
                                Toast.makeText(SearchActivity.this, "No recipes found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SearchActivity.this, "Error searching recipes", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeApiResponse> call, Throwable t) {
                        showLoading(false);
                        showError(getString(R.string.error_searching));
                    }
                });
    }

    private void showLoading(boolean show) {
        if (show) {
            if (shimmerLayout != null) {
                shimmerLayout.setVisibility(View.VISIBLE);
                shimmerLayout.startShimmer();
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
            }
            if (emptyStateText != null) {
                emptyStateText.setVisibility(View.GONE);
            }
        } else {
            if (shimmerLayout != null) {
                shimmerLayout.stopShimmer();
                shimmerLayout.setVisibility(View.GONE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        recipeAdapter = null;
        if (shimmerLayout != null) {
            shimmerLayout.stopShimmer();
        }
    }
} 