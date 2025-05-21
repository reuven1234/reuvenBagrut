package com.example.reuvenbagrut.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    private String id;
    private String text;
    private String senderId;
    private String senderName;
    private String senderImage;
    private long timestamp;

    public ChatMessage() {
        // Required empty constructor for Firestore
    }

    public ChatMessage(String id, String text, String senderId, String senderName, String senderImage, long timestamp) {
        this.id = id;
        this.text = text;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderImage = senderImage;
        this.timestamp = timestamp;
    }

    public ChatMessage(String id, String text, String senderId, String senderName) {
        this.id = id;
        this.text = text;
        this.senderId = senderId;
        this.senderName = senderName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // For compatibility with legacy code
    public String getMessage() {
        return text;
    }
} 