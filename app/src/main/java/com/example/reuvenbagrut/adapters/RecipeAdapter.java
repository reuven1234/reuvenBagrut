package com.example.reuvenbagrut.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.models.Recipe;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private final Context context;
    private List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(Context context, List<Recipe> recipes, OnRecipeClickListener listener) {
        this.context = context;
        this.recipes = recipes;
        this.listener = listener;
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
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView recipeImage;
        private final MaterialTextView recipeName;
        // private final MaterialTextView recipeCategory;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeName = itemView.findViewById(R.id.recipeName);
            // recipeCategory = itemView.findViewById(R.id.recipeCategory);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onRecipeClick(recipes.get(position));
                }
            });
        }

        public void bind(Recipe recipe) {
            if (recipe == null) return;

            // Set meal name with null check
            String mealName = recipe.getStrMeal();
            recipeName.setText(mealName != null ? mealName : "Unknown Recipe");

            // Load image with null check
            String imageUrl = recipe.getStrMealThumb();
            Log.d("RecipeAdapter", "Loading image for recipe " + recipe.getStrMeal() + ": " + (imageUrl != null ? imageUrl.substring(0, Math.min(imageUrl.length(), 50)) + "..." : "null"));
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                try {
                    if (imageUrl.startsWith("data:image")) {
                        // Handle Base64 image
                        String base64Image = imageUrl.substring(imageUrl.indexOf(",") + 1);
                        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        Glide.with(context)
                            .load(decodedByte)
                            .centerCrop()
                            .placeholder(R.drawable.ic_recipe_placeholder)
                            .error(R.drawable.ic_recipe_placeholder)
                            .into(recipeImage);
                    } else {
                        // Handle regular URL
                        Glide.with(context)
                            .load(imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.ic_recipe_placeholder)
                            .error(R.drawable.ic_recipe_placeholder)
                            .into(recipeImage);
                    }
                } catch (Exception e) {
                    Log.e("RecipeAdapter", "Error loading image", e);
                    recipeImage.setImageResource(R.drawable.ic_recipe_placeholder);
                }
            } else {
                recipeImage.setImageResource(R.drawable.ic_recipe_placeholder);
            }
        }
    }
}
