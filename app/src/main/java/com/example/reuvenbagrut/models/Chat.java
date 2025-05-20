package com.example.reuvenbagrut.models;

import java.util.List;
import java.util.ArrayList;
import com.google.firebase.Timestamp;

public class Chat {
    private String id;
    private List<String> participants;
    private String lastMessage;
    private long lastMessageTime;
    private String lastMessageSenderId;
    private String lastMessageSenderName;
    private String lastMessageSenderImage;

    public Chat() {
        // Required empty constructor for Firestore
    }

    public Chat(List<String> participants) {
        this.participants = participants;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public String getLastMessageSenderId() { return lastMessageSenderId; }
    public void setLastMessageSenderId(String lastMessageSenderId) { this.lastMessageSenderId = lastMessageSenderId; }

    public String getLastMessageSenderName() { return lastMessageSenderName; }
    public void setLastMessageSenderName(String lastMessageSenderName) { this.lastMessageSenderName = lastMessageSenderName; }

    public String getLastMessageSenderImage() { return lastMessageSenderImage; }
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