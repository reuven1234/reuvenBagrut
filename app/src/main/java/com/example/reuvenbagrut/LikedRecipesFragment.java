package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import com.example.reuvenbagrut.adapters.RecipeAdapter;

public class LikedRecipesFragment extends Fragment
        implements RecipeAdapter.OnRecipeClickListener {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private TextView emptyStateText;
    private ProgressBar progressBar;

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
                R.layout.fragment_liked_recipes, container, false
        );

        // Bind views
        recyclerView    = view.findViewById(R.id.likedRecipesRecyclerView);
        emptyStateText  = view.findViewById(R.id.emptyStateText);
        progressBar     = view.findViewById(R.id.progressBar);

        // Recycler setup
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new RecipeAdapter(recipeList, this);
        recyclerView.setAdapter(adapter);

        // Load data
        loadLikedRecipes();
        return view;
    }

    private void loadLikedRecipes() {
        if (currentUser == null) {
            showError(getString(R.string.error_not_authenticated));
            return;
        }

        // Show loader
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        db.collection("users")
                .document(currentUser.getUid())
                .collection("favorites")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

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
                    .setAction(R.string.retry, v -> loadLikedRecipes())
                    .show();
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        if (getActivity() != null && recipe != null) {
            Intent intent = new Intent(getActivity(),
                    RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView.setAdapter(null);
        adapter = null;
    }
}
