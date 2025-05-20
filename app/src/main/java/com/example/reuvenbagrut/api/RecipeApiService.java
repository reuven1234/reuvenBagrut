package com.example.reuvenbagrut.api;

import com.example.reuvenbagrut.models.RecipeApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RecipeApiService {
    /**
     * Search meals by name.
     * If you pass an empty string (“”), it returns all meals whose name contains “” (i.e. essentially all recipes).
     */
    @GET("search.php")
    Call<RecipeApiResponse> searchRecipes(@Query("s") String query);

    /**
     * Fetch 10 random meals in one call (so your RecyclerView can show a scrollable list).
     */
    @GET("random.php")
    Call<RecipeApiResponse> getRandomRecipes();

    /**
     * Lookup a single meal by its ID.
     */
    @GET("lookup.php")
    Call<RecipeApiResponse> getRecipeById(@Query("i") String id);

    /**
     * (Optional) Filter by category — useful if you want "Breakfast," "Lunch," etc.
     */
    @GET("filter.php")
    Call<RecipeApiResponse> getRecipesByCategory(@Query("c") String category);
}
