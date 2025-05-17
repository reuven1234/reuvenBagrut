package com.example.reuvenbagrut.adapters;

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
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.models.RecipeApiResponse.RecipeResult;
import com.example.reuvenbagrut.Recipe;
import com.example.reuvenbagrut.activities.RecipeDetailActivity;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Object> recipes = new ArrayList<>();
    private Context context;

    public RecipeAdapter(Context context) {
        this.context = context;
    }

    public void setRecipes(List<RecipeResult> apiRecipes) {
        if (apiRecipes != null) {
            this.recipes = new ArrayList<>(apiRecipes);
            notifyDataSetChanged();
        }
    }

    public void updateRecipes(List<Recipe> localRecipes) {
        if (localRecipes != null) {
            this.recipes = new ArrayList<>(localRecipes);
            notifyDataSetChanged();
        }
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
        if (recipe != null) {
            holder.bind(recipe);
        }
    }

    @Override
    public int getItemCount() {
        return recipes != null ? recipes.size() : 0;
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final ImageView recipeImage;
        private final TextView recipeTitle;
        private final TextView recipeCategory;
        private final TextView authorName;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipe_image);
            recipeTitle = itemView.findViewById(R.id.recipe_title);
            recipeCategory = itemView.findViewById(R.id.recipeCategory);
            authorName = itemView.findViewById(R.id.authorName);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Object recipe = recipes.get(position);
                    Intent intent = new Intent(context, RecipeDetailActivity.class);
                    if (recipe instanceof RecipeResult) {
                        intent.putExtra("recipe_id", ((RecipeResult) recipe).getIdMeal());
                        intent.putExtra("is_api_recipe", true);
                    } else if (recipe instanceof Recipe) {
                        intent.putExtra("recipe_id", ((Recipe) recipe).getId());
                    }
                    context.startActivity(intent);
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
            if (recipe != null) {
                recipeTitle.setText(recipe.getStrMeal());
                recipeCategory.setText(recipe.getStrCategory());
                recipeCategory.setVisibility(View.VISIBLE);
                authorName.setVisibility(View.GONE);
                
                if (recipe.getStrMealThumb() != null && !recipe.getStrMealThumb().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(recipe.getStrMealThumb())
                            .centerCrop()
                            .into(recipeImage);
                }
            }
        }

        private void bindLocalRecipe(Recipe recipe) {
            if (recipe != null) {
                recipeTitle.setText(recipe.getStrMeal());
                recipeCategory.setText(recipe.getStrCategory());
                recipeCategory.setVisibility(View.VISIBLE);
                
                if (recipe.getStrMealThumb() != null && !recipe.getStrMealThumb().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(recipe.getStrMealThumb())
                            .centerCrop()
                            .into(recipeImage);
                }
                
                if (recipe.getStrAuthor() != null && !recipe.getStrAuthor().isEmpty()) {
                    authorName.setText(recipe.getStrAuthor());
                    authorName.setVisibility(View.VISIBLE);
                } else {
                    authorName.setVisibility(View.GONE);
                }
            }
        }
    }
} 