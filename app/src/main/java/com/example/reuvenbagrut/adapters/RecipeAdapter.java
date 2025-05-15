package com.example.reuvenbagrut.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.models.RecipeApiResponse.RecipeResult;
import com.example.reuvenbagrut.Recipe;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Object> recipes = new ArrayList<>();
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Object recipe);
    }

    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void setRecipes(List<RecipeResult> apiRecipes) {
        this.recipes = new ArrayList<>(apiRecipes);
        notifyDataSetChanged();
    }

    public void updateRecipes(List<Recipe> localRecipes) {
        this.recipes = new ArrayList<>(localRecipes);
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
        Object recipe = recipes.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final ImageView recipeImage;
        private final TextView recipeTitle;
        private final TextView recipeTime;
        private final TextView recipeServings;
        private final TextView recipeCategory;
        private final TextView authorName;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeTitle = itemView.findViewById(R.id.recipeTitle);
            recipeTime = itemView.findViewById(R.id.recipeTime);
            recipeServings = itemView.findViewById(R.id.recipeServings);
            recipeCategory = itemView.findViewById(R.id.recipeCategory);
            authorName = itemView.findViewById(R.id.authorName);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRecipeClick(recipes.get(position));
                }
            });
        }

        void bind(Object recipe) {
            if (recipe instanceof RecipeResult) {
                bindApiRecipe((RecipeResult) recipe);
            } else if (recipe instanceof Recipe) {
                bindLocalRecipe((Recipe) recipe);
            }
        }

        private void bindApiRecipe(RecipeResult recipe) {
            recipeTitle.setText(recipe.getTitle());
            recipeTime.setText(recipe.getReadyInMinutes() + " min");
            recipeServings.setText(recipe.getServings() + " servings");
            recipeCategory.setVisibility(View.GONE);
            authorName.setVisibility(View.GONE);

            Glide.with(itemView.getContext())
                    .load(recipe.getImageUrl())
                    .centerCrop()
                    .into(recipeImage);
        }

        private void bindLocalRecipe(Recipe recipe) {
            recipeTitle.setText(recipe.getStrMeal());
            recipeCategory.setText(recipe.getStrCategory());
            recipeTime.setVisibility(View.GONE);
            recipeServings.setVisibility(View.GONE);

            if (recipe.getStrMealThumb() != null && !recipe.getStrMealThumb().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(recipe.getStrMealThumb())
                        .centerCrop()
                        .into(recipeImage);
            }

            if (recipe.getStrAuthor() != null) {
                authorName.setText(recipe.getStrAuthor());
                authorName.setVisibility(View.VISIBLE);
            } else {
                authorName.setVisibility(View.GONE);
            }
        }
    }
} 