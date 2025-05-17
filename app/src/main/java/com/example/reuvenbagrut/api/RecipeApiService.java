package com.example.reuvenbagrut.api;

import com.example.reuvenbagrut.models.RecipeApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RecipeApiService {
    @GET("search.php")
    Call<RecipeApiResponse> searchRecipes(@Query("s") String query);

    @GET("random.php")
    Call<RecipeApiResponse> getRandomRecipes();

    @GET("lookup.php")
    Call<RecipeApiResponse> getRecipeById(@Query("i") String id);
} 