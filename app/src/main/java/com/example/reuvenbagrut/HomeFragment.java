package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import com.example.reuvenbagrut.api.RetrofitClient;
import com.example.reuvenbagrut.models.RecipeApiResponse;
import com.example.reuvenbagrut.models.RecipeApiResponse.RecipeResult;

import com.example.reuvenbagrut.activities.RecipeDetailActivity;
import com.example.reuvenbagrut.activities.UserProfileActivity;

import com.example.reuvenbagrut.adapters.RecipeAdapter;
import com.example.reuvenbagrut.models.Recipe;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private SearchView searchView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChipGroup categoryChipGroup;
    private ShimmerFrameLayout shimmerLayout;
    private TextView emptyStateText;
    private ProgressBar progressBar;
    private boolean isSearching = false;
    private String selectedCategory = "All";
    private List<Recipe> recipes = new ArrayList<>();
    private boolean isAdapterInitialized = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");

        // Initialize views first
        initViews(view);
        Log.d(TAG, "Views initialized");

        // Setup RecyclerView
        setupRecyclerView();

        // Setup other components
        setupSearchView();
        setupSwipeRefresh();
        setupCategoryChips();
        Log.d(TAG, "All components setup completed");

        // Load data after all components are set up
        loadRecipes();
        Log.d(TAG, "Initial recipes loading started");
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.popularRecipesRecyclerView);
        searchView = view.findViewById(R.id.searchView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        progressBar = view.findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView is null");
            return;
        }

        // Set layout manager first
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        
        // Create adapter if not exists
        if (!isAdapterInitialized) {
            recipeAdapter = new RecipeAdapter(getContext(), recipes, recipe -> {
                Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
                intent.putExtra("recipe", recipe);
                startActivity(intent);
            });
            isAdapterInitialized = true;
        }

        // Set adapter
        recyclerView.setAdapter(recipeAdapter);
        Log.d(TAG, "RecyclerView adapter set successfully");

        // Ensure adapter is properly attached
        if (recipeAdapter != null && recipeAdapter.getItemCount() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "RecyclerView setup completed");
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
                String q = searchView.getQuery().toString();
                if (!q.isEmpty()) searchRecipes(q);
            } else {
                loadRecipes();
            }
        });
    }

    private void setupCategoryChips() {
        categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) return;
            selectedCategory = ((com.google.android.material.chip.Chip)group.findViewById(checkedId))
                    .getText().toString();
            if (!isSearching) loadRecipes();
        });
        categoryChipGroup.check(getCategoryChipId(selectedCategory));
    }

    private void loadRecipes() {
        if (recipeAdapter == null || getContext() == null || recyclerView == null) {
            Log.e(TAG, "Required components are null");
            return;
        }

        showLoading(true);

        db.collection("recipes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    if (recipeAdapter == null || getContext() == null || recyclerView == null) {
                        Log.e(TAG, "Required components are null in callback");
                        return;
                    }

                    List<Recipe> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Recipe r = doc.toObject(Recipe.class);
                        if (r != null) {
                            r.setId(doc.getId());
                            if (r.getUserId() != null) {
                                db.collection("users")
                                        .document(r.getUserId())
                                        .get()
                                        .addOnSuccessListener(userDoc -> {
                                            if (userDoc.exists() && recipeAdapter != null) {
                                                r.setUserName(userDoc.getString("name"));
                                                r.setUserImage(userDoc.getString("profileImageUrl"));
                                                recipeAdapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                            list.add(r);
                        }
                    }
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            recipes.clear();
                            recipes.addAll(list);
                            recipeAdapter.setRecipes(recipes);
                            updateEmptyState(list.isEmpty());
                            showLoading(false);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error loading recipes", Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    }
                });
    }

    private void searchRecipes(String query) {
        if (recipeAdapter == null || getContext() == null) {
            return;
        }

        showLoading(true);
        RetrofitClient.getInstance()
                .getApiService()
                .searchRecipes(query)
                .enqueue(new Callback<RecipeApiResponse>() {
                    @Override
                    public void onResponse(Call<RecipeApiResponse> call,
                                           Response<RecipeApiResponse> response) {
                        if (recipeAdapter == null || getContext() == null) {
                            return;
                        }

                        showLoading(false);
                        List<Recipe> recipes = new ArrayList<>();
                        // ... rest of your existing code ...

                        recipeAdapter.setRecipes(recipes);
                        updateEmptyState(recipes.isEmpty());
                    }

                    @Override
                    public void onFailure(Call<RecipeApiResponse> call, Throwable t) {
                        if (recipeAdapter == null || getContext() == null) {
                            return;
                        }

                        showLoading(false);
                        if (getView() != null) {
                            Snackbar.make(getView(), R.string.error_searching, Snackbar.LENGTH_LONG).show();
                        }
                        Log.e(TAG, "searchRecipes onFailure", t);
                    }
                });
    }

    private void updateEmptyState(boolean empty) {
        if (emptyStateText != null && recyclerView != null) {
            if (empty) {
                recyclerView.setVisibility(View.GONE);
                emptyStateText.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateText.setVisibility(View.GONE);
            }
        }
    }

    private void showLoading(boolean show) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        
        if (shimmerLayout != null) {
            if (show) {
                shimmerLayout.setVisibility(View.VISIBLE);
                shimmerLayout.startShimmer();
            } else {
                shimmerLayout.stopShimmer();
                shimmerLayout.setVisibility(View.GONE);
            }
        }
        
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        
        if (emptyStateText != null) {
            emptyStateText.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private int getCategoryChipId(String category) {
        switch (category) {
            case "Breakfast": return R.id.chipBreakfast;
            case "Lunch":     return R.id.chipLunch;
            case "Dinner":    return R.id.chipDinner;
            case "Dessert":   return R.id.chipDessert;
            default:          return R.id.chipAll;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("selected_category", selectedCategory);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (shimmerLayout != null) {
            shimmerLayout.stopShimmer();
        }
        // Don't clear the adapter reference, just detach it
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        // Clear view references
        recyclerView = null;
        shimmerLayout = null;
        searchView = null;
        swipeRefreshLayout = null;
        categoryChipGroup = null;
        emptyStateText = null;
        progressBar = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isSearching && recipeAdapter != null) {
            loadRecipes();
        }
    }
}
