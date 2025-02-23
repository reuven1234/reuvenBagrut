package com.example.reuvenbagrut;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecipeApiClient {

    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static RecipeApiService getRecipeApiService() {
        return getRetrofitInstance().create(RecipeApiService.class);
    }
}

