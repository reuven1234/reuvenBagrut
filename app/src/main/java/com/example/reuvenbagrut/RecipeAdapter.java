package com.example.reuvenbagrut;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.models.RecipeApiResponse.RecipeResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for showing RecipeResult items in a RecyclerView.
 * Requires that RecipeResult defines a getIngredients() returning List<String>.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<RecipeResult> recipes;
    private Context context;

    public RecipeAdapter(Context context) {
        this.context = context;
        this.recipes = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeResult recipe = recipes.get(position);
        holder.titleTextView.setText(recipe.getTitle());

        // Load thumbnail
        Glide.with(context)
                .load(recipe.getImageUrl())
                .placeholder(R.drawable.placeholder_recipe)
                .error(R.drawable.placeholder_recipe)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailActivity.class);
            // Pass core details
            intent.putExtra("recipe_id", recipe.getId());
            intent.putExtra("recipe_title", recipe.getTitle());
            intent.putExtra("recipe_image", recipe.getImageUrl());
            intent.putExtra("recipe_category", recipe.getStrCategory());
            intent.putExtra("recipe_instructions", recipe.getStrInstructions());

            // ingredients: requires RecipeResult.getIngredients()
            List<String> ingList = recipe.getIngredients();
            String ingredientsText;
            if (ingList != null && !ingList.isEmpty()) {
                // join with bullet
                String joined = TextUtils.join("\n• ", ingList);
                ingredientsText = joined.startsWith("• ") ? joined : "• " + joined;
            } else {
                ingredientsText = "No ingredients listed";
            }
            intent.putExtra("recipe_ingredients", ingredientsText);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    /**
     * Replace the current list of recipes.
     */
    public void setRecipes(List<RecipeResult> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    /**
     * Append new recipes and notify insertion.
     */
    public void addRecipes(List<RecipeResult> newRecipes) {
        int start = recipes.size();
        recipes.addAll(newRecipes);
        notifyItemRangeInserted(start, newRecipes.size());
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.recipe_image);
            titleTextView = itemView.findViewById(R.id.recipe_title);
        }
    }
}
