package com.example.reuvenbagrut.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> implements Filterable {
    private List<Recipe> recipes;
    private List<Recipe> allRecipes;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.allRecipes = new ArrayList<>(recipes);
        this.listener = listener;
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        this.allRecipes = new ArrayList<>(newRecipes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Recipe> filteredList = new ArrayList<>();
                
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(allRecipes);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Recipe recipe : allRecipes) {
                        if (recipe.getStrMeal().toLowerCase().contains(filterPattern)) {
                            filteredList.add(recipe);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                recipes.clear();
                recipes.addAll((List<Recipe>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final ImageView recipeImage;
        private final TextView recipeTitle;
        private final TextView recipeCategory;
        private final TextView authorName;

        RecipeViewHolder(View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeTitle = itemView.findViewById(R.id.recipeTitle);
            recipeCategory = itemView.findViewById(R.id.recipeCategory);
            authorName = itemView.findViewById(R.id.authorName);
        }

        void bind(Recipe recipe) {
            recipeTitle.setText(recipe.getStrMeal());
            recipeCategory.setText(recipe.getStrCategory());

            if (recipe.getStrMealThumb() != null && !recipe.getStrMealThumb().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(recipe.getStrMealThumb())
                    .centerCrop()
                    .into(recipeImage);
            }

            // Load author details if available
            if (recipe.getStrAuthor() != null) {
                authorName.setText(recipe.getStrAuthor());
                authorName.setVisibility(View.VISIBLE);
            } else {
                authorName.setVisibility(View.GONE);
            }
        }
    }
} 