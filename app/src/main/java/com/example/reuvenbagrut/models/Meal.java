package com.example.reuvenbagrut.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Meal {
    @SerializedName("idMeal")
    private String idMeal;
    @SerializedName("strMeal")
    private String strMeal;
    @SerializedName("strDrinkAlternate")
    private String strDrinkAlternate;
    @SerializedName("strCategory")
    private String strCategory;
    @SerializedName("strArea")
    private String strArea;
    @SerializedName("strInstructions")
    private String strInstructions;
    @SerializedName("strMealThumb")
    private String strMealThumb;
    @SerializedName("strTags")
    private String strTags;
    @SerializedName("strYoutube")
    private String strYoutube;

    // Ingredients and Measures (up to 20)
    @SerializedName("strIngredient1")
    private String strIngredient1;
    @SerializedName("strIngredient2")
    private String strIngredient2;
    @SerializedName("strIngredient3")
    private String strIngredient3;
    @SerializedName("strIngredient4")
    private String strIngredient4;
    @SerializedName("strIngredient5")
    private String strIngredient5;
    @SerializedName("strIngredient6")
    private String strIngredient6;
    @SerializedName("strIngredient7")
    private String strIngredient7;
    @SerializedName("strIngredient8")
    private String strIngredient8;
    @SerializedName("strIngredient9")
    private String strIngredient9;
    @SerializedName("strIngredient10")
    private String strIngredient10;
    @SerializedName("strIngredient11")
    private String strIngredient11;
    @SerializedName("strIngredient12")
    private String strIngredient12;
    @SerializedName("strIngredient13")
    private String strIngredient13;
    @SerializedName("strIngredient14")
    private String strIngredient14;
    @SerializedName("strIngredient15")
    private String strIngredient15;
    @SerializedName("strIngredient16")
    private String strIngredient16;
    @SerializedName("strIngredient17")
    private String strIngredient17;
    @SerializedName("strIngredient18")
    private String strIngredient18;
    @SerializedName("strIngredient19")
    private String strIngredient19;
    @SerializedName("strIngredient20")
    private String strIngredient20;

    @SerializedName("strMeasure1")
    private String strMeasure1;
    @SerializedName("strMeasure2")
    private String strMeasure2;
    @SerializedName("strMeasure3")
    private String strMeasure3;
    @SerializedName("strMeasure4")
    private String strMeasure4;
    @SerializedName("strMeasure5")
    private String strMeasure5;
    @SerializedName("strMeasure6")
    private String strMeasure6;
    @SerializedName("strMeasure7")
    private String strMeasure7;
    @SerializedName("strMeasure8")
    private String strMeasure8;
    @SerializedName("strMeasure9")
    private String strMeasure9;
    @SerializedName("strMeasure10")
    private String strMeasure10;
    @SerializedName("strMeasure11")
    private String strMeasure11;
    @SerializedName("strMeasure12")
    private String strMeasure12;
    @SerializedName("strMeasure13")
    private String strMeasure13;
    @SerializedName("strMeasure14")
    private String strMeasure14;
    @SerializedName("strMeasure15")
    private String strMeasure15;
    @SerializedName("strMeasure16")
    private String strMeasure16;
    @SerializedName("strMeasure17")
    private String strMeasure17;
    @SerializedName("strMeasure18")
    private String strMeasure18;
    @SerializedName("strMeasure19")
    private String strMeasure19;
    @SerializedName("strMeasure20")
    private String strMeasure20;

    @SerializedName("strSource")
    private String strSource;
    @SerializedName("dateModified")
    private String dateModified;

    // Getters
    public String getIdMeal() { return idMeal; }
    public String getStrMeal() { return strMeal; }
    public String getStrCategory() { return strCategory; }
    public String getStrArea() { return strArea; }
    public String getStrInstructions() { return strInstructions; }
    public String getStrMealThumb() { return strMealThumb; }

    public List<String> getIngredients() {
        List<String> ingredients = new ArrayList<>();
        if (strIngredient1 != null && !strIngredient1.isEmpty()) ingredients.add(strIngredient1);
        if (strIngredient2 != null && !strIngredient2.isEmpty()) ingredients.add(strIngredient2);
        if (strIngredient3 != null && !strIngredient3.isEmpty()) ingredients.add(strIngredient3);
        if (strIngredient4 != null && !strIngredient4.isEmpty()) ingredients.add(strIngredient4);
        if (strIngredient5 != null && !strIngredient5.isEmpty()) ingredients.add(strIngredient5);
        if (strIngredient6 != null && !strIngredient6.isEmpty()) ingredients.add(strIngredient6);
        if (strIngredient7 != null && !strIngredient7.isEmpty()) ingredients.add(strIngredient7);
        if (strIngredient8 != null && !strIngredient8.isEmpty()) ingredients.add(strIngredient8);
        if (strIngredient9 != null && !strIngredient9.isEmpty()) ingredients.add(strIngredient9);
        if (strIngredient10 != null && !strIngredient10.isEmpty()) ingredients.add(strIngredient10);
        if (strIngredient11 != null && !strIngredient11.isEmpty()) ingredients.add(strIngredient11);
        if (strIngredient12 != null && !strIngredient12.isEmpty()) ingredients.add(strIngredient12);
        if (strIngredient13 != null && !strIngredient13.isEmpty()) ingredients.add(strIngredient13);
        if (strIngredient14 != null && !strIngredient14.isEmpty()) ingredients.add(strIngredient14);
        if (strIngredient15 != null && !strIngredient15.isEmpty()) ingredients.add(strIngredient15);
        if (strIngredient16 != null && !strIngredient16.isEmpty()) ingredients.add(strIngredient16);
        if (strIngredient17 != null && !strIngredient17.isEmpty()) ingredients.add(strIngredient17);
        if (strIngredient18 != null && !strIngredient18.isEmpty()) ingredients.add(strIngredient18);
        if (strIngredient19 != null && !strIngredient19.isEmpty()) ingredients.add(strIngredient19);
        if (strIngredient20 != null && !strIngredient20.isEmpty()) ingredients.add(strIngredient20);
        return ingredients;
    }

    public List<String> getMeasures() {
        List<String> measures = new ArrayList<>();
        if (strMeasure1 != null && !strMeasure1.isEmpty()) measures.add(strMeasure1);
        if (strMeasure2 != null && !strMeasure2.isEmpty()) measures.add(strMeasure2);
        if (strMeasure3 != null && !strMeasure3.isEmpty()) measures.add(strMeasure3);
        if (strMeasure4 != null && !strMeasure4.isEmpty()) measures.add(strMeasure4);
        if (strMeasure5 != null && !strMeasure5.isEmpty()) measures.add(strMeasure5);
        if (strMeasure6 != null && !strMeasure6.isEmpty()) measures.add(strMeasure6);
        if (strMeasure7 != null && !strMeasure7.isEmpty()) measures.add(strMeasure7);
        if (strMeasure8 != null && !strMeasure8.isEmpty()) measures.add(strMeasure8);
        if (strMeasure9 != null && !strMeasure9.isEmpty()) measures.add(strMeasure9);
        if (strMeasure10 != null && !strMeasure10.isEmpty()) measures.add(strMeasure10);
        if (strMeasure11 != null && !strMeasure11.isEmpty()) measures.add(strMeasure11);
        if (strMeasure12 != null && !strMeasure12.isEmpty()) measures.add(strMeasure12);
        if (strMeasure13 != null && !strMeasure13.isEmpty()) measures.add(strMeasure13);
        if (strMeasure14 != null && !strMeasure14.isEmpty()) measures.add(strMeasure14);
        if (strMeasure15 != null && !strMeasure15.isEmpty()) measures.add(strMeasure15);
        if (strMeasure16 != null && !strMeasure16.isEmpty()) measures.add(strMeasure16);
        if (strMeasure17 != null && !strMeasure17.isEmpty()) measures.add(strMeasure17);
        if (strMeasure18 != null && !strMeasure18.isEmpty()) measures.add(strMeasure18);
        if (strMeasure19 != null && !strMeasure19.isEmpty()) measures.add(strMeasure19);
        if (strMeasure20 != null && !strMeasure20.isEmpty()) measures.add(strMeasure20);
        return measures;
    }

    public List<String> getIngredientMeasures() {
        List<String> ingredientMeasures = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            try {
                String ingredient = (String) getClass().getDeclaredField("strIngredient" + i).get(this);
                String measure = (String) getClass().getDeclaredField("strMeasure" + i).get(this);

                if (ingredient != null && !ingredient.isEmpty()) {
                    String combined = (measure != null && !measure.isEmpty()) ? measure + " " + ingredient : ingredient;
                    ingredientMeasures.add(combined);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return ingredientMeasures;
    }

    // Setters (if needed, but for API response, typically only getters are used)

} 