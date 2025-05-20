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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
        adapter = new RecipeAdapter(getContext(), new ArrayList<>(), null);
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

        db.collection("users")
          .document(userId)
          .collection("liked_recipes")
          .orderBy("timestamp", Query.Direction.DESCENDING)
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful() && isAdded()) {
                  List<Recipe> likedRecipes = new ArrayList<>();
                  
                  for (QueryDocumentSnapshot document : task.getResult()) {
                      try {
                          Recipe recipe = new Recipe();
                          recipe.setId(document.getString("recipeId"));
                          recipe.setStrMeal(document.getString("title"));
                          recipe.setStrMealThumb(document.getString("imageUrl"));
                          recipe.setStrCategory(document.getString("category"));
                          if (document.getTimestamp("timestamp") != null) {
                              recipe.setTimestamp(document.getTimestamp("timestamp").toDate().getTime());
                          }
                          likedRecipes.add(recipe);
                      } catch (Exception e) {
                          Log.e("LikedRecipesFragment", "Error parsing recipe", e);
                      }
                  }
                  
                  updateRecipeList(likedRecipes);
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
