package com.example.reuvenbagrut.models;

public class ChatPreview {

    /*  chat doc id  */
    private String documentId;      // ← חדש

    private String otherUid;
    private String otherName;
    private String lastMessage;
    private long   lastMessageTime;
    private String profileImageUrl; // New field

    public ChatPreview() { }

    public ChatPreview(String otherUid,
                       String otherName,
                       String lastMessage,
                       long lastMessageTime) {
        this.otherUid        = otherUid;
        this.otherName       = otherName;
        this.lastMessage     = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.profileImageUrl = null; // Initialize to null
    }

    public ChatPreview(String otherUid,
                       String otherName,
                       String lastMessage,
                       long lastMessageTime,
                       String profileImageUrl) {
        this.otherUid        = otherUid;
        this.otherName       = otherName;
        this.lastMessage     = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.profileImageUrl = profileImageUrl;
    }

    /* ---------- id ---------- */
    public String getDocumentId()            { return documentId; }
    public void   setDocumentId(String id)   { this.documentId = id; }

    /* ---------- rest ---------- */
    public String getOtherUid()              { return otherUid; }
    public void   setOtherUid(String v)      { otherUid = v; }

    public String getOtherName()             { return otherName; }
    public void   setOtherName(String v)     { otherName = v; }

    public String getLastMessage()           { return lastMessage; }
    public void   setLastMessage(String v)   { lastMessage = v; }

    public long   getLastMessageTime()       { return lastMessageTime; }
    public void   setLastMessageTime(long v) { lastMessageTime = v; }

    public String getProfileImageUrl()       { return profileImageUrl; }
    public void   setProfileImageUrl(String v) { profileImageUrl = v; }
}
