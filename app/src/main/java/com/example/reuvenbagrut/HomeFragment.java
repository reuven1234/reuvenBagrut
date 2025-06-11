package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.reuvenbagrut.activities.RecipeDetailActivity;
import com.example.reuvenbagrut.adapters.RecipeAdapter;
import com.example.reuvenbagrut.api.TheMealDbApi;
import com.example.reuvenbagrut.models.Meal;
import com.example.reuvenbagrut.models.MealApiResponse;
import com.example.reuvenbagrut.models.Recipe;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private SearchView searchView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmerLayout;
    private TextView emptyStateText;
    private ProgressBar progressBar;
    private boolean isSearching = false;
    private String selectedCategory = "All";
    private TheMealDbApi theMealDbApi;
    private final List<Recipe> recipes = new ArrayList<>();
    private boolean isAdapterInitialized = false;

    /* ────────────────────────────────────────── */
    /*    Lifecycle                               */
    /* ────────────────────────────────────────── */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSearchView();
        setupSwipeRefresh();
        initRetrofit();
        loadRecipes();        // initial load
    }

    /* ────────────────────────────────────────── */
    /*    Init & setup                            */
    /* ────────────────────────────────────────── */

    private void initViews(View view) {
        recyclerView       = view.findViewById(R.id.popularRecipesRecyclerView);
        searchView         = view.findViewById(R.id.searchView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        shimmerLayout      = view.findViewById(R.id.shimmerLayout);
        emptyStateText     = view.findViewById(R.id.emptyStateText);
        progressBar        = view.findViewById(R.id.progressBar);

        db      = FirebaseFirestore.getInstance();
        mAuth   = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TheMealDbApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        theMealDbApi = retrofit.create(TheMealDbApi.class);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        if (!isAdapterInitialized) {
            recipeAdapter = new RecipeAdapter(getContext(), recipes, recipe -> {
                Intent i = new Intent(getActivity(), RecipeDetailActivity.class);
                i.putExtra("recipe", recipe);
                startActivity(i);
            });
            isAdapterInitialized = true;
        }
        recyclerView.setAdapter(recipeAdapter);
        updateEmptyState(recipes.isEmpty());
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                handleSearch(q);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String t) {
                if (t != null && t.length() >= 3) {
                    handleSearch(t);
                } else if (t.isEmpty()) {
                    isSearching = false;
                    loadRecipes();
                }
                return true;
            }

            private void handleSearch(String q) {
                isSearching = true;
                searchRecipes(q.trim());
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_color);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (isSearching) {
                searchRecipes(searchView.getQuery().toString());
            } else {
                loadRecipes();
            }
        });
    }

    /* ────────────────────────────────────────── */
    /*    Data loads & search                     */
    /* ────────────────────────────────────────── */

    private void loadRecipes() {
        showLoading(true);
        db.collection("recipes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Recipe> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Recipe r = d.toObject(Recipe.class);
                        if (r == null) continue;
                        r.setId(d.getId());
                        list.add(r);
                    }
                    recipes.clear();
                    recipes.addAll(list);
                    recipeAdapter.setRecipes(recipes);
                    updateEmptyState(list.isEmpty());
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading local recipes", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    Log.e(TAG, "loadRecipes failure", e);
                });
    }

    private void searchRecipes(String query) {
        showLoading(true);
        theMealDbApi.searchMealsByName(query).enqueue(new Callback<MealApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<MealApiResponse> call, @NonNull Response<MealApiResponse> response) {
                if (!isAdded()) { // Check if fragment is attached
                    return;
                }
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getMeals() != null) {
                    List<Meal> meals = response.body().getMeals();
                    List<Recipe> apiRecipes = new ArrayList<>();
                    for (Meal meal : meals) {
                        Recipe recipe = new Recipe();
                        recipe.setId(meal.getIdMeal());
                        recipe.setRecipeName(meal.getStrMeal());
                        recipe.setImageUrl(meal.getStrMealThumb());
                        recipe.setIngredients(meal.getIngredientMeasures());
                        recipe.setInstructions(meal.getStrInstructions());
                        recipe.setCategory(meal.getStrCategory());
                        apiRecipes.add(recipe);
                    }
                    recipes.clear();
                    recipes.addAll(apiRecipes);
                    recipeAdapter.setRecipes(recipes);
                    updateEmptyState(apiRecipes.isEmpty());
                    Log.d(TAG, "Fetched " + apiRecipes.size() + " API recipes");
                } else {
                    Toast.makeText(getContext(), "No recipes found from API or error in response", Toast.LENGTH_SHORT).show();
                    recipes.clear();
                    recipeAdapter.setRecipes(recipes);
                    updateEmptyState(true);
                    Log.e(TAG, "API response not successful or empty: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MealApiResponse> call, @NonNull Throwable t) {
                if (!isAdded()) { // Check if fragment is attached
                    return;
                }
                showLoading(false);
                Toast.makeText(getContext(), "Error searching recipes from API", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API search failure", t);
                // Fallback to local search if API fails and query is empty, or if we weren't searching previously.
                // For now, only display API results on search.
                recipes.clear();
                recipeAdapter.setRecipes(recipes);
                updateEmptyState(true);
            }
        });
    }

    /* ────────────────────────────────────────── */
    /*    UI helpers                              */
    /* ────────────────────────────────────────── */

    private void updateEmptyState(boolean empty) {
        if (empty) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText(isSearching ? R.string.no_api_recipes_found : R.string.no_local_recipes_found);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

        if (shimmerLayout != null) {
            if (show) { shimmerLayout.setVisibility(View.VISIBLE); shimmerLayout.startShimmer(); }
            else      { shimmerLayout.stopShimmer(); shimmerLayout.setVisibility(View.GONE); }
        }
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateText.setVisibility(show ? View.GONE : emptyStateText.getVisibility());
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /* ────────────────────────────────────────── */
    /*    Lifecycle cleanup                       */
    /* ────────────────────────────────────────── */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (shimmerLayout != null) shimmerLayout.stopShimmer();
        if (recyclerView != null) recyclerView.setAdapter(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        // No longer auto-load local recipes on resume if a search was active.
        // If not searching, load local recipes; otherwise, maintain search results.
        if (!isSearching) {
            loadRecipes();
        } else if (searchView != null && !searchView.getQuery().toString().isEmpty()) {
            // Re-run the API search if it was active and the query is not empty
            searchRecipes(searchView.getQuery().toString());
        }
    }
}
