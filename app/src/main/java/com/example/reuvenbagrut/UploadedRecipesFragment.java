package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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

import java.util.ArrayList;
import java.util.List;

import com.example.reuvenbagrut.adapters.RecipeAdapter;
import com.example.reuvenbagrut.activities.RecipeDetailActivity;

public class UploadedRecipesFragment extends Fragment
        implements RecipeAdapter.OnRecipeClickListener {

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_uploaded_recipes, container, false
        );

        recyclerView        = view.findViewById(R.id.uploadedRecipesRecyclerView);
        swipeRefreshLayout  = view.findViewById(R.id.swipeRefreshLayout);
        shimmerLayout       = view.findViewById(R.id.shimmerLayout);
        emptyStateText      = view.findViewById(R.id.emptyStateText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecipeAdapter();
        adapter.setOnRecipeClickListener(this);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light
        );
        swipeRefreshLayout.setOnRefreshListener(this::loadUploadedRecipes);

        loadUploadedRecipes();
        return view;
    }

    private void loadUploadedRecipes() {
        if (currentUser == null) {
            showError(getString(R.string.error_not_authenticated));
            return;
        }

        // Show shimmer
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        recyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);

        db.collection("recipes")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (task.isSuccessful() && isAdded()) {
                        List<Recipe> recipes = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Recipe r = doc.toObject(Recipe.class);
                            if (r != null) {
                                r.setId(doc.getId());
                                recipes.add(r);
                            }
                        }
                        updateRecipeList(recipes);
                    } else if (isAdded()) {
                        showError(getString(R.string.error_loading_recipes));
                    }
                });
    }

    private void updateRecipeList(List<Recipe> recipes) {
        recipeList.clear();
        recipeList.addAll(recipes);
        adapter.updateRecipes(recipes);

        boolean empty = recipes.isEmpty();
        emptyStateText.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showError(String msg) {
        if (getView() != null) {
            Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry, v -> loadUploadedRecipes())
                    .show();
        }
    }

    @Override
    public void onRecipeClick(Object recipe) {
        if (recipe instanceof Recipe) {
            navigateToRecipeDetail((Recipe) recipe);
        }
    }

    private void navigateToRecipeDetail(Recipe recipe) {
        if (recipe != null) {
            Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView.setAdapter(null);
        adapter = null;
        shimmerLayout.stopShimmer();
    }
}
