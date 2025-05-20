package com.example.reuvenbagrut.models;

public class Comment {
    private String id;
    private String recipeId;
    private String userId;
    private String userName;
    private String userImage;
    private String content;
    private long timestamp;

    public Comment() {
        // Required empty constructor for Firestore
    }

    public Comment(String id, String recipeId, String userId, String userName, String userImage, String content) {
        this.id = id;
        this.recipeId = recipeId;
        this.userId = userId;
        this.userName = userName;
        this.userImage = userImage;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserImage() { return userImage; }
    public void setUserImage(String userImage) { this.userImage = userImage; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
} 