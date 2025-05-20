package com.example.reuvenbagrut;

import com.example.reuvenbagrut.models.RecipeApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RecipeApiService {
    @GET("search.php")
    Call<RecipeApiResponse> searchRecipe(@Query("s") String recipeName);
}
