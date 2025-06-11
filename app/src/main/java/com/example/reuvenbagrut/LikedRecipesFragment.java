package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.reuvenbagrut.activities.RecipeDetailActivity;

import java.util.ArrayList;
import java.util.List;

import com.example.reuvenbagrut.adapters.RecipeAdapter;
import com.example.reuvenbagrut.models.Recipe;

public class LikedRecipesFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmerLayout;
    private TextView emptyStateText;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_liked_recipes, container, false
        );

        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadLikedRecipes();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.likedRecipesRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        progressBar = view.findViewById(R.id.progressBar);
        
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecipeAdapter(getContext(), new ArrayList<>(), recipe -> {
            Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
            intent.putExtra("recipe", recipe);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_color);
        swipeRefreshLayout.setOnRefreshListener(this::loadLikedRecipes);
    }

    private void loadLikedRecipes() {
        showLoading(true);
        
        String userId = currentUser != null ? currentUser.getUid() : null;
        if (userId == null) {
            showError(getString(R.string.error_not_authenticated));
            showLoading(false);
            return;
        }
        Log.d("LikedRecipesDebug", "Loading liked recipes for user: " + userId);

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    com.example.reuvenbagrut.models.User user = documentSnapshot.toObject(com.example.reuvenbagrut.models.User.class);
                    if (user != null && user.getLikedRecipes() != null && !user.getLikedRecipes().isEmpty()) {
                        List<String> likedRecipeIds = user.getLikedRecipes();
                        Log.d("LikedRecipesDebug", "Fetched liked recipe IDs: " + likedRecipeIds.size() + " IDs: " + likedRecipeIds);

                        final List<Recipe> fetchedRecipes = new ArrayList<>();
                        final int[] completedFetches = {0};
                        final int totalFetches = likedRecipeIds.size();

                        if (totalFetches == 0) {
                            Log.d("LikedRecipesDebug", "No liked recipe IDs to fetch.");
                            updateRecipeList(new ArrayList<>());
                            showLoading(false);
                            return;
                        }

                        for (String recipeId : likedRecipeIds) {
                            db.collection("recipes").document(recipeId).get()
                                    .addOnSuccessListener(doc -> {
                                        if (doc.exists()) {
                                            Recipe recipe = doc.toObject(Recipe.class);
                                            if (recipe != null) {
                                                recipe.setId(doc.getId());
                                                fetchedRecipes.add(recipe);
                                            }
                                        }
                                        completedFetches[0]++;
                                        if (completedFetches[0] == totalFetches) {
                                            Log.d("LikedRecipesDebug", "Fetched " + fetchedRecipes.size() + " actual liked recipes.");
                                            updateRecipeList(fetchedRecipes);
                                            showLoading(false);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("LikedRecipesFragment", "Error fetching individual recipe: " + recipeId, e);
                                        completedFetches[0]++;
                                        if (completedFetches[0] == totalFetches) {
                                            Log.d("LikedRecipesDebug", "Finished fetching with errors. Fetched " + fetchedRecipes.size() + " recipes.");
                                            updateRecipeList(fetchedRecipes);
                                            showLoading(false);
                                        }
                                    });
                        }

                    } else {
                        // No liked recipes or user not found
                        Log.d("LikedRecipesDebug", "User has no liked recipes or user document not found.");
                        updateRecipeList(new ArrayList<>());
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

    private void updateRecipeList(List<Recipe> recipes) {
        if (adapter != null) {
            adapter.setRecipes(recipes);
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
                .setAction(R.string.retry, v -> loadLikedRecipes())
                .show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        adapter = null;
        if (shimmerLayout != null) {
            shimmerLayout.stopShimmer();
        }
    }
}
