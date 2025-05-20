package com.example.reuvenbagrut.models;

import java.util.List;
import java.util.ArrayList;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class Chat {
    private String id;
    @PropertyName("participants")
    private List<String> participants;
    @PropertyName("lastMessage")
    private String lastMessage;
    @PropertyName("lastMessageTime")
    private long lastMessageTime;
    @PropertyName("lastMessageSenderId")
    private String lastMessageSenderId;
    @PropertyName("lastMessageSenderName")
    private String lastMessageSenderName;
    @PropertyName("lastMessageSenderImage")
    private String lastMessageSenderImage;

    public Chat() {
        // Required empty constructor for Firestore
        this.participants = new ArrayList<>();
    }

    public Chat(List<String> participants) {
        this.participants = participants;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("participants")
    public List<String> getParticipants() { return participants; }
    @PropertyName("participants")
    public void setParticipants(List<String> participants) { this.participants = participants; }

    @PropertyName("lastMessage")
    public String getLastMessage() { return lastMessage; }
    @PropertyName("lastMessage")
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    @PropertyName("lastMessageTime")
    public long getLastMessageTime() { return lastMessageTime; }
    @PropertyName("lastMessageTime")
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    @PropertyName("lastMessageSenderId")
    public String getLastMessageSenderId() { return lastMessageSenderId; }
    @PropertyName("lastMessageSenderId")
    public void setLastMessageSenderId(String lastMessageSenderId) { this.lastMessageSenderId = lastMessageSenderId; }

    @PropertyName("lastMessageSenderName")
    public String getLastMessageSenderName() { return lastMessageSenderName; }
    @PropertyName("lastMessageSenderName")
    public void setLastMessageSenderName(String lastMessageSenderName) { this.lastMessageSenderName = lastMessageSenderName; }

    @PropertyName("lastMessageSenderImage")
    public String getLastMessageSenderImage() { return lastMessageSenderImage; }
    @PropertyName("lastMessageSenderImage")
    public void setLastMessageSenderImage(String lastMessageSenderImage) { this.lastMessageSenderImage = lastMessageSenderImage; }

    public void updateLastMessage(ChatMessage message) {
        this.lastMessage = message.getMessage();
        this.lastMessageTime = message.getTimestamp();
        this.lastMessageSenderId = message.getSenderId();
        this.lastMessageSenderName = message.getSenderName();
        this.lastMessageSenderImage = message.getSenderImage();
    }

    public String getOtherParticipantId(String currentUserId) {
        if (participants == null || participants.size() != 2) return null;
        return participants.get(0).equals(currentUserId) ? participants.get(1) : participants.get(0);
    }
} 