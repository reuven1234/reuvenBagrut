package com.example.reuvenbagrut;

import android.content.Context;
import android.content.Intent;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeResult recipe = recipes.get(position);
        holder.titleTextView.setText(recipe.getTitle());
        
        // Load image using Glide
        Glide.with(context)
            .load(recipe.getImageUrl())
            .placeholder(R.drawable.placeholder_recipe)
            .error(R.drawable.placeholder_recipe)
            .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            intent.putExtra("recipe_title", recipe.getTitle());
            intent.putExtra("recipe_image", recipe.getImageUrl());
            intent.putExtra("recipe_category", recipe.getStrCategory());
            intent.putExtra("recipe_instructions", recipe.getStrInstructions());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void setRecipes(List<RecipeResult> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    public void addRecipes(List<RecipeResult> newRecipes) {
        int startPosition = recipes.size();
        recipes.addAll(newRecipes);
        notifyItemRangeInserted(startPosition, newRecipes.size());
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
