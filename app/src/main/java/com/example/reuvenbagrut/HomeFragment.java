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

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final String KEY_SELECTED_CATEGORY = "selected_category";
    private static final int GRID_SPAN_COUNT = 1;

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

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        if (savedInstanceState != null) {
            selectedCategory = savedInstanceState.getString(KEY_SELECTED_CATEGORY, "All");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupRecyclerView();
        setupSearchView();
        setupSwipeRefresh();
        setupCategoryChips();
        loadRecipes();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.popularRecipesRecyclerView);
        searchView = view.findViewById(R.id.searchView);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        emptyStateText = view.findViewById(R.id.emptyStateText);
    }

    private void setupRecyclerView() {
        if (recyclerView == null) return;
        
        // Use a GridLayoutManager with proper configuration
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), GRID_SPAN_COUNT);
        recyclerView.setLayoutManager(layoutManager);
        
        // Important: disable nested scrolling to prevent conflict with NestedScrollView
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false); // Allow dynamic sizing
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER); // Disable overscroll effect in the RecyclerView
        
        // Setup adapter
        recipeAdapter = new RecipeAdapter();
        recipeAdapter.setOnRecipeClickListener((recipe, position) -> {
            if (recipe != null && isAdded()) {
                navigateToRecipeDetail(recipe);
            }
        });
        recyclerView.setAdapter(recipeAdapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (recipeAdapter != null) {
                    recipeAdapter.getFilter().filter(newText);
                    return true;
                }
                return false;
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_color);
        swipeRefreshLayout.setOnRefreshListener(this::loadRecipes);
    }

    private void setupCategoryChips() {
        categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Get selected category and reload recipes
            selectedCategory = getSelectedCategory(checkedId);
            loadRecipes();
        });

        // Set initial selection
        categoryChipGroup.check(getCategoryChipId(selectedCategory));
    }

    private void loadRecipes() {
        showLoading(true);
        
        List<Recipe> recipes = new ArrayList<>();
        
        db.collection("recipes")
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful() && isAdded()) {
                  for (QueryDocumentSnapshot document : task.getResult()) {
                      try {
                          Recipe recipe = new Recipe();
                          recipe.setIdMeal(document.getId());
                          recipe.setStrMeal(document.getString("strMeal"));
                          recipe.setStrCategory(document.getString("strCategory"));
                          recipe.setStrInstructions(document.getString("strInstructions"));
                          recipe.setStrMealThumb(document.getString("strMealThumb"));
                          recipe.setStrAuthor(document.getString("strAuthor"));
                          recipe.setStrAuthorImage(document.getString("strAuthorImage"));
                          recipe.setUserId(document.getString("userId"));
                          
                          // Only add recipes that match the selected category or if "All" is selected
                          if (selectedCategory.equals("All") || 
                              (recipe.getStrCategory() != null && recipe.getStrCategory().equals(selectedCategory))) {
                              recipes.add(recipe);
                          }
                      } catch (Exception e) {
                          Log.e(TAG, "Error parsing recipe", e);
                      }
                  }
                  
                  updateRecipeList(recipes);
                  showLoading(false);
              } else {
                  if (isAdded()) {
                      showError(getString(R.string.error_loading_recipes));
                      showLoading(false);
                  }
              }
          })
          .addOnFailureListener(e -> {
              if (isAdded()) {
                  Log.e(TAG, "Error loading recipes", e);
                  showError(getString(R.string.error_loading_recipes));
                  showLoading(false);
              }
          });
    }

    private void updateRecipeList(List<Recipe> recipes) {
        if (recipeAdapter != null) {
            recipeAdapter.setRecipes(recipes);
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
                .setAction(R.string.retry, v -> loadRecipes())
                .show();
        }
    }

    private void navigateToRecipeDetail(Recipe recipe) {
        if (getActivity() != null && recipe != null) {
            Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getIdMeal());
            startActivity(intent);
        }
    }

    private String getSelectedCategory(int checkedId) {
        if (checkedId == R.id.chipAll) return "All";
        if (checkedId == R.id.chipBreakfast) return "Breakfast";
        if (checkedId == R.id.chipLunch) return "Lunch";
        if (checkedId == R.id.chipDinner) return "Dinner";
        if (checkedId == R.id.chipDessert) return "Dessert";
        return "All";
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
        // Clean up any resources
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        recipeAdapter = null;
        if (shimmerLayout != null) {
            shimmerLayout.stopShimmer();
        }
    }
}