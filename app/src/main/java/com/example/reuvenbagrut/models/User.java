package com.example.reuvenbagrut.models;

import java.util.List;
import java.util.ArrayList;

public class User {
    private String id;
    private String email;
    private String name;
    private String profileImageUrl;
    private String password;
    private String bio;
    private String imageUrl;
    private List<String> followers;
    private List<String> following;
    private List<String> likedRecipes;
    private List<String> uploadedRecipes;

    public User() {
        // Required empty constructor for Firestore
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        this.likedRecipes = new ArrayList<>();
        this.uploadedRecipes = new ArrayList<>();
    }

    public User(String id, String email, String name, String profileImageUrl) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.bio = "";
        this.imageUrl = "";
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        this.likedRecipes = new ArrayList<>();
        this.uploadedRecipes = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getFollowers() { return followers; }
    public void setFollowers(List<String> followers) { this.followers = followers; }

    public List<String> getFollowing() { return following; }
    public void setFollowing(List<String> following) { this.following = following; }

    public List<String> getLikedRecipes() { return likedRecipes; }
    public void setLikedRecipes(List<String> likedRecipes) { this.likedRecipes = likedRecipes; }

    public List<String> getUploadedRecipes() { return uploadedRecipes; }
    public void setUploadedRecipes(List<String> uploadedRecipes) { this.uploadedRecipes = uploadedRecipes; }

    public int getFollowersCount() { return followers != null ? followers.size() : 0; }
    public int getFollowingCount() { return following != null ? following.size() : 0; }
    public int getUploadedRecipesCount() { return uploadedRecipes != null ? uploadedRecipes.size() : 0; }

    public boolean isFollowing(String userId) {
        return following != null && following.contains(userId);
    }

    public void addFollower(String userId) {
        if (followers == null) followers = new ArrayList<>();
        if (!followers.contains(userId)) followers.add(userId);
    }

    public void removeFollower(String userId) {
        if (followers != null) followers.remove(userId);
    }

    public void addFollowing(String userId) {
        if (following == null) following = new ArrayList<>();
        if (!following.contains(userId)) following.add(userId);
    }

    public void removeFollowing(String userId) {
        if (following != null) following.remove(userId);
    }

    public void addLikedRecipe(String recipeId) {
        if (likedRecipes == null) likedRecipes = new ArrayList<>();
        if (!likedRecipes.contains(recipeId)) likedRecipes.add(recipeId);
    }

    public void removeLikedRecipe(String recipeId) {
        if (likedRecipes != null) likedRecipes.remove(recipeId);
    }

    public void addUploadedRecipe(String recipeId) {
        if (uploadedRecipes == null) uploadedRecipes = new ArrayList<>();
        if (!uploadedRecipes.contains(recipeId)) uploadedRecipes.add(recipeId);
    }

    public void removeUploadedRecipe(String recipeId) {
        if (uploadedRecipes != null) uploadedRecipes.remove(recipeId);
    }

    // Alias methods for compatibility
    public String getDisplayName() { return name; }
    public void setDisplayName(String name) { this.name = name; }

    public String getPhotoUrl() { return profileImageUrl; }
    public void setPhotoUrl(String photoUrl) { this.profileImageUrl = photoUrl; }
} 