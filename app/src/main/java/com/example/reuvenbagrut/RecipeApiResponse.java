package com.example.reuvenbagrut;

import java.util.List;



public class RecipeApiResponse {
    private List<Recipe> meals;

    public List<Recipe> getMeals() {
        return meals;
    }

    public void setMeals(List<Recipe> meals) {
        this.meals = meals;
    }
}
