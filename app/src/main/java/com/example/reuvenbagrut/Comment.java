package com.example.reuvenbagrut;

public class Comment {
    private String userId;
    private String userName;
    private String userProfileImage;
    private String content;
    private long timestamp;

    public Comment() {
        // Empty constructor for Firebase
    }

    public Comment(String userId, String userName, String userProfileImage, String content) {
        this.userId = userId;
        this.userName = userName;
        this.userProfileImage = userProfileImage;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 