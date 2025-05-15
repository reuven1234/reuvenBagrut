package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.Query;
import com.example.reuvenbagrut.adapters.RecipeAdapter;
import com.example.reuvenbagrut.Recipe;
import com.example.reuvenbagrut.api.RetrofitClient;
import com.example.reuvenbagrut.models.RecipeApiResponse;
import com.example.reuvenbagrut.models.RecipeApiResponse.RecipeResult;
import com.example.reuvenbagrut.activities.RecipeDetailActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.Toast;

public class HomeFragment extends Fragment implements RecipeAdapter.OnRecipeClickListener {
    private static final String TAG = "HomeFragment";
    private static final String KEY_SELECTED_CATEGORY = "selected_category";
    private static final int GRID_SPAN_COUNT = 2;
    private static final String API_KEY = "07194345ea6d4e2eaf7f93b9d974d285"; // Replace this with your actual Spoonacular API key

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private SearchView searchView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private View emptyStateView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChipGroup categoryChipGroup;
    private ShimmerFrameLayout shimmerLayout;
    private TextView emptyStateText;
    
    private FirebaseUser currentUser;
    private String selectedCategory = "All";
    private boolean isSearching = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupSearchView();
        setupSwipeRefresh();
        setupCategoryChips();
        
        if (!isSearching) {
            loadRecipes();
        }
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.popularRecipesRecyclerView);
        searchView = view.findViewById(R.id.searchView);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), GRID_SPAN_COUNT);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        
        recipeAdapter = new RecipeAdapter();
        recipeAdapter.setOnRecipeClickListener(this);
        recyclerView.setAdapter(recipeAdapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    isSearching = true;
                    searchRecipes(query.trim());
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && newText.length() >= 3) {
                    isSearching = true;
                    searchRecipes(newText.trim());
                } else if (newText == null || newText.isEmpty()) {
                    isSearching = false;
                    loadRecipes();
                }
                return true;
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_color);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (isSearching) {
                String currentQuery = searchView.getQuery().toString();
                if (!currentQuery.isEmpty()) {
                    searchRecipes(currentQuery);
                }
            } else {
                loadRecipes();
            }
        });
    }

    private void setupCategoryChips() {
        categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) return;
            
            com.google.android.material.chip.Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                selectedCategory = chip.getText().toString();
                if (!isSearching) {
                    loadRecipes();
                }
            }
        });

        categoryChipGroup.check(getCategoryChipId(selectedCategory));
    }

    private void loadRecipes() {
        showLoading(true);
        
        db.collection("recipes")
          .orderBy("timestamp", Query.Direction.DESCENDING)
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful() && isAdded()) {
                  List<Recipe> recipes = new ArrayList<>();
                  
                  for (QueryDocumentSnapshot document : task.getResult()) {
                      try {
                          Recipe recipe = new Recipe();
                          recipe.setId(document.getId());
                          recipe.setStrMeal(document.getString("strMeal"));
                          recipe.setStrCategory(document.getString("strCategory"));
                          recipe.setStrInstructions(document.getString("strInstructions"));
                          recipe.setStrMealThumb(document.getString("strMealThumb"));
                          recipe.setStrAuthor(document.getString("strAuthor"));
                          recipe.setStrAuthorImage(document.getString("strAuthorImage"));
                          recipe.setUserId(document.getString("userId"));
                          recipe.setTimestamp(document.getLong("timestamp"));
                          
                          // Handle ingredients list
                          List<String> ingredients = (List<String>) document.get("ingredients");
                          if (ingredients != null) {
                              recipe.setIngredients(ingredients);
                          }
                          
                          // Handle steps list
                          List<String> steps = (List<String>) document.get("steps");
                          if (steps != null) {
                              recipe.setSteps(steps);
                          }
                          
                          if (selectedCategory.equals("All") || 
                              (recipe.getStrCategory() != null && recipe.getStrCategory().equals(selectedCategory))) {
                              recipes.add(recipe);
                          }
                      } catch (Exception e) {
                          Log.e(TAG, "Error parsing recipe: " + e.getMessage(), e);
                      }
                  }
                  
                  updateRecipeList(recipes);
                  showLoading(false);
              } else if (isAdded()) {
                  showError(getString(R.string.error_loading_recipes));
                  showLoading(false);
              }
          })
          .addOnFailureListener(e -> {
              if (isAdded()) {
                  showError(getString(R.string.error_loading_recipes));
                  showLoading(false);
              }
          });
    }

    private void searchRecipes(String query) {
        showLoading(true);
        
        RetrofitClient.getInstance()
                .getApiService()
                .searchRecipes(API_KEY, query, 20, true)
                .enqueue(new Callback<RecipeApiResponse>() {
                    @Override
                    public void onResponse(Call<RecipeApiResponse> call, Response<RecipeApiResponse> response) {
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            recipeAdapter.setRecipes(response.body().getResults());
                            updateEmptyState(response.body().getResults().isEmpty());
                        } else {
                            showError(getString(R.string.error_searching));
                        }
                    }

                    @Override
                    public void onFailure(Call<RecipeApiResponse> call, Throwable t) {
                        showLoading(false);
                        showError(getString(R.string.error_searching));
                    }
                });
    }

    private void updateRecipeList(List<Recipe> recipes) {
        if (recipeAdapter != null) {
            recipeAdapter.updateRecipes(recipes);
            updateEmptyState(recipes.isEmpty());
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (emptyStateText != null) {
            emptyStateText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        
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
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> {
                    if (isSearching) {
                        String currentQuery = searchView.getQuery().toString();
                        if (!currentQuery.isEmpty()) {
                            searchRecipes(currentQuery);
                        }
                    } else {
                        loadRecipes();
                    }
                })
                .show();
        }
    }

    @Override
    public void onRecipeClick(Object recipe) {
        if (recipe instanceof Recipe) {
            navigateToRecipeDetail((Recipe) recipe);
        } else if (recipe instanceof RecipeApiResponse.RecipeResult) {
            navigateToRecipeDetail((RecipeApiResponse.RecipeResult) recipe);
        }
    }

    private void navigateToRecipeDetail(Recipe recipe) {
        if (recipe != null) {
            Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            startActivity(intent);
        }
    }

    private void navigateToRecipeDetail(RecipeApiResponse.RecipeResult recipe) {
        if (recipe != null) {
            Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
            intent.putExtra("recipe_id", String.valueOf(recipe.getId()));
            intent.putExtra("is_api_recipe", true);
            startActivity(intent);
        }
    }

    private int getCategoryChipId(String category) {
        switch (category) {
            case "Breakfast": return R.id.chipBreakfast;
            case "Lunch": return R.id.chipLunch;
            case "Dinner": return R.id.chipDinner;
            case "Dessert": return R.id.chipDessert;
            default: return R.id.chipAll;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SELECTED_CATEGORY, selectedCategory);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        recipeAdapter = null;
        if (shimmerLayout != null) {
            shimmerLayout.stopShimmer();
        }
    }
}