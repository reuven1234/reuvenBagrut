package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import com.example.reuvenbagrut.adapters.RecipeAdapter;
import com.example.reuvenbagrut.activities.RecipeDetailActivity;

public class LikedRecipesFragment extends Fragment implements RecipeAdapter.OnRecipeClickListener {
    private static final String TAG = "LikedRecipesFragment";
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmerLayout;
    private TextView emptyStateText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        recipeList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_liked_recipes, container, false);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.likedRecipesRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new RecipeAdapter(recipeList, this);
        recyclerView.setAdapter(adapter);
        
        // Setup swipe refresh
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_color);
        swipeRefreshLayout.setOnRefreshListener(this::loadLikedRecipes);
        
        // Load recipes
        loadLikedRecipes();
        
        return view;
    }

    private void loadLikedRecipes() {
        if (currentUser == null) {
            showError(getString(R.string.error_not_authenticated));
            return;
        }

        showLoading(true);
        
        db.collection("users")
          .document(currentUser.getUid())
          .collection("favorites")
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful() && isAdded()) {
                  List<Recipe> recipes = new ArrayList<>();
                  for (DocumentSnapshot document : task.getResult()) {
                      Recipe recipe = document.toObject(Recipe.class);
                      if (recipe != null) {
                          recipe.setId(document.getId());
                          recipes.add(recipe);
                      }
                  }
                  updateRecipeList(recipes);
              } else if (isAdded()) {
                  showError(getString(R.string.error_loading_recipes));
              }
              showLoading(false);
          });
    }

    private void updateRecipeList(List<Recipe> recipes) {
        recipeList.clear();
        recipeList.addAll(recipes);
        adapter.updateRecipes(recipes);
        updateEmptyState(recipes.isEmpty());
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
    public void onRecipeClick(Recipe recipe) {
        if (getActivity() != null && recipe != null) {
            Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            startActivity(intent);
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
