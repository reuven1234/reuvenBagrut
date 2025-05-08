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

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final String KEY_SELECTED_CATEGORY = "selected_category";
    private static final int GRID_SPAN_COUNT = 2;

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
    private List<Recipe> recipes = new ArrayList<>();

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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize views
        initializeViews(view);
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup search view
        setupSearchView();
        
        // Setup swipe refresh
        setupSwipeRefresh();
        
        // Setup category chips
        setupCategoryChips();
        
        // Load recipes
        loadRecipes();
        
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
        recipeAdapter = new RecipeAdapter(recipes, recipe -> {
            // Handle recipe click
            navigateToRecipeDetail(recipe);
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
            if (checkedId == View.NO_ID) return;
            
            // Get the selected chip
            com.google.android.material.chip.Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                selectedCategory = chip.getText().toString();
                loadRecipes();
            }
        });

        // Set initial selection
        categoryChipGroup.check(getCategoryChipId(selectedCategory));
    }

    private void loadRecipes() {
        showLoading(true);
        Log.d(TAG, "Starting to load recipes...");
        
        db.collection("recipes")
          .orderBy("timestamp", Query.Direction.DESCENDING)
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful() && isAdded()) {
                  List<Recipe> recipes = new ArrayList<>();
                  Log.d(TAG, "Query successful. Document count: " + task.getResult().size());
                  
                  for (QueryDocumentSnapshot document : task.getResult()) {
                      try {
                          Log.d(TAG, "Processing document: " + document.getId());
                          Recipe recipe = document.toObject(Recipe.class);
                          recipe.setId(document.getId());
                          Log.d(TAG, "Recipe data - Title: " + recipe.getStrMeal() + 
                                    ", Category: " + recipe.getStrCategory());
                          
                          // Only add recipes that match the selected category or if "All" is selected
                          if (selectedCategory.equals("All") || 
                              (recipe.getStrCategory() != null && recipe.getStrCategory().equals(selectedCategory))) {
                              recipes.add(recipe);
                              Log.d(TAG, "Added recipe to list");
                          } else {
                              Log.d(TAG, "Recipe skipped - category mismatch. Selected: " + selectedCategory);
                          }
                      } catch (Exception e) {
                          Log.e(TAG, "Error parsing recipe: " + e.getMessage(), e);
                      }
                  }
                  
                  Log.d(TAG, "Final recipes list size: " + recipes.size());
                  updateRecipeList(recipes);
                  showLoading(false);
              } else if (isAdded()) {
                  Log.e(TAG, "Error loading recipes: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                  showError(getString(R.string.error_loading_recipes));
                  showLoading(false);
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
            Log.d(TAG, "Updating adapter with " + recipes.size() + " recipes");
            recipeAdapter.updateRecipes(recipes);
            updateEmptyState(recipes.isEmpty());
        } else {
            Log.e(TAG, "RecipeAdapter is null!");
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
            intent.putExtra("recipe_id", recipe.getId());
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