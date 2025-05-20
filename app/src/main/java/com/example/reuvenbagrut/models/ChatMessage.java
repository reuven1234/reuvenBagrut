package com.example.reuvenbagrut.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    private String id;
    private String senderId;
    private String senderName;
    private String senderImage;
    private String message;
    private long timestamp;

    public ChatMessage() {
        // Required empty constructor for Firestore
    }

    public ChatMessage(String senderId, String senderName, String senderImage, String message) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderImage = senderImage;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderImage() { return senderImage; }
    public void setSenderImage(String senderImage) { this.senderImage = senderImage; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
} 