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
        holder.recipeTitle.setText(recipe.getStrMeal());
        holder.userName.setText(recipe.getUserName());

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
            if (listener != null) {
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
        // ImageButton likeButton; // Remove or comment out if not in layout

        RecipeViewHolder(View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipe_image);
            userImage = itemView.findViewById(R.id.authorImage);
            recipeTitle = itemView.findViewById(R.id.recipe_title);
            userName = itemView.findViewById(R.id.authorName);
            // likeButton = itemView.findViewById(R.id.likeButton); // Remove or comment out if not in layout
        }
    }
} 