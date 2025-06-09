package com.example.reuvenbagrut.models;

import java.util.List;
import java.util.ArrayList;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class Chat {
    private String chatId;
    private List<String> participants;
    private String lastMessage;
    private long lastMessageTime;
    private String otherUserId;
    private String otherUserName;
    private String otherUserImage;
    private boolean isRead;

    public Chat() {
        // Required empty constructor for Firestore
        this.participants = new ArrayList<>();
    }

    public Chat(List<String> participants) {
        this.participants = participants;
    }

    // Getters and Setters
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserImage() {
        return otherUserImage;
    }

    public void setOtherUserImage(String otherUserImage) {
        this.otherUserImage = otherUserImage;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void updateLastMessage(ChatMessage message) {
        this.lastMessage = message.getMessage();
        this.lastMessageTime = message.getTimestamp();
        this.otherUserId = message.getSenderId();
        this.otherUserName = message.getSenderName();
        this.otherUserImage = message.getSenderImage();
    }

    public String getOtherParticipantId(String currentUserId) {
        if (participants == null || participants.size() != 2) return null;
        return participants.get(0).equals(currentUserId) ? participants.get(1) : participants.get(0);
    }
} 