// File: app/src/main/java/com/example/reuvenbagrut/models/ChatSummary.java
package com.example.reuvenbagrut.models;

public class ChatSummary {
    private String id;
    private String lastMessage;
    private Long lastMessageTime;
    private String otherUserId;
    private String otherUserName;
    private String otherUserImage;
    
    public ChatSummary() {
        // Required empty constructor for Firestore
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getLastMessage() {
        return lastMessage;
    }
    
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public Long getLastMessageTime() {
        return lastMessageTime;
    }
    
    public void setLastMessageTime(Long lastMessageTime) {
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
}
