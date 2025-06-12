package com.example.reuvenbagrut.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MealApiResponse {
    @SerializedName("meals")
    private List<Meal> meals;

    public List<Meal> getMeals() {
        return meals;
    }
} 