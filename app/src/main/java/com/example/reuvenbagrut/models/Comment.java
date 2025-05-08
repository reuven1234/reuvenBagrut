package com.example.reuvenbagrut.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Comment {
    private String id;
    private String userId;
    private String text;
    private Date timestamp;

    public Comment() {
        // Default constructor required for Firebase
    }

    public Comment(String userId, String text) {
        this.userId = userId;
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

    public String getFormattedTime() {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(timestamp);
    }

    public User getUser() {
        // This is a placeholder - the actual user data should be loaded from Firestore
        // when the comment is displayed
        return null;
    }
} 