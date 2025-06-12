package com.example.reuvenbagrut.api;

import com.example.reuvenbagrut.models.MealApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TheMealDbApi {
    String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";

    @GET("search.php")
    Call<MealApiResponse> searchMealsByName(@Query("s") String mealName);
} 