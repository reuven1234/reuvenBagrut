package com.example.reuvenbagrut.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.models.Recipe;
import java.util.List;
import com.google.firebase.auth.FirebaseAuth;
import android.util.Log;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private Context context;
    private List<Recipe> recipes;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
        void onUserClick(String userId);
        void onLikeClick(Recipe recipe);
    }

    public RecipeAdapter(Context context, List<Recipe> recipes, OnRecipeClickListener listener) {
        this.context = context;
        this.recipes = recipes;
        this.listener = listener;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        
        // Set recipe title
        holder.recipeTitle.setText(recipe.getStrMeal());
        
        // Set user information
        holder.userName.setText(recipe.getUserName());
        
        // Set recipe details
        if (recipe.getStrCookingTime() != null && !recipe.getStrCookingTime().isEmpty()) {
            holder.recipeTime.setText(recipe.getStrCookingTime());
            holder.recipeTime.setVisibility(View.VISIBLE);
        } else {
            holder.recipeTime.setVisibility(View.GONE);
        }

        if (recipe.getStrServings() != null && !recipe.getStrServings().isEmpty()) {
            holder.recipeServings.setText(recipe.getStrServings());
            holder.recipeServings.setVisibility(View.VISIBLE);
        } else {
            holder.recipeServings.setVisibility(View.GONE);
        }

        if (recipe.getStrCategory() != null && !recipe.getStrCategory().isEmpty()) {
            holder.recipeCategory.setText(recipe.getStrCategory());
            holder.recipeCategory.setVisibility(View.VISIBLE);
        } else {
            holder.recipeCategory.setVisibility(View.GONE);
        }

        // Set ingredients
        List<String> ingredients = recipe.getIngredients();
        Log.d("RecipeAdapter", "Recipe: " + recipe.getStrMeal() + ", Ingredients: " + ingredients);
        if (ingredients != null && !ingredients.isEmpty()) {
            String joined = android.text.TextUtils.join(", ", ingredients);
            holder.recipeIngredients.setText("Ingredients: " + joined);
            holder.recipeIngredients.setVisibility(View.VISIBLE);
        } else {
            holder.recipeIngredients.setText("No ingredients available");
            holder.recipeIngredients.setVisibility(View.VISIBLE);
        }

        // Load recipe image
        if (recipe.getStrMealThumb() != null && !recipe.getStrMealThumb().isEmpty()) {
            Glide.with(context)
                .load(recipe.getStrMealThumb())
                .centerCrop()
                .into(holder.recipeImage);
        }

        // Load user image
        if (recipe.getUserImage() != null && !recipe.getUserImage().isEmpty()) {
            Glide.with(context)
                .load(recipe.getUserImage())
                .circleCrop()
                .into(holder.userImage);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });

        holder.userImage.setOnClickListener(v -> {
            if (listener != null && recipe.getUserId() != null) {
                listener.onUserClick(recipe.getUserId());
            }
        });
        
        holder.userName.setOnClickListener(v -> {
            if (listener != null && recipe.getUserId() != null) {
                listener.onUserClick(recipe.getUserId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes != null ? recipes.size() : 0;
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        ImageView userImage;
        TextView recipeTitle;
        TextView userName;
        TextView recipeTime;
        TextView recipeServings;
        TextView recipeCategory;
        TextView recipeIngredients;

        RecipeViewHolder(View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipe_image);
            userImage = itemView.findViewById(R.id.authorImage);
            recipeTitle = itemView.findViewById(R.id.recipe_title);
            userName = itemView.findViewById(R.id.authorName);
            recipeTime = itemView.findViewById(R.id.recipeTime);
            recipeServings = itemView.findViewById(R.id.recipeServings);
            recipeCategory = itemView.findViewById(R.id.recipeCategory);
            recipeIngredients = itemView.findViewById(R.id.recipe_ingredients);
        }
    }
} 