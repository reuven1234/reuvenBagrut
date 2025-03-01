package com.example.reuvenbagrut;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UploadedRecipesFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipeList = new ArrayList<>();

    public UploadedRecipesFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_uploaded_recipes, container, false);
        recyclerView = view.findViewById(R.id.uploadedRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 columns grid
        adapter = new RecipeAdapter(recipeList);
        recyclerView.setAdapter(adapter);
        loadUploadedRecipes();
        return view;
    }

    private void loadUploadedRecipes() {
        FirebaseFirestore.getInstance().collection("recipes")
                .whereEqualTo("authorId", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recipeList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Recipe recipe = doc.toObject(Recipe.class);
                        if (recipe != null) {
                            recipe.setId(doc.getId());
                            recipeList.add(recipe);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
