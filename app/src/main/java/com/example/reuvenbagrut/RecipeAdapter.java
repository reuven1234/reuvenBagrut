package com.example.reuvenbagrut;

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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> implements Filterable {

    private List<Recipe> recipes = new ArrayList<>();
    private List<Recipe> allRecipes = new ArrayList<>();
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe, int position);
    }

    // Default constructor
    public RecipeAdapter() {
        this.recipes = new ArrayList<>();
        this.allRecipes = new ArrayList<>();
    }

    // Constructor with initial list
    public RecipeAdapter(List<Recipe> recipes) {
        this.recipes = recipes != null ? recipes : new ArrayList<>();
        this.allRecipes = new ArrayList<>(this.recipes);
    }

    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes != null ? recipes : new ArrayList<>();
        this.allRecipes = new ArrayList<>(this.recipes);
        notifyDataSetChanged();
    }

    // Alias for setRecipes for compatibility
    public void setRecipeList(List<Recipe> recipes) {
        setRecipes(recipes);
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
    }

    @Override
    public int getItemCount() {
        return recipes != null ? recipes.size() : 0;
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
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                recipes.clear();
                recipes.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        private ImageView recipeImage;
        private TextView recipeName;
        private TextView recipeCategory;
        private TextView authorName;
        private ImageView authorImage;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeName = itemView.findViewById(R.id.recipeTitle);
            recipeCategory = itemView.findViewById(R.id.recipeCategory);
            authorName = itemView.findViewById(R.id.authorName);
            authorImage = itemView.findViewById(R.id.authorImage);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRecipeClick(recipes.get(position), position);
                }
            });
        }

        void bind(Recipe recipe) {
            recipeName.setText(recipe.getStrMeal());
            
            if (recipe.getStrCategory() != null) {
                recipeCategory.setText(recipe.getStrCategory());
                recipeCategory.setVisibility(View.VISIBLE);
            } else {
                recipeCategory.setVisibility(View.GONE);
            }
            
            if (recipe.getStrAuthor() != null) {
                authorName.setText(recipe.getStrAuthor());
                authorName.setVisibility(View.VISIBLE);
            } else {
                authorName.setVisibility(View.GONE);
            }
            
            // Load recipe image
            if (recipe.getStrMealThumb() != null && !recipe.getStrMealThumb().isEmpty()) {
                Glide.with(recipeImage.getContext())
                    .load(recipe.getStrMealThumb())
                    .apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(recipeImage);
            } else {
                recipeImage.setImageResource(R.drawable.placeholder_image);
            }
            
            // Load author image
            if (recipe.getStrAuthorImage() != null && !recipe.getStrAuthorImage().isEmpty()) {
                Glide.with(authorImage.getContext())
                    .load(recipe.getStrAuthorImage())
                    .apply(new RequestOptions()
                        .placeholder(R.drawable.avatar_placeholder)
                        .error(R.drawable.avatar_placeholder)
                        .circleCrop())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(authorImage);
            } else {
                authorImage.setImageResource(R.drawable.avatar_placeholder);
            }
        }
    }
}
