package com.example.reuvenbagrut;

import com.google.firebase.Timestamp;
import java.util.Date;

public class Comment {
    private String id;
    private String userId;
    private String userName;
    private String userPhotoUrl;
    private String text;
    private Date timestamp;

    public Comment() {
        // Default constructor required for Firestore
    }

    public Comment(String userId, String userName, String userPhotoUrl, String text) {
        this.userId = userId;
        this.userName = userName;
        this.userPhotoUrl = userPhotoUrl;
        this.text = text;
        this.timestamp = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // For Firestore timestamp conversion
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp.toDate();
    }
} 