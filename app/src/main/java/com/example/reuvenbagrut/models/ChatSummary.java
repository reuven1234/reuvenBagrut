// File: app/src/main/java/com/example/reuvenbagrut/models/ChatSummary.java
package com.example.reuvenbagrut.models;

public class ChatSummary {
    private String chatId;
    private String name;
    private String lastMessage;
    private long   timestamp;
    private String avatarUrl;

    // Firestore requires a no-arg constructor
    public ChatSummary() {}

    public ChatSummary(String chatId, String name, String lastMessage, long timestamp, String avatarUrl) {
        this.chatId      = chatId;
        this.name        = name;
        this.lastMessage = lastMessage;
        this.timestamp   = timestamp;
        this.avatarUrl   = avatarUrl;
    }

    public String getChatId()      { return chatId; }
    public String getName()        { return name; }
    public String getLastMessage() { return lastMessage; }
    public long   getTimestamp()   { return timestamp; }
    public String getAvatarUrl()   { return avatarUrl; }

    public void setChatId(String chatId)         { this.chatId = chatId; }
    public void setName(String name)             { this.name = name; }
    public void setLastMessage(String msg)       { this.lastMessage = msg; }
    public void setTimestamp(long timestamp)     { this.timestamp = timestamp; }
    public void setAvatarUrl(String avatarUrl)   { this.avatarUrl = avatarUrl; }
}
