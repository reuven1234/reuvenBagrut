package com.example.reuvenbagrut.models;

import java.util.List;

public class RecipeApiResponse {
    private List<RecipeResult> meals;

    public List<RecipeResult> getMeals() {
        return meals;
    }

    public void setMeals(List<RecipeResult> meals) {
        this.meals = meals;
    }

    public static class RecipeResult {
        private String idMeal;
        private String strMeal;
        private String strCategory;
        private String strArea;
        private String strInstructions;
        private String strMealThumb;
        private String strTags;
        private String strYoutube;
        private String strSource;
        private String strImageSource;
        private String strCreativeCommonsConfirmed;
        private String dateModified;
        private String userId;

        // Getters and Setters
        public String getIdMeal() { return idMeal; }
        public void setIdMeal(String idMeal) { this.idMeal = idMeal; }

        public String getStrMeal() { return strMeal; }
        public void setStrMeal(String strMeal) { this.strMeal = strMeal; }

        public String getStrCategory() { return strCategory; }
        public void setStrCategory(String strCategory) { this.strCategory = strCategory; }

        public String getStrArea() { return strArea; }
        public void setStrArea(String strArea) { this.strArea = strArea; }

        public String getStrInstructions() { return strInstructions; }
        public void setStrInstructions(String strInstructions) { this.strInstructions = strInstructions; }

        public String getStrMealThumb() { return strMealThumb; }
        public void setStrMealThumb(String strMealThumb) { this.strMealThumb = strMealThumb; }

        public String getStrTags() { return strTags; }
        public void setStrTags(String strTags) { this.strTags = strTags; }

        public String getStrYoutube() { return strYoutube; }
        public void setStrYoutube(String strYoutube) { this.strYoutube = strYoutube; }

        public String getStrSource() { return strSource; }
        public void setStrSource(String strSource) { this.strSource = strSource; }

        public String getStrImageSource() { return strImageSource; }
        public void setStrImageSource(String strImageSource) { this.strImageSource = strImageSource; }

        public String getStrCreativeCommonsConfirmed() { return strCreativeCommonsConfirmed; }
        public void setStrCreativeCommonsConfirmed(String strCreativeCommonsConfirmed) { this.strCreativeCommonsConfirmed = strCreativeCommonsConfirmed; }

        public String getDateModified() { return dateModified; }
        public void setDateModified(String dateModified) { this.dateModified = dateModified; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        // Helper methods for RecipeAdapter
        public String getTitle() { return strMeal; }
        public String getImageUrl() { return strMealThumb; }
        public String getId() { return idMeal; }
    }
} 