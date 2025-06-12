package com.example.reuvenbagrut.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.PropertyName;

import java.util.List;
import java.util.ArrayList;

public class Recipe implements Parcelable {
    private String id;
    private String strMeal;
    private String strCategory;
    private String strArea;
    private String strInstructions;
    private String strMealThumb;
    private String strTags;
    private String strYoutube;
    private String strSource;
    private String strCookingTime;
    private String strServings;
    private String strDifficultyLevel;
    private String strAuthorId;
    private String strAuthor;
    private String strAuthorImage;
    private String userId;
    private String userName;
    private String userImage;
    @PropertyName("ingredients")
    private List<String> ingredients;
    @PropertyName("ingredientsString")
    private String ingredientsString;
    private List<String> steps;
    private int likesCount;
    private boolean isLiked;
    private long timestamp;
    private List<String> likedBy;

    public Recipe() {
        this.likedBy = new ArrayList<>();
        this.ingredients = new ArrayList<>();
    }

    public Recipe(String id, String strMeal, String strCategory, String strCookingTime,
                  String strDifficultyLevel, String strMealThumb, String userId,
                  String userName, String userImage) {
        this.id = id;
        this.strMeal = strMeal;
        this.strCategory = strCategory;
        this.strCookingTime = strCookingTime;
        this.strDifficultyLevel = strDifficultyLevel;
        this.strMealThumb = strMealThumb;
        this.userId = userId;
        this.userName = userName;
        this.userImage = userImage;
        this.likedBy = new ArrayList<>();
        this.ingredients = new ArrayList<>();
    }

    protected Recipe(Parcel in) {
        id = in.readString();
        strMeal = in.readString();
        strCategory = in.readString();
        strArea = in.readString();
        strInstructions = in.readString();
        strMealThumb = in.readString();
        strTags = in.readString();
        strYoutube = in.readString();
        strSource = in.readString();
        strCookingTime = in.readString();
        strServings = in.readString();
        strDifficultyLevel = in.readString();
        strAuthorId = in.readString();
        strAuthor = in.readString();
        strAuthorImage = in.readString();
        userId = in.readString();
        userName = in.readString();
        userImage = in.readString();
        timestamp = in.readLong();
        likedBy = new ArrayList<>();
        in.readStringList(likedBy);
        ingredients = in.createStringArrayList();
        // Note: steps and other custom fields can be added here if needed.
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(strMeal);
        dest.writeString(strCategory);
        dest.writeString(strArea);
        dest.writeString(strInstructions);
        dest.writeString(strMealThumb);
        dest.writeString(strTags);
        dest.writeString(strYoutube);
        dest.writeString(strSource);
        dest.writeString(strCookingTime);
        dest.writeString(strServings);
        dest.writeString(strDifficultyLevel);
        dest.writeString(strAuthorId);
        dest.writeString(strAuthor);
        dest.writeString(strAuthorImage);
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(userImage);
        dest.writeLong(timestamp);
        dest.writeStringList(likedBy);
        dest.writeStringList(ingredients);
        // Note: steps and other custom fields can be written here if needed.
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public String getStrCookingTime() { return strCookingTime; }
    public void setStrCookingTime(String strCookingTime) { this.strCookingTime = strCookingTime; }

    public String getStrServings() { return strServings; }
    public void setStrServings(String strServings) { this.strServings = strServings; }

    public String getStrDifficultyLevel() { return strDifficultyLevel; }
    public void setStrDifficultyLevel(String strDifficultyLevel) { this.strDifficultyLevel = strDifficultyLevel; }

    public String getStrAuthorId() { return strAuthorId; }
    public void setStrAuthorId(String strAuthorId) { this.strAuthorId = strAuthorId; }

    public String getStrAuthor() { return strAuthor; }
    public void setStrAuthor(String strAuthor) { this.strAuthor = strAuthor; }

    public String getStrAuthorImage() { return strAuthorImage; }
    public void setStrAuthorImage(String strAuthorImage) { this.strAuthorImage = strAuthorImage; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserImage() { return userImage; }
    public void setUserImage(String userImage) { this.userImage = userImage; }

    @PropertyName("ingredients")
    public List<String> getIngredients() {
        return ingredients == null ? new ArrayList<>() : ingredients;
    }
    @PropertyName("ingredients")
    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    @PropertyName("ingredientsString")
    public String getIngredientsString() {
        return ingredientsString;
    }

    @PropertyName("ingredientsString")
    public void setIngredientsString(String ingredientsString) {
        this.ingredientsString = ingredientsString;
    }

    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }

    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public List<String> getLikedBy() { return likedBy; }
    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }

    public boolean isLikedByUser(String userId) {
        return likedBy != null && likedBy.contains(userId);
    }

    public void addLike(String userId) {
        if (likedBy == null) {
            likedBy = new ArrayList<>();
        }
        if (!likedBy.contains(userId)) {
            likedBy.add(userId);
        }
    }

    public void removeLike(String userId) {
        if (likedBy != null) {
            likedBy.remove(userId);
        }
    }
}
