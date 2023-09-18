package com.example.facebook;

import java.util.ArrayList;
import java.util.List;

public class Comment {
    private String commentId;
    private String userId;
    private String text;
    private long timestamp;
    private int likes;
    private List<Reply> replies;

    public Comment() {
        // Default constructor required for Firebase
    }


    public Comment(String commentId, String userId, String text, long timestamp) {
        this.commentId = commentId;
        this.userId = userId;
        this.text = text;
        this.timestamp = timestamp;
        this.likes = 0; // Initialize likes to 0
        this.replies = new ArrayList<>();
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public List<Reply> getReplies() {
        return replies;
    }

    public void setReplies(List<Reply> replies) {
        this.replies = replies;
    }
}
