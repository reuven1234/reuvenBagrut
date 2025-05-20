package com.example.reuvenbagrut.models;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class RecipeApiResponse {
    private List<RecipeResult> meals;

    public List<RecipeResult> getMeals() { return meals; }
    public void setMeals(List<RecipeResult> meals) { this.meals = meals; }

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
        private String strCookingTime;
        // Ingredient fields from 1 to 20
        private String strIngredient1;
        private String strIngredient2;
        private String strIngredient3;
        private String strIngredient4;
        private String strIngredient5;
        private String strIngredient6;
        private String strIngredient7;
        private String strIngredient8;
        private String strIngredient9;
        private String strIngredient10;
        private String strIngredient11;
        private String strIngredient12;
        private String strIngredient13;
        private String strIngredient14;
        private String strIngredient15;
        private String strIngredient16;
        private String strIngredient17;
        private String strIngredient18;
        private String strIngredient19;
        private String strIngredient20;

        // Getters and Setters for all fields
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
        public String getStrIngredient1() { return strIngredient1; }
        public void setStrIngredient1(String strIngredient1) { this.strIngredient1 = strIngredient1; }
        public String getStrIngredient2() { return strIngredient2; }
        public void setStrIngredient2(String strIngredient2) { this.strIngredient2 = strIngredient2; }
        public String getStrIngredient3() { return strIngredient3; }
        public void setStrIngredient3(String strIngredient3) { this.strIngredient3 = strIngredient3; }
        public String getStrIngredient4() { return strIngredient4; }
        public void setStrIngredient4(String strIngredient4) { this.strIngredient4 = strIngredient4; }
        public String getStrIngredient5() { return strIngredient5; }
        public void setStrIngredient5(String strIngredient5) { this.strIngredient5 = strIngredient5; }
        public String getStrIngredient6() { return strIngredient6; }
        public void setStrIngredient6(String strIngredient6) { this.strIngredient6 = strIngredient6; }
        public String getStrIngredient7() { return strIngredient7; }
        public void setStrIngredient7(String strIngredient7) { this.strIngredient7 = strIngredient7; }
        public String getStrIngredient8() { return strIngredient8; }
        public void setStrIngredient8(String strIngredient8) { this.strIngredient8 = strIngredient8; }
        public String getStrIngredient9() { return strIngredient9; }
        public void setStrIngredient9(String strIngredient9) { this.strIngredient9 = strIngredient9; }
        public String getStrIngredient10() { return strIngredient10; }
        public void setStrIngredient10(String strIngredient10) { this.strIngredient10 = strIngredient10; }
        public String getStrIngredient11() { return strIngredient11; }
        public void setStrIngredient11(String strIngredient11) { this.strIngredient11 = strIngredient11; }
        public String getStrIngredient12() { return strIngredient12; }
        public void setStrIngredient12(String strIngredient12) { this.strIngredient12 = strIngredient12; }
        public String getStrIngredient13() { return strIngredient13; }
        public void setStrIngredient13(String strIngredient13) { this.strIngredient13 = strIngredient13; }
        public String getStrIngredient14() { return strIngredient14; }
        public void setStrIngredient14(String strIngredient14) { this.strIngredient14 = strIngredient14; }
        public String getStrIngredient15() { return strIngredient15; }
        public void setStrIngredient15(String strIngredient15) { this.strIngredient15 = strIngredient15; }
        public String getStrIngredient16() { return strIngredient16; }
        public void setStrIngredient16(String strIngredient16) { this.strIngredient16 = strIngredient16; }
        public String getStrIngredient17() { return strIngredient17; }
        public void setStrIngredient17(String strIngredient17) { this.strIngredient17 = strIngredient17; }
        public String getStrIngredient18() { return strIngredient18; }
        public void setStrIngredient18(String strIngredient18) { this.strIngredient18 = strIngredient18; }
        public String getStrIngredient19() { return strIngredient19; }
        public void setStrIngredient19(String strIngredient19) { this.strIngredient19 = strIngredient19; }
        public String getStrIngredient20() { return strIngredient20; }
        public void setStrIngredient20(String strIngredient20) { this.strIngredient20 = strIngredient20; }
        public String getStrCookingTime() { return strCookingTime; }
        public void setStrCookingTime(String strCookingTime) { this.strCookingTime = strCookingTime; }

        // Convenience getters
        public String getTitle() { return strMeal; }
        public String getImageUrl() { return strMealThumb; }
        public String getId() { return idMeal; }

        /**
         * Collects non-empty ingredient fields into a List
         */
        public List<String> getIngredients() {
            List<String> list = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                try {
                    String key = (String) RecipeResult.class
                            .getDeclaredField("strIngredient" + i)
                            .get(this);
                    if (!TextUtils.isEmpty(key)) list.add(key.trim());
                } catch (Exception e) {
                    // ignore
                }
            }
            return list;
        }

        /**
         * Estimates cooking time based on recipe complexity
         * @return Estimated cooking time in minutes
         */
        public String getEstimatedCookingTime() {
            int ingredientCount = getIngredients().size();
            int instructionLength = strInstructions != null ? strInstructions.length() : 0;
            
            // Base time of 30 minutes
            int estimatedTime = 30;
            
            // Add time based on number of ingredients (2 minutes per ingredient)
            estimatedTime += ingredientCount * 2;
            
            // Add time based on instruction length (1 minute per 100 characters)
            estimatedTime += instructionLength / 100;
            
            // Round to nearest 5 minutes
            estimatedTime = ((estimatedTime + 4) / 5) * 5;
            
            return estimatedTime + " mins";
        }
    }
}