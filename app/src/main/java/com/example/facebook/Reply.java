package com.example.facebook;

public class Reply {
    private String replyText;
    private String userId; // User ID of the user who replied
    private long timestamp; // Timestamp of the reply

    public Reply() {
        // Default constructor required for Firebase
    }

    public Reply(String replyText, String userId, long timestamp) {
        this.replyText = replyText;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
