package com.example.reuvenbagrut.models;

import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessage {
    private String id;
    private String senderId;
    private String senderName;
    private String senderImage;
    private String message;
    private long timestamp;
    private boolean seen;
    private boolean read;
    private List<String> attachments;

    // No-arg constructor
    public ChatMessage() {}

    // Full constructor (add more as needed)
    public ChatMessage(String id, String senderId, String senderName, String senderImage, String message, long timestamp, boolean seen, boolean read) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderImage = senderImage;
        this.message = message;
        this.timestamp = timestamp;
        this.seen = seen;
        this.read = read;
    }
// Add this inside ChatMessage.java

    public ChatMessage(String id, String senderId, String message, long timestamp, boolean seen) {
        this.id = id;
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.seen = seen;
        this.read = false; // default
    }

    // Common usage constructor
    public ChatMessage(String senderId, String senderName, String senderImage, String message, long timestamp, boolean seen, boolean read) {
        this(null, senderId, senderName, senderImage, message, timestamp, seen, read);
    }

    // Minimal
    public ChatMessage(String senderId, String message, long timestamp, boolean seen) {
        this(null, senderId, null, null, message, timestamp, seen, false);
    }

    // Extra - for previews/old code
    public ChatMessage(String senderId, String senderName, String senderImage, String message) {
        this(null, senderId, senderName, senderImage, message, 0, false, false);
    }

    // Static method for Firebase snapshot
    public static ChatMessage fromDocument(DocumentSnapshot doc) {
        String id = doc.getId();
        String senderId = doc.getString("senderId");
        String senderName = doc.getString("senderName");
        String senderImage = doc.getString("senderImage");
        String message = doc.getString("message");
        long timestamp = doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0L;
        boolean seen = doc.getBoolean("seen") != null ? doc.getBoolean("seen") : false;
        boolean read = doc.getBoolean("read") != null ? doc.getBoolean("read") : false;
        return new ChatMessage(id, senderId, senderName, senderImage, message, timestamp, seen, read);
    }

    // --- All getters/setters ---
    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getSenderImage() { return senderImage; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public boolean isSeen() { return seen; }
    public boolean isRead() { return read; }
    public void setId(String id) { this.id = id; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setSenderImage(String senderImage) { this.senderImage = senderImage; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setSeen(boolean seen) { this.seen = seen; }
    public void setRead(boolean read) { this.read = read; }

    // -- Your required methods --
    public String getFormattedTime() {
        if (timestamp == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
