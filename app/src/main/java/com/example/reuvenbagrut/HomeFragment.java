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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        setupRecyclerView();
        setupSearchView();
        setupSwipeRefresh();
        setupCategoryChips();

        if (!isSearching) {
            loadRecipes();
        }
        return view;
    }

    private void initViews(View view) {
        recyclerView       = view.findViewById(R.id.popularRecipesRecyclerView);
        searchView         = view.findViewById(R.id.searchView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        categoryChipGroup  = view.findViewById(R.id.categoryChipGroup);
        shimmerLayout      = view.findViewById(R.id.shimmerLayout);
        emptyStateText     = view.findViewById(R.id.emptyStateText);
        progressBar        = view.findViewById(R.id.progressBar);

        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recipeAdapter = new RecipeAdapter(getContext(), new ArrayList<>(), new RecipeAdapter.OnRecipeClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(getContext(), RecipeDetailActivity.class);
                intent.putExtra("recipe", recipe);
                startActivity(intent);
            }

            @Override
            public void onUserClick(String userId) {
                Intent intent = new Intent(getContext(), UserProfileActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }

            @Override
            public void onLikeClick(Recipe recipe) {
                if (currentUser != null) {
                    String uid = currentUser.getUid();
                    if (recipe.isLikedByUser(uid)) {
                        recipe.removeLike(uid);
                    } else {
                        recipe.addLike(uid);
                    }
                    recipeAdapter.notifyDataSetChanged();
                }
            }
        });
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
        db.collection("recipes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Recipe> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Recipe r = doc.toObject(Recipe.class);
                        if (r != null) {
                            r.setId(doc.getId());
                            // Get user information
                            if (r.getUserId() != null) {
                                db.collection("users")
                                    .document(r.getUserId())
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            r.setUserName(userDoc.getString("name"));
                                            r.setUserImage(userDoc.getString("profileImageUrl"));
                                            recipeAdapter.notifyDataSetChanged();
                                        }
                                    });
                            }
                            list.add(r);
                        }
                    }
                    recipeAdapter.setRecipes(list);
                    updateEmptyState(list.isEmpty());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading recipes", Toast.LENGTH_SHORT).show()
                );
    }

    private void searchRecipes(String query) {
        showLoading(true);
        RetrofitClient.getInstance()
                .getApiService()
                .searchRecipes(query)
                .enqueue(new Callback<RecipeApiResponse>() {
                    @Override
                    public void onResponse(Call<RecipeApiResponse> call,
                                           Response<RecipeApiResponse> response) {
                        showLoading(false);
                        List<Recipe> recipes = new ArrayList<>();
                        if (response.isSuccessful() && response.body() != null) {
                            List<RecipeResult> results = response.body().getMeals();
                            if (results != null) {
                                for (RecipeResult res : results) {
                                    Recipe r = new Recipe();
                                    r.setId(res.getIdMeal());
                                    r.setStrMeal(res.getStrMeal());
                                    r.setStrCategory(res.getStrCategory());
                                    r.setStrArea(res.getStrArea());
                                    r.setStrInstructions(res.getStrInstructions());
                                    r.setStrMealThumb(res.getStrMealThumb());
                                    r.setStrTags(res.getStrTags());
                                    r.setStrYoutube(res.getStrYoutube());
                                    r.setStrSource(res.getStrSource());
                                    // build ingredient list…
                                    List<String> ingr = new ArrayList<>();
                                    for (int i = 1; i <= 20; i++) {
                                        String ing = null, measure = null;
                                        switch (i) {
                                            case 1:
                                                ing = res.getStrIngredient1();
                                                measure = res.getStrMeasure1();
                                                break;
                                            case 2:
                                                ing = res.getStrIngredient2();
                                                measure = res.getStrMeasure2();
                                                break;
                                            // …cases 3–20…
                                            case 20:
                                                ing = res.getStrIngredient20();
                                                measure = res.getStrMeasure20();
                                                break;
                                        }
                                        if (ing != null && !ing.trim().isEmpty()) {
                                            String line = ing.trim();
                                            if (measure != null && !measure.trim().isEmpty()) {
                                                line += " – " + measure.trim();
                                            }
                                            ingr.add(line);
                                        }
                                    }
                                    r.setIngredients(ingr);
                                    recipes.add(r);
                                }
                            }
                        } else {
                            Snackbar.make(getView(), R.string.error_searching, Snackbar.LENGTH_LONG).show();
                        }
                        recipeAdapter.setRecipes(recipes);
                        updateEmptyState(recipes.isEmpty());
                    }
                    @Override
                    public void onFailure(Call<RecipeApiResponse> call, Throwable t) {
                        showLoading(false);
                        Snackbar.make(getView(), R.string.error_searching, Snackbar.LENGTH_LONG).show();
                        Log.e(TAG, "searchRecipes onFailure", t);
                    }
                });
    }

    private void updateEmptyState(boolean empty) {
        emptyStateText.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showLoading(boolean show) {
        swipeRefreshLayout.setRefreshing(false);
        if (show) {
            shimmerLayout.setVisibility(View.VISIBLE);
            shimmerLayout.startShimmer();
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
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
        recyclerView.setAdapter(null);
        recipeAdapter = null;
        shimmerLayout.stopShimmer();
    }
}
