package com.example.reuvenbagrut.models;

public class ChatPreview {
    private String otherUid;
    private String otherName;
    private String lastMessage;
    private long   lastMessageTime;

    public ChatPreview() { }

    public ChatPreview(String otherUid,
                       String otherName,
                       String lastMessage,
                       long lastMessageTime) {
        this.otherUid        = otherUid;
        this.otherName       = otherName;
        this.lastMessage     = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public String getOtherUid()          { return otherUid; }
    public void   setOtherUid(String v)  { otherUid = v; }

    public String getOtherName()         { return otherName; }
    public void   setOtherName(String v) { otherName = v; }

    public String getLastMessage()       { return lastMessage; }
    public void   setLastMessage(String v){ lastMessage = v; }

    public long   getLastMessageTime()   { return lastMessageTime; }
    public void   setLastMessageTime(long v){ lastMessageTime = v; }
}
